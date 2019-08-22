package models

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.security.SlackUser
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.libs.json

case class BallotData(
                   id: String,
                   ballotData: String,
                   election: String,
                   expiresOn: DateTime,
                   voterId: LoginInfo
                 ) {
  def getBallotInstance (user: SlackUser): Ballot = {
    Ballot(voterId, user.name, user.image_24, ballotData)
  }
}

object BallotData {
  implicit val jodaDateReads: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val ballotFormat: json.Format[BallotData] = Json.format[BallotData]
  implicit val ballotOFormat: OFormat[BallotData] = Json.format[BallotData]

}