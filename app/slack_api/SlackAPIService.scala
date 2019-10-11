package slack_api

import models.{BallotData, Election, PartialElection}
import models.security.{SlackTeam, WebHook}
import models.slack_api.SlashCommandPayLoad
import models.slack_api.payloads.{ElectionSelectedActionPayload, NewElectionDialogPayload, SimpleActionPayload}
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait SlackAPIService {

  def sendElectionDialog(payload: SlashCommandPayLoad, team: SlackTeam): Future[JsValue]
  def sendDateMsg(payload: NewElectionDialogPayload, team: SlackTeam, partialElection: PartialElection): Future[JsValue]
  def sendCompletedElectionMsg(election: Election, response_url: String, team: SlackTeam): Future[JsValue]
  def sendVoteInviteMsg(election: Election, team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue]
  def deleteMsg(responseUrl: String, team: SlackTeam): Future[JsValue]
  def userSignUpOnError(channel: String, user: String, team: SlackTeam): Future[JsValue]
  def userSignUp(webHook: WebHook, team: SlackTeam): Future[JsValue]
  def sendVoteMsg(election: Election, ballot: BallotData, team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue]
  def sendVoteCompletedMsgWithNoResults(election: Election, response_url: String, team: SlackTeam): Future[JsValue]
  def sendVotedAlreadyMsg(election: Election, simpleActionPayload: SimpleActionPayload, team: SlackTeam): Future[JsValue]
  def sendResultsNotAvailableMsg(election: Election, response_url: String, team: SlackTeam): Future[JsValue]
  def sendResultsMsg(election: Election, team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue]
  def sendElectionsListForView(elections: List[Election], team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue]
  def sendElectionsListForView(elections: List[Election], team: SlackTeam, payLoad: SlashCommandPayLoad): Future[JsValue]
  def sendElectionDetails(election: Election, team: SlackTeam, payload: ElectionSelectedActionPayload): Future[JsValue]
  def sendNoElectionFound(team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue]
  def sendNoElectionFound(team: SlackTeam, payLoad: SlashCommandPayLoad): Future[JsValue]
  def sendResultsMsg(election: Election, team: SlackTeam): Future[JsValue]

}
