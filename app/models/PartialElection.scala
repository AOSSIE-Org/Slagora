package models

import com.mohiva.play.silhouette.api.LoginInfo
import models.slack_api.payloads.NewElectionDialogPayload
import models.slack_api.submissions.ElectionDialogSubmission
import org.joda.time.{DateTime, LocalDate}
import play.api.libs.json
import play.api.libs.json._

case class PartialElection(
                            id: String,
                            name: String,
                            algorithm: String,
                            isRealtime: Boolean,
                            ballotVisibility: Boolean,
                            candidates: List[String],
                            noSeats: Int,
                            start: String,
                            end: String,
                            description: String,
                            expiresOn: DateTime,
                            loginInfo: LoginInfo,
                          )

object PartialElection {
  implicit val jodaDateReads: Reads[DateTime] = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites: Writes[DateTime] = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val format: json.Format[PartialElection] = Json.format[PartialElection]
  implicit val oFormat: OFormat[PartialElection] = Json.format[PartialElection]

  def buildFromElectionPayload(data: ElectionDialogSubmission, expiresOn: DateTime, loginInfo: LoginInfo) = {
    PartialElection(java.util.UUID.randomUUID().toString, data.name, data.algorithm, data.isRealtime.toBoolean, data.ballotVisibility.toBoolean, data.candidates.split(',').toList, data.noSeats.toInt, LocalDate.now().toString("yyyy-MM-dd"), LocalDate.now().plusDays(1).toString("yyyy-MM-dd"), data.description, expiresOn, loginInfo)
  }
}
