package models

import org.joda.time.DateTime
import play.api.libs.json
import play.api.libs.json.{JodaReads, JodaWrites, Json}

case class ElectionTimeData (
                              startingDate: DateTime,
                              endingDate: DateTime
                            )

object ElectionTimeData {
  implicit val jodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit  val electionTimeDataFormat: json.Format[ElectionTimeData] = Json.format[ElectionTimeData]
}
