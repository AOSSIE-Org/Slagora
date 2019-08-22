package models.slack_api.submissions

import play.api.libs.json
import play.api.libs.json.Json

case class DateSubmission (
                          action_id: String,
                          selected_date: String,
                          initial_date: String
                          )
object DateSubmission {
  implicit  val format: json.Format[DateSubmission] = Json.format[DateSubmission]
}