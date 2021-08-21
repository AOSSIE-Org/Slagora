package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.repositories.AuthInfoRepository
import com.mohiva.play.silhouette.api.util.{Clock, Credentials, ExtractableRequest, PasswordHasherRegistry}
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth2.FacebookProvider
import formatters.json.Token
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.security.{SlackTeamBuilder, SlackUser, SlackUserBuilder}
import play.api.Configuration
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import service.{TeamService, UserService}
import slack_api.SlackAPIService
import slack_auth.{SlackTeamProvider, SlackUserProvider}
import utils.auth.DefaultEnv
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "Authentication")
class CredentialsAuthController @Inject()(components: ControllerComponents,
                                          userService: UserService,
                                          teamService: TeamService,
                                          configuration: Configuration,
                                          silhouette: Silhouette[DefaultEnv],
                                          clock: Clock,
                                          credentialsProvider: CredentialsProvider,
                                          authInfoRepository: AuthInfoRepository,
                                          passwordHasherRegistry: PasswordHasherRegistry,
                                          slackAPIService: SlackAPIService,
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
    (socialProviderRegistry.get[SocialProvider](provider) match {
      case Some(p: SocialProvider) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with unexpected social provider $provider"))
    }).recover {
      case e: ProviderException => InternalServerError("Server Error")
    }
  }

  /**
    * Completes the authentication for users on slack URL redirect
    * @return The result to send.
   */

  def authenticateUser() = Action.async { implicit request: Request[AnyContent] =>
    ( (socialProviderRegistry.get[SocialProvider]("slack_user"), request.getQueryString("code")) match {
      case ( Some(p: SlackUserProvider), Some(code: String) ) =>
        p.buildProfileFromAccessCode(code).flatMap{
          profile =>
            userService.get(profile.loginInfo).flatMap{
              case Some(user) => silhouette.env.authenticatorService
                .create(profile.loginInfo)
                .map {
                  case authenticator => authenticator
                }
                .flatMap { authenticator =>
                  silhouette.env.eventBus.publish(LoginEvent(user, request))
                  silhouette.env.authenticatorService
                    .init(authenticator)
                    .flatMap { token =>
                      silhouette.env.authenticatorService
                        .embed(
                          token,
                          Ok(
                            Json.toJson(Json.obj("user" -> user, "token" -> Token(
                              token,
                              authenticator.expirationDateTime)))))
                    }
                }
              case _ => userService.save(profile)
                .flatMap{
                  case true =>
                    silhouette.env.authenticatorService
                      .create(profile.loginInfo)
                      .map {
                        case authenticator => authenticator
                      }
                      .flatMap { authenticator =>
                        silhouette.env.eventBus.publish(LoginEvent(profile, request))
                        silhouette.env.authenticatorService
                          .init(authenticator)
                          .flatMap { token =>
                            silhouette.env.authenticatorService
                              .embed(
                                token,
                                Ok(
                                  Json.toJson(Json.obj("user" -> profile, "token" -> Token(
                                    token,
                                    authenticator.expirationDateTime)))))
                          }
                      }
                  case _ =>  Future.successful(InternalServerError("Server Error"))
                }
            }
        }
    }).recover {
      case e: ProviderException => InternalServerError("Server Error")
    }
  }

  /**
    * Completes the authentication for teams on slack URL redirect
    * @return The result to send.
    */

  def authenticateTeam() = Action.async { implicit request: Request[AnyContent] =>
    ( (socialProviderRegistry.get[SocialProvider]("slack_team"), request.getQueryString("code")) match {
      case ( Some(p: SlackTeamProvider), Some(code: String) ) =>
        p.buildProfileFromAccessCode(code).flatMap{
          profile =>
            teamService.get(profile.loginInfo).flatMap{
              case Some(_) => Future.successful(Conflict("Team already exists"))
              case _ => teamService.save(profile).flatMap { _ =>
                slackAPIService.userSignUp(profile.webHook, profile).flatMap(response => Future.successful(logger.error(s"Failed to validate with error: $response")))
                Future.successful(Ok("Thank you for using Slagora. Your team been registered successfully. You can now go back to Slack"))
            }
          }
        }
    }).recover {
      case e: ProviderException => InternalServerError("Server Error")
    }
  }
}
