package slack_auth

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.exceptions.{ProfileRetrievalException, UnexpectedResponseException}
import com.mohiva.play.silhouette.impl.providers.OAuth2Provider.{InvalidInfoFormat, JsonParseError, UnexpectedResponse}
import com.mohiva.play.silhouette.impl.providers._
import slack_auth.SlackTeamProvider._
import models.security._
import play.api.libs.json.{JsNull, JsObject, JsValue}
import play.api.libs.ws.WSResponse
import play.api.mvc.{AnyContent, Request, RequestHeader}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

/**
  * Base SlackUser OAuth2 Provider.
  *
  * @see https://api.slack.com/docs/sign-in-with-slack
  */
trait BaseSlackTeamProvider extends SlackTeamOAuth2Provider {

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
  override protected def buildProfile(authInfo: SlackTeamOAuth2Info): Future[Profile] = {
    //The auth step ends here. Since the information required is already provided in the authInfo
    //There is thus no need to fetch the profile using the access token as it is done with the sign-in with slack flow
    //See https://api.slack.com/docs/slack-button

    profileParser.parse(
      JsNull, //Passing a null JsValue since non was fetched
      authInfo)
  }
}

/**
  * The profile parser for the common social profile.
  */
class SlackTeamProfileParser extends SocialProfileParser[JsValue, SlackTeam, SlackTeamOAuth2Info] {

  /**
    * Parses the social profile.
    *
    * @param json     The content returned from the provider.
    * @param authInfo The auth info to query the provider again for additional data.
    * @return The social profile from given result.
    */
  def parse(json: JsValue, authInfo: SlackTeamOAuth2Info): Future[SlackTeam] = Future.successful {
    //The add to slack flow ends at the authInfo step
    //Hence providing the profile directly into the authInfo fetch.
    //We just need to extract the required information from this authInfo
    //See https://api.slack.com/docs/slack-button
    SlackTeam(
      LoginInfo(ID, s"team_id=${authInfo.team_id}"),
      accessToken = authInfo.access_token,
      scope = authInfo.scope,
      name = authInfo.team_name,
      id = authInfo.team_id,
      webHook = authInfo.incoming_webhook,
      bot = authInfo.bot
    )
  }
}

/**
  * The Slack OAuth2 Provider.
  *
  * @param httpLayer    The HTTP layer implementation.
  * @param stateHandler The state provider implementation.
  * @param settings     The provider settings.
  */
class SlackTeamProvider(
                         protected val httpLayer: HTTPLayer,
                         protected val stateHandler: SocialStateHandler,
                         val settings: OAuth2Settings)
  extends BaseSlackTeamProvider with SlackTeamBuilder {

  /**
    * The type of this class.
    */
  override type Self = SlackTeamProvider

  /**
    * The profile parser implementation.
    */
  override val profileParser = new SlackTeamProfileParser

  /**
    * Gets a provider initialized with a new settings object.
    *
    * @param f A function which gets the settings passed and returns different settings.
    * @return An instance of the provider initialized with new settings.
    */
  override def withSettings(f: (Settings) => Settings) = new SlackTeamProvider(httpLayer, stateHandler, f(settings))

  def buildProfileFromAccessCode(code: String)(implicit request: Request[AnyContent]): Future[Profile] = {
    this.getAccessToken(code).flatMap {
      case oAuth2Info =>
        buildProfile(oAuth2Info)
      case _ => Future.failed(new ProfileRetrievalException("Fail to build profile. Could not retrieve oAuth2Info"))
    }
  }
}

/**
  * The companion object.
  */
object SlackTeamProvider {

  /**
    * The error messages.
    */
  val SpecifiedProfileError = "[Silhouette][%s] Error retrieving profile information. Error message: %s, type: %s, code: %s"

  /**
    * The SlackTeam constants.
    */
  val ID = "slack_team"
  val API = "https://slack.com/api/users.identity"
}
