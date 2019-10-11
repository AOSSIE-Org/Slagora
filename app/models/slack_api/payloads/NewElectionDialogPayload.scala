package models.slack_api.payloads

import models.slack_api.submissions.ElectionDialogSubmission
import models.slack_api.{SlackChannelReference, SlackTeamReference, SlackUserReference}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class NewElectionDialogPayload(
                                     callback_id: String,
                                     submission: ElectionDialogSubmission,
                                     state: String,
                                     team: SlackTeamReference,
                                     user: SlackUserReference,
                                     channel: SlackChannelReference,
                                     action_ts: String,
                                     token: String,
                                     response_url: String
                                   )

object NewElectionDialogPayload {
  implicit val format: json.Format[NewElectionDialogPayload] = Json.format[NewElectionDialogPayload]
  implicit val oFormat: OFormat[NewElectionDialogPayload] = Json.format[NewElectionDialogPayload]
}
