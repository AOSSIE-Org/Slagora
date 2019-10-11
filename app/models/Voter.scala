package models

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json.Json
import play.api.libs.json

case class Voter(name: String,
                 voterId: LoginInfo
                )

object Voter {
  implicit val voterFormat: json.Format[Voter] = Json.format[Voter]
}
