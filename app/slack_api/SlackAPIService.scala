package slack_api

import models.security.SlackTeam
import models.slack_api.SlashCommandPayLoad
import play.api.libs.json.JsValue

import scala.concurrent.Future

trait SlackAPIService {

  def sendElectionDialog(payload: SlashCommandPayLoad, team: SlackTeam): Future[JsValue]

}
