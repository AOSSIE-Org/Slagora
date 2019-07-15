package models.slack_api

import play.api.libs.json
import play.api.libs.json.Json

case class SlashCommandPayLoad (
                               teamId: String,
                               teamName: String,
                               channelId: String,
                               channelName: String,
                               userId: String,
                               userName: String,
                               command: String,
                               text: String,
                               responseUrl: String,
                               triggerId: String
                               )

object SlashCommandPayLoad {
  implicit val format: json.Format[SlashCommandPayLoad] = Json.format[SlashCommandPayLoad]
}
