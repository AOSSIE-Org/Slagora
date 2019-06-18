package slack_auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.ProfileRetrievalException
import com.mohiva.play.silhouette.impl.providers._
import slack_auth.SlackUserProvider._
import models.security.{SlackUser, SlackUserBuilder, Team}
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.{AnyContent, Request, RequestHeader}
import scala.concurrent.Future

/**
  * Base SlackUser OAuth2 Provider.
  *
  * @see https://api.slack.com/docs/sign-in-with-slack
  */
trait BaseSlackUserProvider extends OAuth2Provider {

  /**
    * The content type to parse a profile from.
    */
  override type Content = JsValue

  /**
    * The provider ID.
    */
  override val id = ID

  /**
    * Defines the URLs that are needed to retrieve the profile data.
    */
  override protected val urls = Map("api" -> settings.apiURL.getOrElse(API))

  /**
    * Builds the social profile.
    *
    * @param authInfo The auth info received from the provider.
    * @return On success the build social profile, otherwise a failure.
    */
  override protected def buildProfile(authInfo: OAuth2Info): Future[Profile] = {
    httpLayer.url(urls("api")).withQueryStringParameters(("token", authInfo.accessToken)).get().flatMap { response =>
      val json = response.json
      (json \ "error").asOpt[JsObject] match {
        case Some(error) =>
          val errorMsg = (error \ "message").as[String]
          val errorType = (error \ "type").as[String]
          val errorCode = (error \ "code").as[Int]

          throw new ProfileRetrievalException(SpecifiedProfileError.format(id, errorMsg, errorType, errorCode))
        case _ => profileParser.parse(json, authInfo)
      }
    }
  }
}

/**
  * The profile parser for the common social profile.
  */
class SlackUserProfileParser extends SocialProfileParser[JsValue, SlackUser, OAuth2Info] {

  /**
    * Parses the social profile.
    *
    * @param json     The content returned from the provider.
    * @param authInfo The auth info to query the provider again for additional data.
    * @return The social profile from given result.
    */
  override def parse(json: JsValue, authInfo: OAuth2Info) = Future.successful {
    val id = (json \ "user"\ "id").as[String]
    val name = (json \ "user"\ "name").as[String]
    val email = (json \ "user"\ "email").as[String]
    val image_24 = (json \ "user"\ "image_24").as[String]
    val image_32 = (json \ "user"\ "image_32").as[String]
    val image_48 = (json \ "user"\ "image_48").as[String]
    val image_72 = (json \ "user"\ "image_72").as[String]
    val image_192 = (json \ "user"\ "image_192").as[String]
    val image_512 = (json \ "user"\ "image_512").as[String]
    val teamId = (json \ "team" \ "id").as[String]
    val teamName = (json \ "team" \ "name").as[String]

    SlackUser(
      loginInfo = LoginInfo(ID, s"user_id=${id}&team_id=${teamId}"),
      id = id,
      name = name,
      email = email,
      image_24 = image_24,
      image_32 = image_32,
      image_48 = image_48,
      image_72 = image_72,
      image_192 = image_192,
      image_512 = image_512,
      team = Team(teamId, teamName)
    )
  }
}

/**
  * The Slack OAuth2 Provider.
  *
  * @param httpLayer     The HTTP layer implementation.
  * @param stateHandler  The state provider implementation.
  * @param settings      The provider settings.
  */
class SlackUserProvider(
                        protected val httpLayer: HTTPLayer,
                        protected val stateHandler: SocialStateHandler,
                        val settings: OAuth2Settings)
  extends BaseSlackUserProvider with SlackUserBuilder {

  /**
    * The type of this class.
    */
  override type Self = SlackUserProvider

  /**
    * The profile parser implementation.
    */
  override val profileParser = new SlackUserProfileParser

  /**
    * Gets a provider initialized with a new settings object.
    *
    * @param f A function which gets the settings passed and returns different settings.
    * @return An instance of the provider initialized with new settings.
    */
  override def withSettings(f: (Settings) => Settings) = new SlackUserProvider(httpLayer, stateHandler, f(settings))

  def buildProfileFromAccessCode(code: String)(implicit request: Request[AnyContent]): Future[Profile] = {
    this.getAccessToken(code).flatMap {
      case oAuth2Info =>
        this.buildProfile(oAuth2Info.asInstanceOf[this.A])
      case _ => Future.failed(new ProfileRetrievalException("Fail to build profile. Could not retrieve oAuth2Info"))
    }
  }
}

/**
  * The companion object.
  */
object SlackUserProvider {

  /**
    * The error messages.
    */
  val SpecifiedProfileError = "[Silhouette][%s] Error retrieving profile information. Error message: %s, type: %s, code: %s"

  /**
    * The SlackUser constants.
    */
  val ID = "slack_user"
  val API = "https://slack.com/api/users.identity"
}
