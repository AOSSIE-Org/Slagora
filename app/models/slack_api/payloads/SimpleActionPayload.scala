package models.slack_api.payloads

import models.slack_api.{SlackChannelReference, SlackTeamReference, SlackUserReference}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class SimpleActionPayload (
                                 team: SlackTeamReference,
                                 user: SlackUserReference,
                                 channel: SlackChannelReference,
                                 token: String,
                                 response_url: String
                               )
object SimpleActionPayload {
  implicit val format: json.Format[SimpleActionPayload] = Json.format[SimpleActionPayload]
  implicit val oFormat: OFormat[SimpleActionPayload] = Json.format[SimpleActionPayload]
}
