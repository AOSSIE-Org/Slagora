package models.slack_api.submissions

import play.api.libs.json
import play.api.libs.json.Json

case class ElectionSubmission (
                           action_id: String,
                           selected_option: SelectedOption
                         )
object ElectionSubmission {
  implicit val format: json.Format[ElectionSubmission] = Json.format[ElectionSubmission]
}
