package models.security

import play.api.libs.json
import play.api.libs.json.Json

case class Bot (
                 bot_user_id: String,
                 bot_access_token: String
               )
object Bot {
  implicit val botFormat: json.Format[Bot] = Json.format[Bot]
}