package controllers

import java.util.UUID
import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.util.Clock
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.{APIElectionData, BallotData, Election, PartialElection}
import models.security.{SlackTeam, SlackTeamBuilder, SlackUser, SlackUserBuilder}
import models.slack_api.payloads._
import models.slack_api.{ActionIDs, Commands, DialogStates, SlashCommandPayLoad}
import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import service._
import slack_api.SlackAPIService
import utils.auth.DefaultEnv
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}

@Api(value = "API")
class ApiController @Inject()(messagesApi: MessagesApi,
                                   components: ControllerComponents,
                                   silhouette: Silhouette[DefaultEnv],
                                   teamService: TeamService,
                                   electionService: ElectionService,
                                   clock: Clock,
                                   slackAPIService: SlackAPIService)
                                  (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport with Logger {
  def createElection() = silhouette.SecuredAction.async(parse.json) { implicit request =>
    request.body.validate[APIElectionData].map { data =>
      val maybeElection = Election.buildFromAPI(data, request.authenticator.loginInfo)
      //      logger.error(s"Election: $maybeElection Team: $team Payload: $data")
      if (maybeElection.isDefined) {
        electionService.save(maybeElection.get)
          .flatMap(election => teamService.get(request.authenticator.loginInfo)
          .flatMap(team =>slackAPIService.sendVoteInviteMsg(election, team.get)
            .flatMap ( _ => Future.successful(Ok))))
      }
      else {
        Future.successful(BadRequest)
      }
    }.recoverTotal {
      _ => Future.successful(BadRequest)
    }
  }

  def deleteElection(electionId: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    electionService.get(electionId).flatMap{
      case Some(election) if election.loginInfo.get.providerID == request.authenticator.loginInfo.providerID =>
        electionService.delete(electionId).flatMap(_ => Future.successful(Ok))
      case _ => Future.successful(BadRequest)
    }
  }

  def getElections() = silhouette.SecuredAction.async(parse.json) { implicit request =>
    electionService.userElectionList(request.authenticator.loginInfo).flatMap(elections => Future.successful(Ok(Json.toJson(elections))))
  }

  def getElection(electionId: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    electionService.get(electionId).flatMap{
      case Some(election) => Future.successful(Ok(Json.toJson(election)))
      case _ => Future.successful(NotFound)
    }
  }

  def sendReminder(electionId: String) = silhouette.SecuredAction.async(parse.json) { implicit request =>
    electionService.get(electionId).flatMap{
      case Some(election) if election.end.toDateTimeISO.isAfter(DateTime.now(DateTimeZone.UTC)) =>
        teamService.get(request.authenticator.loginInfo)
          .flatMap(team => slackAPIService.sendVoteInviteMsg(election, team.get)
            .flatMap ( _ => Future.successful(Ok)))
      case _ => Future.successful(Forbidden)
    }
  }
}