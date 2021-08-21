package controllers

import java.util.UUID

import javax.inject.Inject
import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.util.Clock
import io.swagger.annotations.{Api, ApiImplicitParam, ApiImplicitParams, ApiOperation}
import models.{BallotData, Election, PartialElection}
import models.security.{SlackTeam, SlackTeamBuilder, SlackUser, SlackUserBuilder}
import models.slack_api.payloads._
import models.slack_api.{ActionIDs, Commands, DialogStates, SlashCommandPayLoad}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsError, Json}
import play.api.mvc._
import service._
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
                                   electionService: ElectionService,
                                   ballotDataService: BallotDataService,
                                   partialElectionService: PartialElectionService,
                                   clock: Clock,
                                   slackAPIService: SlackAPIService)
                                  (implicit ex: ExecutionContext) extends AbstractController(components) with I18nSupport with Logger {

  def receiveSlashCommandPayLoad(): Action[AnyContent] = Action.async { implicit request: Request[AnyContent] =>
    val formdata = request.body.asFormUrlEncoded
    val payload = SlashCommandPayLoad.createFromFormData(formdata)
    for {
      user <- userService.get(SlackUser.buildLoginInfo(payload.userId, payload.teamId))
      team <- teamService.get(SlackTeam.buildLoginInfo(payload.teamId))
    } yield {
      payload.text.toLowerCase match {
        case Commands.CREATE =>
          if(user.isDefined && team.isDefined) {
            slackAPIService.sendElectionDialog(payload, team.get)
            Ok("")
          } else if(team.isDefined) {
            slackAPIService.userSignUpOnError(payload.channelId, payload.userId, team.get)
            Ok("")
          } else {
            NotFound("User requested team not found.")
          }

        case Commands.POLLS | Commands.ELECTIONS =>
          if(user.isDefined && team.isDefined) {
            electionService.userElectionList(user.get.loginInfo).flatMap{ elections =>
              if(elections.nonEmpty)
                slackAPIService.sendElectionsListForView(elections, team.get, payload)
              else {
                slackAPIService.sendNoElectionFound(team.get, payload)
              }
            }
            Ok("")
          } else if(team.isDefined) {
            slackAPIService.userSignUpOnError(payload.channelId, payload.userId, team.get)
            Ok("")
          } else {
            NotFound("User requested team not found.")
          }

        case Commands.HELP =>
          if(user.isDefined && team.isDefined) {
            slackAPIService.sendHelpMsg(payload.channelId, payload.userId, team.get)
            Ok("")
          } else if(team.isDefined) {
            slackAPIService.userSignUpOnError(payload.channelId, payload.userId, team.get)
            Ok("")
          } else {
            NotFound("User requested team not found.")
          }

        case Commands.DELETE =>
          if(user.isDefined && team.isDefined) {
            electionService.userElectionList(user.get.loginInfo).flatMap{ elections =>
              if(elections.nonEmpty)
                slackAPIService.sendElectionsListForDelete(elections, team.get, payload)
              else {
                slackAPIService.sendNoElectionFound(team.get, payload)
              }
            }
            Ok("")
          } else if(team.isDefined) {
            slackAPIService.userSignUpOnError(payload.channelId, payload.userId, team.get)
            Ok("")
          } else {
            NotFound("User requested team not found.")
          }

        case _ => NotFound("Slash text command not found.")
      }
    }
  }


  def interactiveMsgPayload() = Action.async { implicit request =>
    val payload = Json.parse(request.body.asFormUrlEncoded.get("payload").head)
    logger.error(s"Slack response ${payload}")
    (payload \ "type").as[String] match {
      case "dialog_submission" =>
        payload.validate[NewElectionDialogPayload].map { data =>
          data.state match {
            case DialogStates.NEW_ELECTION =>
              val partialElection = PartialElection.buildFromElectionPayload(data.submission, clock.now, SlackUser.buildLoginInfo(data.user.id, data.team.id))
              partialElectionService.save(partialElection).flatMap {
                case true => //Send interactive message to user to proceed with date selection for the new election. This will complete the election creation process
                  for {
                    team <- teamService.get(SlackTeam.buildLoginInfo(data.team.id))
                  } yield {
                    if (team.isDefined) {
                      slackAPIService.sendDateMsg(data, team.get, partialElection).flatMap(response => Future.successful(logger.error(s"Slack message response ${response}")))
                      Ok("")
                    } else {
                      NotFound("User requested team not found")
                    }
                  }
                case false =>
                  Future.successful(Ok("Error unable to save data"))
              }
          }
        }.recoverTotal { error =>
          logger.error(s"Failed to validate with error: $error")
          Future.successful(BadRequest(Json.toJson("Bad Request")))
        }
      case "block_actions" =>
        val electionAndAction = (payload \ "actions" \ 0 \ "action_id").as[String].split(':').toList
        val tuple = (electionAndAction.head, electionAndAction.last)
        tuple._2 match {
          case ActionIDs.END_DATE | ActionIDs.START_DATE =>
            payload.validate[DateActionPayload].map { data =>
              for {
                partialElection <- partialElectionService.get(tuple._1)
              } yield {
                if (partialElection.isDefined) {
                  if(tuple._2 == ActionIDs.END_DATE) {
                    partialElectionService.update(partialElection.get.copy(end = data.actions.head.selected_date))
                  } else {
                    partialElectionService.update(partialElection.get.copy(start = data.actions.head.selected_date))
                  }
                  Ok("")
                } else {
                  NotFound(s"Partial election with ID: ${tuple._1}")
                }
              }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }

          case ActionIDs.FINISH_ELECTION_SETUP =>
            Future.successful(Ok(""))
            payload.validate[SimpleActionPayload].map { data =>
              for {
                partialElection <- partialElectionService.get(tuple._1)
                team <- teamService.get(SlackTeam.buildLoginInfo(data.team.id))
              } yield {
                if (partialElection.isDefined && team.isDefined) {
                  val maybeElection = Election.buildFromSlack(partialElection.get, data)
                  logger.error(s"Election: $maybeElection Team: $team Payload: $data")
                  if(maybeElection.isDefined) {
                    Ok("")
                    electionService.save(maybeElection.get).flatMap{election =>
                      slackAPIService.sendVoteInviteMsg(election, team.get).flatMap{ _ =>
                        slackAPIService.sendCompletedElectionMsg(election, data.response_url, team.get)
                          .flatMap { _ =>
                            partialElectionService.delete(partialElection.get.id)
                          }
                      }
                    }
                    partialElectionService.delete(tuple._1)
                  } else {
                    //TODO show errors specifying that there is an error with the dates since that the only data that can cause issues
                  }
                  Ok("")
                } else {
                  NotFound(s"Partial election with ID: ${tuple._1}")
                }
              }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
          case ActionIDs.TRY_VOTING =>
            Future.successful(Ok(""))
            payload.validate[SimpleActionPayload].map { data =>
              for {
                election <- electionService.get(tuple._1)
                user <- userService.get(SlackUser.buildLoginInfo(data.user.id, data.team.id))
                team <- teamService.get(SlackTeam.buildLoginInfo(data.team.id))
              } yield {
                if(user.isDefined && team.isDefined && election.isDefined) {
                  if (election.get.hasVoted(user.get.loginInfo)) {
                    slackAPIService.sendVotedAlreadyMsg(election.get, data, team.get)
                  } else {
                    val ballot = BallotData(java.util.UUID.randomUUID().toString, "", election.get.id.get, clock.now, user.get.loginInfo)
                    ballotDataService.save(ballot).flatMap { js =>
                      slackAPIService.sendVoteMsg(
                        election.get,
                        ballot,
                        team.get,
                        data
                      )
                    }
                  }
                } else if(user.isEmpty && team.isDefined) {
                  slackAPIService.userSignUpOnError(data.channel.id, data.user.id, team.get).flatMap(response => Future.successful(logger.error(s"Failed sent sign up msg error: $response")))
                } else {
                  Future.successful(BadRequest(Json.toJson("Bad Request")))
                }
              }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
            Future.successful(Ok(""))

          case ActionIDs.CANDIDATE_SELECTED =>
            Future.successful(Ok(""))
            payload.validate[BallotActionPayload].map { data =>
              for(
                ballot <- ballotDataService.get(tuple._1)
              ) yield {
                if(ballot.isDefined) {
                  ballotDataService.update(ballot.get.copy(ballotData = data.actions.head.selected_option.value))
                }
              }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
            Future.successful(Ok(""))

          case ActionIDs.VOTE =>
            Future.successful(Ok(""))
            payload.validate[SimpleActionPayload].map { data =>
              for {
                ballot <- ballotDataService.get(tuple._1)
                election <- electionService.get(ballot.get.election)
                team <- teamService.get(SlackTeam.buildLoginInfo(data.team.id))
                user <- userService.get(SlackUser.buildLoginInfo(data.user.id, data.team.id))
              } yield {
                if(ballot.isDefined && election.isDefined && team.isDefined) {
                  electionService.vote(ballot.get.election, ballot.get.getBallotInstance(user.get)).flatMap{_ =>
                    ballotDataService.delete(ballot.get.id)
                    slackAPIService.sendVoteCompletedMsgWithNoResults(election.get, data.response_url, team.get)
                  }
                } else {
                  Future.successful(BadRequest(Json.toJson("Bad Request")))
                }
              }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
            Future.successful(Ok(""))

          case ActionIDs.SIGN_UP =>
            Future.successful(Ok(""))
            payload.validate[SimpleActionPayload].map { data =>
              teamService.get(SlackTeam.buildLoginInfo(data.team.id)).flatMap{ team =>
                if(team.isDefined){
                  Future.successful(slackAPIService.deleteMsg(data.response_url, team.get))
                } else{
                  Future.successful(BadRequest(Json.toJson("Bad Request")))
                }
              }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
            Future.successful(Ok(""))

          case ActionIDs.RESULT => Future.successful(Ok(""))
            payload.validate[SimpleActionPayload].map { data =>
              for{
                team <- teamService.get(SlackTeam.buildLoginInfo(data.team.id))
                election <- electionService.get(tuple._1)
              } yield {
                  if(election.isDefined && team.isDefined) {
                    if(election.get.realtimeResult || election.get.isCounted || election.get.end.isBeforeNow) {
                      slackAPIService.sendResultsMsg(election.get, team.get, data)
                    } else {
                      slackAPIService.sendResultsNotAvailableMsg(election.get, data.response_url, team.get)
                    }
                  }
                }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
            Future.successful(Ok(""))

          case ActionIDs.DELETE => Future.successful(Ok(""))
            payload.validate[SimpleActionPayload].map { data =>
              for(team <- teamService.get(SlackTeam.buildLoginInfo(data.team.id)))
                yield {
                  if(team.isDefined)
                    slackAPIService.deleteMsg(data.response_url, team.get)
                }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
            Future.successful(Ok(""))
          case ActionIDs.ELECTION_SELECTED =>
            Future.successful(Ok(""))
            payload.validate[ElectionSelectedActionPayload].map { data =>
              for {
                election <- electionService.get(data.actions.head.selected_option.value)
                team <- teamService.get(SlackTeam.buildLoginInfo(data.team.id))
              } yield {
                if(election.isDefined && team.isDefined) {
                  slackAPIService.sendElectionDetails(election.get, team.get, data)
                }
              }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
            Future.successful(Ok(""))

          case ActionIDs.ELECTION_SELECTED_FOR_DELETE =>
            Future.successful(Ok(""))
            payload.validate[ElectionSelectedActionPayload].map { data =>
              for {
                election <- electionService.get(data.actions.head.selected_option.value)
                team <- teamService.get(SlackTeam.buildLoginInfo(data.team.id))
              } yield {
                if(election.isDefined && team.isDefined) {
                  electionService.delete(election.get.id.get).flatMap(_ => slackAPIService.sendElectionDetails(election.get, team.get, data))
                }
              }
            }.recoverTotal { error =>
              logger.error(s"Failed to validate with error: $error")
              Future.successful(BadRequest(Json.toJson("Bad Request")))
            }
            Future.successful(Ok(""))

          case _ => Future.successful(Ok(""))
        }
      case _ => Future.successful(Ok(""))
    }
  }
}
