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

  def createFromFormData(formdata: Option[Map[String, Seq[String]]]): SlashCommandPayLoad = {
    new SlashCommandPayLoad(
      teamId = formdata.get("team_id").head,
      teamName = formdata.get("team_domain").head,
      channelId = formdata.get("channel_id").head,
      channelName = formdata.get("channel_name").head,
      userId = formdata.get("user_id").head,
      userName = formdata.get("user_name").head,
      command = formdata.get("command").head,
      text = formdata.get("text").head,
      responseUrl = formdata.get("response_url").head,
      triggerId = formdata.get("trigger_id").head
    )
  }
}
