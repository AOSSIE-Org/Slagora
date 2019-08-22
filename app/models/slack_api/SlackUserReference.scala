package models.slack_api

import play.api.libs.json
import play.api.libs.json.Json

case class SlackUserReference (
                              id: String,
                              name: String
                              )

object SlackUserReference {
  implicit  val format: json.Format[SlackUserReference] = Json.format[SlackUserReference]

}