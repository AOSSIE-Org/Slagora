package slack_auth

import java.net.URLEncoder._

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions._
import com.mohiva.play.silhouette.api.util.ExtractableRequest
import com.mohiva.play.silhouette.impl.exceptions.{AccessDeniedException, UnexpectedResponseException}
import com.mohiva.play.silhouette.impl.providers.OAuth2Provider._
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.state.UserStateItemHandler
import models.security.{Bot, WebHook}
import play.api.libs.functional.syntax._
import play.api.libs.json
import play.api.libs.json._
import play.api.libs.ws.WSResponse
import play.api.mvc._

import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

/**
  * The OAuth2 info.
  *
  * @param access_token      The access token.
  * @param tokenType        The token type.
  * @param expiresIn        The number of seconds before the token expires.
  * @param refreshToken     The refresh token.
  * @param params           Additional params transported in conjunction with the token.
  * @param scope            The scope
  * @param team_name        The Slack team name
  * @param team_id          The Slack team id
  * @param incoming_webhook The teams incoming web hook object
  * @param bot              The teams bot object
  */
case class SlackTeamOAuth2Info(
                                access_token: String,
                                tokenType: Option[String] = None,
                                expiresIn: Option[Int] = None,
                                refreshToken: Option[String] = None,
                                params: Option[Map[String, String]] = None,
                                scope: String,
                                team_name: String,
                                team_id: String,
                                incoming_webhook: WebHook,
                                bot: Bot
                              ) extends AuthInfo

object SlackTeamOAuth2Info extends OAuth2Constants {
  implicit val infoReads: json.Format[SlackTeamOAuth2Info] = Json.format[SlackTeamOAuth2Info]
}

/**
  * Base implementation for all OAuth2 providers.
  */
trait SlackTeamOAuth2Provider extends SocialStateProvider with OAuth2Constants with Logger {

  /**
    * The type of the auth info.
    */
  type A = SlackTeamOAuth2Info

  /**
    * The settings type.
    */
  type Settings = OAuth2Settings

  /**
    * The social state handler implementation.
    */
  protected val stateHandler: SocialStateHandler

  /**
    * A list with headers to send to the API.
    */
  protected val headers: Seq[(String, String)] = Seq()

  /**
    * The default access token response code.
    *
    * Override this if a specific provider uses another HTTP status code for a successful access token response.
    */
  protected val accessTokeResponseCode: Int = 200

  /**
    * The implicit access token reads.
    *
    * Override this if a specific provider needs another reads.
    */
  implicit protected val accessTokenReads: Reads[OAuth2Info] = OAuth2Info.infoReads

  /**
    * Starts the authentication process.
    *
    * @param request The current request.
    * @tparam B The type of the request body.
    * @return Either a Result or the auth info from the provider.
    */
  def authenticate[B]()(implicit request: ExtractableRequest[B]): Future[Either[Result, SlackTeamOAuth2Info]] = {
    handleFlow(handleAuthorizationFlow(stateHandler)) { code =>
      stateHandler.unserialize(request.extractString(State).getOrElse("")).flatMap { _ =>
        getAccessToken(code).map(oauth2Info => oauth2Info)
      }
    }
  }

  /**
    * Authenticates the user and returns the auth information and the user state.
    *
    * Returns either a [[StatefulAuthInfo]] if all went OK or a `play.api.mvc.Result` that the controller
    * sends to the browser (e.g.: in the case of OAuth where the user needs to be redirected to the service
    * provider).
    *
    * @tparam S The type of the user state.
    * @tparam B The type of the request body.
    * @param format   The JSON format to the transform the user state into JSON.
    * @param request  The request.
    * @param classTag The class tag for the user state item.
    * @return Either a `play.api.mvc.Result` or the [[StatefulAuthInfo]] from the provider.
    */
  def authenticate[S <: SocialStateItem, B](userState: S)(
    implicit
    format: Format[S],
    request: ExtractableRequest[B],
    classTag: ClassTag[S]
  ): Future[Either[Result, StatefulAuthInfo[A, S]]] = {
    val userStateItemHandler = new UserStateItemHandler(userState)
    val newStateHandler = stateHandler.withHandler(userStateItemHandler)

    handleFlow(handleAuthorizationFlow(newStateHandler)) { code =>
      newStateHandler.unserialize(request.extractString(State).getOrElse("")).flatMap { state =>
        val maybeUserState: Option[S] = state.items.flatMap(item => userStateItemHandler.canHandle(item)).headOption
        maybeUserState match {
          case Some(s) => getAccessToken(code).map(oauth2Info => StatefulAuthInfo(oauth2Info, s))
          case None => Future.failed(new UnexpectedResponseException("Cannot extract user info from response"))
        }
      }
    }
  }

