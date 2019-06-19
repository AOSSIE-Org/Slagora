package models.security

import play.api.libs.json
import play.api.libs.json.Json

case class WebHook (
                   url: String,
                   channel: String,
                   configuration_url: String
                   )

object WebHook {
  implicit val webHookFormat: json.Format[WebHook] = Json.format[WebHook]
}