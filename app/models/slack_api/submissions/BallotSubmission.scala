package models.slack_api.submissions

import play.api.libs.json
import play.api.libs.json.Json

case class BallotSubmission(
                             action_id: String,
                             selected_option: SelectedOption
                           )
object BallotSubmission {
  implicit val format: json.Format[BallotSubmission] = Json.format[BallotSubmission]
}

case class SelectedOption(value: String)
object SelectedOption {
   implicit val format: json.Format[SelectedOption] = Json.format[SelectedOption]
 }