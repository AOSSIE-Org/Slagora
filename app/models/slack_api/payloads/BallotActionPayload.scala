package models.slack_api.payloads

import models.slack_api.{SlackChannelReference, SlackTeamReference, SlackUserReference}
import models.slack_api.submissions.{BallotSubmission, DateSubmission}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class BallotActionPayload (
                                 actions: List[BallotSubmission],
                                 team: SlackTeamReference,
                                 user: SlackUserReference,
                                 channel: SlackChannelReference,
                                 token: String,
                                 response_url: String
                               )
object BallotActionPayload {
  implicit val format: json.Format[BallotActionPayload] = Json.format[BallotActionPayload]
  implicit val oFormat: OFormat[BallotActionPayload] = Json.format[BallotActionPayload]
}