  /**
    * Handles the OAuth2 flow.
    *
    * The left flow is the authorization flow, which will be processed, if no `code` parameter exists
    * in the request. The right flow is the access token flow, which will be executed after a successful
    * authorization.
    *
    * @param left    The authorization flow.
    * @param right   The access token flow.
    * @param request The request.
    * @tparam L The return type of the left flow.
    * @tparam R The return type of the right flow.
    * @tparam B The type of the request body.
    * @return Either the left or the right flow.
    */
  def handleFlow[L, R, B](left: => Future[L])(right: String => Future[R])(
    implicit
    request: ExtractableRequest[B]
  ): Future[Either[L, R]] = {
    request.extractString(Error).map {
      case e@AccessDenied => new AccessDeniedException(AuthorizationError.format(id, e))
      case e => new UnexpectedResponseException(AuthorizationError.format(id, e))
    } match {
      case Some(throwable) => Future.failed(throwable)
      case None => request.extractString(Code) match {
        // We're being redirected back from the authorization server with the access code and the state
        case Some(code) => right(code).map(Right.apply)
        // There's no code in the request, this is the first step in the OAuth flow
        case None => left.map(Left.apply)
      }
    }
  }

  /**
    * Handles the authorization step of the OAuth2 flow.
    *
    * @tparam B The type of the request body.
    * @param stateHandler The state handler to use.
    * @param request      The request.
    * @return The redirect to the authorization URL of the OAuth2 provider.
    */
  protected def handleAuthorizationFlow[B](stateHandler: SocialStateHandler)(
    implicit
    request: ExtractableRequest[B]
  ): Future[Result] = {
    stateHandler.state.map { state =>
      val serializedState = stateHandler.serialize(state)
      val stateParam = if (serializedState.isEmpty) List() else List(State -> serializedState)
      val redirectParam = settings.redirectURL match {
        case Some(rUri) => List((RedirectURI, resolveCallbackURL(rUri)))
        case None => Nil
      }
      val params = settings.scope.foldLeft(List(
        (ClientID, settings.clientID),
        (ResponseType, Code)) ++ stateParam ++ settings.authorizationParams.toList ++ redirectParam) {
        case (p, s) => (Scope, s) :: p
      }
      val encodedParams = params.map { p => encode(p._1, "UTF-8") + "=" + encode(p._2, "UTF-8") }
      val url = settings.authorizationURL.getOrElse {
        throw new ConfigurationException(AuthorizationURLUndefined.format(id))
      } + encodedParams.mkString("?", "&", "")
      val redirect = stateHandler.publish(Results.Redirect(url), state)
      logger.debug("[Silhouette][%s] Use authorization URL: %s".format(id, settings.authorizationURL))
      logger.debug("[Silhouette][%s] Redirecting to: %s".format(id, url))
      redirect
    }
  }

  /**
    * Gets the access token.
    *
    * @param code    The access code.
    * @param request The current request.
    * @return The info containing the access token.
    */
  protected def getAccessToken(code: String)(implicit request: RequestHeader): Future[SlackTeamOAuth2Info] = {
    val redirectParam = settings.redirectURL match {
      case Some(rUri) => List((RedirectURI, resolveCallbackURL(rUri)))
      case None => Nil
    }
    val params = Map(
      ClientID -> Seq(settings.clientID),
      ClientSecret -> Seq(settings.clientSecret),
      GrantType -> Seq(AuthorizationCode),
      Code -> Seq(code)) ++ settings.accessTokenParams.mapValues(Seq(_)) ++ redirectParam.toMap.mapValues(Seq(_))
    httpLayer.url(settings.accessTokenURL).withHttpHeaders(headers: _*).post(params).flatMap { response =>
      logger.debug("[Silhouette][%s] Access token response: [%s]".format(id, response.body))
      Future.fromTry(buildInfo(response))
    }
  }

  /**
    * Builds the OAuth2 info from response.
    *
    * @param response The response from the provider.
    * @return The OAuth2 info on success, otherwise a failure.
    */
  protected def buildInfo(response: WSResponse): Try[SlackTeamOAuth2Info] = {
    response.status match {
      case status if status == accessTokeResponseCode =>
        Try(response.json) match {
          case Success(json) => json.validate[SlackTeamOAuth2Info].asEither.fold(
            error => Failure(new UnexpectedResponseException(InvalidInfoFormat.format(id, error))),
            info => Success(info)
          )
          case Failure(error) => Failure(
            new UnexpectedResponseException(JsonParseError.format(id, response.body, error))
          )
        }
      case status => Failure(
        new UnexpectedResponseException(UnexpectedResponse.format(id, response.body, status))
      )
    }
  }
}

/**
  * The SlackTeamOAuth2Provider companion object.
  */
object SlackTeamOAuth2Provider extends OAuth2Constants {

  /**
    * The error messages.
    */
  val AuthorizationURLUndefined = "[Silhouette][%s] Authorization URL is undefined"
  val AuthorizationError = "[Silhouette][%s] Authorization server returned error: %s"
  val InvalidInfoFormat = "[Silhouette][%s] Cannot build OAuth2Info because of invalid response format: %s"
  val JsonParseError = "[Silhouette][%s] Cannot parse response `%s` to Json; got error: %s"
  val UnexpectedResponse = "[Silhouette][%s] Got unexpected response `%s`; status code: %s"
}