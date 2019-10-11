package models

import agora.model.Candidate
import play.api.libs.json.Json
import play.api.libs.json

case class Winner(
                   candidate: Candidate,
                   score: Score
                 )

object Winner {
  implicit lazy val candidateFormat: json.Format[Candidate] = Json.format[Candidate]

  implicit lazy val winnerFormat: json.Format[Winner] = Json.format[Winner]
}
