package models.slack_api

import play.api.libs.json
import play.api.libs.json.Json

case class SlackTeamReference (
                              id: String,
                              domain: String
                              )

object SlackTeamReference {
  implicit  val format: json.Format[SlackTeamReference] = Json.format[SlackTeamReference]
}