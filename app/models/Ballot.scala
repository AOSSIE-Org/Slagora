package models

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class Ballot (
                    voterId: LoginInfo,
                    voterName: String,
                    voterImage: String,
                    ballotData: String
                  )

object Ballot {
  implicit val ballotFormat: json.Format[Ballot] = Json.format[Ballot]
  implicit val ballotOFormat: OFormat[Ballot] = Json.format[Ballot]
}