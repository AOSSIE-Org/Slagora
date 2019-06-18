package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, ExtractableRequest, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.security.SlackUserBuilder
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import service.UserService
import slack_auth.SlackUserProvider
import utils.auth.DefaultEnv
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Authentication")
class CredentialsAuthController @Inject()(components: ControllerComponents,
                                          userService: UserService,
                                          configuration: Configuration,
                                          silhouette: Silhouette[DefaultEnv],
                                          clock: Clock,
                                          credentialsProvider: CredentialsProvider,
                                          authInfoRepository: AuthInfoRepository,
                                          passwordHasherRegistry: PasswordHasherRegistry,
                                          messagesApi: MessagesApi,
                                          socialProviderRegistry: SocialProviderRegistry)
                                         (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport with Logger {

  /**
    * Begins the authentication flow a user against a social provider.
    *
    * @param provider The ID of the provider to authenticate against.
    * @return The result to send.
    */
  def authenticate(provider: String) = Action.async { implicit request: Request[AnyContent] =>
      logger.error(provider.toString)
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider with SlackUserBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
          } yield {
            Ok(Json.toJson(profile))
          }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException => InternalServerError("Server Error")
    }
  }

  /**
    * Completes the authentication on slack URL redirect
    * @return The result to send.
   */

  def authenticateUser() = Action.async { implicit request: Request[AnyContent] =>
    ( (socialProviderRegistry.get[SocialProvider]("slack_user"), request.getQueryString("code")) match {
      case ( Some(p: SlackUserProvider), Some(code: String) ) =>
        p.buildProfileFromAccessCode(code).flatMap{
          profile => Future.successful(Ok(Json.toJson(profile)))
        }
    }).recover {
      case e: ProviderException => InternalServerError("Server Error")
    }
  }
}
