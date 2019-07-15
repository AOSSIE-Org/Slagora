package formatters.json

import play.api.libs.json
import play.api.libs.json.Json

case class ElectionData(
                         name: String,
                         description: String,
                         candidates: List[String],
                         ballotVisibility: Boolean,
                         isRealTime: Boolean,
                         votingAlgo: String,
                         noVacancies: Int
                       )

object ElectionData {
  implicit val electionDataFormat: json.Format[ElectionData] = Json.format[ElectionData]
}