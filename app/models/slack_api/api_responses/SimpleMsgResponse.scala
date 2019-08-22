package models.slack_api.api_responses

import play.api.libs.json
import play.api.libs.json.Json

case class SimpleMsgResponse (
                             ok: String,
                             channel: String,
                             ts: String
                             )

object SimpleMsgResponse {
  implicit val format: json.Format[SimpleMsgResponse] = Json.format[SimpleMsgResponse]
}