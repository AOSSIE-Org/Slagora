package models.slack_api

import play.api.libs.json
import play.api.libs.json.Json

case class SlackChannelReference (
                                 id: String,
                                 name: String
                                 )

object SlackChannelReference {
  implicit  val format: json.Format[SlackChannelReference] = Json.format[SlackChannelReference]
}