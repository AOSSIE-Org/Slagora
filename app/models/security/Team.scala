package models.security

import play.api.libs.json
import play.api.libs.json.Json

case class Team (
                id: String,
                name: String
                )
object Team {
  implicit val teamFormat: json.Format[Team] = Json.format[Team]
}