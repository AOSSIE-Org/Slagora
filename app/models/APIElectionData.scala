package models

import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class APIElectionData (
                        name: String,
                        createdBy: String,
                        algorithm: String,
                        isRealtime: Boolean,
                        channelID: String,
                        ballotVisibility: Boolean,
                        candidates: List[String],
                        noSeats: Int,
                        start: String,
                        end: String,
                        description: String
                      )

object APIElectionData {
  implicit val format: json.Format[APIElectionData] = Json.format[APIElectionData]
  implicit val oFormat: OFormat[APIElectionData] = Json.format[APIElectionData]
}