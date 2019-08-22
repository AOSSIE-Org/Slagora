package models.slack_api.submissions

import play.api.libs.json
import play.api.libs.json.Json


case class ElectionDialogSubmission (
                                    name: String,
                                    algorithm: String,
                                    isRealtime: String,
                                    ballotVisibility: String,
                                    candidates: String,
                                    noSeats: String,
                                    description: String
                                    )

object ElectionDialogSubmission {
  implicit  val format: json.Format[ElectionDialogSubmission] = Json.format[ElectionDialogSubmission]
}

