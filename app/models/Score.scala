package models

import play.api.libs.json
import play.api.libs.json.Json

case class Score(
                  numerator: Int,
                  denominator: Int
                )

object Score {
  implicit val scoreFormat: json.Format[Score] = Json.format[Score]
}