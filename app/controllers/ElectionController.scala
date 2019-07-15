package controllers

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.security.{SlackTeam, SlackTeamBuilder, SlackUser, SlackUserBuilder}
import models.slack_api.SlashCommandPayLoad
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{AbstractController, AnyContent, ControllerComponents, Request}
import service.{TeamService, UserService}
import slack_api.SlackAPIService
import utils.auth.DefaultEnv
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}


@Api(value = "Election")
class ElectionController @Inject()(messagesApi: MessagesApi,
                                   components: ControllerComponents,
                                   silhouette: Silhouette[DefaultEnv],
                                   userService: UserService,
                                   teamService: TeamService,
                                   slackAPIService: SlackAPIService)
                                  (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport with Logger {

  def receiveSlashCommandPayLoad() = Action.async { implicit request: Request[AnyContent] =>
    val formdata = request.body.asFormUrlEncoded
    val payload = new SlashCommandPayLoad(
      teamId = formdata.get("team_id").head,
      teamName = formdata.get("team_domain").head,
      channelId = formdata.get("channel_id").head,
      channelName = formdata.get("channel_name").head,
      userId = formdata.get("user_id").head,
      userName = formdata.get("user_name").head,
      command = formdata.get("command").head,
      text = formdata.get("text").head,
      responseUrl = formdata.get("response_url").head,
      triggerId = formdata.get("trigger_id").head
    )
    userService.get(SlackUser.buildLoginInfo(payload.userId, payload.teamId)).flatMap {
      case Some(user) =>
        for {
          team <- teamService.get(SlackTeam.buildLoginInfo(user.team.id))
    } yield {
          if(team.isDefined) {
            slackAPIService.sendElectionDialog(payload, team.get).map(response => logger.debug(s"Slack response ${Json.toJson(response).toString()}"))
            Ok("")
          } else {
            NotFound("User requested team not found")
          }
        }
      case None => //TODO send message asking user to signup
      Future.successful(Ok(""))
    }
  }

}
