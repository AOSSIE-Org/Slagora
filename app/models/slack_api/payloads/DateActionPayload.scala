package models.slack_api.payloads

import models.slack_api.{SlackChannelReference, SlackTeamReference, SlackUserReference}
import models.slack_api.submissions.{DateSubmission, ElectionDialogSubmission}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class DateActionPayload (
                               actions: List[DateSubmission],
                               team: SlackTeamReference,
                               user: SlackUserReference,
                               channel: SlackChannelReference,
                               token: String,
                               response_url: String
                             )
object DateActionPayload {
  implicit val format: json.Format[DateActionPayload] = Json.format[DateActionPayload]
  implicit val oFormat: OFormat[DateActionPayload] = Json.format[DateActionPayload]
}