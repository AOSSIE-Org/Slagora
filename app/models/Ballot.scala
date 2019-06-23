package models

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json.Json
import play.api.libs.json

case class Ballot(
                   voteBallot: String,
                   voterId: LoginInfo
                 )

object Ballot {
  implicit val ballotFormat: json.Format[Ballot] = Json.format[Ballot]

}