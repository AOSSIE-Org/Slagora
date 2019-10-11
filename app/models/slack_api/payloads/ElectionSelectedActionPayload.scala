package models.slack_api.payloads

import models.slack_api.{SlackChannelReference, SlackTeamReference, SlackUserReference}
import models.slack_api.submissions.{BallotSubmission, ElectionSubmission}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class ElectionSelectedActionPayload (
                                           actions: List[ElectionSubmission],
                                           team: SlackTeamReference,
                                           user: SlackUserReference,
                                           channel: SlackChannelReference,
                                           token: String,
                                           response_url: String
                                         )
object ElectionSelectedActionPayload {
  implicit val format: json.Format[ElectionSelectedActionPayload] = Json.format[ElectionSelectedActionPayload]
  implicit val oFormat: OFormat[ElectionSelectedActionPayload] = Json.format[ElectionSelectedActionPayload]
}