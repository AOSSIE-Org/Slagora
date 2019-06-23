package models

import java.text.SimpleDateFormat
import java.util.Date

import play.api.libs.json._
import com.mohiva.play.silhouette.api.LoginInfo
import org.joda.time.DateTime

import scala.util.{Failure, Success, Try}

/**
  * Election model which is created by user or guest
  *
  * @param id                  The name of the election
  * @param name                The name of the election
  * @param description         The short description about the election
  * @param creatorName         The name of the creator of the election
  * @param start               The start date of the election
  * @param end                 The end date of the election
  * @param realtimeResult      Specify whether show the results in real time or not
  * @param votingAlgo          The voting alogorithm for the election
  * @param candidates          The canditate list for the election
  * @param ballotVisibility    Specify  the ballot visibility level
  * @param voterListVisibility Specify  the voter list visibility level
  * @param createdTime         created time of election
  * @param ballot              ballot list of the election
  * @param voterList           voter list of the election
  * @param winners             winner list
  * @param isCounted           is the election is counted or not
  */
case class Election(
                     id: Option[String],
                     name: String,
                     description: String,
                     creatorName: String,
                     start: DateTime,
                     end: DateTime,
                     realtimeResult: Boolean,
                     votingAlgo: String,
                     candidates: List[String],
                     ballotVisibility: String,
                     voterListVisibility: Boolean,
                     createdTime: DateTime,
                     ballot: List[Ballot],
                     voterList: List[Voter],
                     winners: List[Winner],
                     isCounted: Boolean,
                     noVacancies: Int,
                     loginInfo: Option[LoginInfo]
                   )

object Election {

  implicit val jodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss'Z'")
  implicit val jodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss'Z'")

  implicit val loginInfoReader = Json.reads[LoginInfo]
  implicit val loginInfoWriter = Json.writes[LoginInfo]

  implicit object ElectionWrites extends OWrites[Election] {
    def writes(election: Election): JsObject = {
      (election.id, election.loginInfo) match {
        case (Some(id), Some(loginInfo)) =>
          Json.obj(
            "_id" -> id,
            "name" -> election.name,
            "description" -> election.description,
            "creatorName" -> election.creatorName,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> election.candidates,
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "createdTime" -> election.createdTime,
            "ballot" -> election.ballot,
            "voterList" -> election.voterList,
            "winners" -> election.winners,
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies,
            "loginInfo" -> Json.obj(
              "providerID" -> loginInfo.providerID,
              "providerKey" -> loginInfo.providerKey
            )
          )
        case (Some(id), None) =>
          Json.obj(
            "_id" -> id,
            "name" -> election.name,
            "description" -> election.description,
            "creatorName" -> election.creatorName,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> election.candidates,
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "createdTime" -> election.createdTime,
            "ballot" -> election.ballot,
            "voterList" -> election.voterList,
            "winners" -> election.winners,
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies
          )
        case (None, Some(loginInfo)) =>
          Json.obj(
            "name" -> election.name,
            "description" -> election.description,
            "creatorName" -> election.creatorName,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> election.candidates,
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "createdTime" -> election.createdTime,
            "ballot" -> election.ballot,
            "voterList" -> election.voterList,
            "winners" -> election.winners,
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies,
            "loginInfo" -> Json.obj(
              "providerID" -> loginInfo.providerID,
              "providerKey" -> loginInfo.providerKey
            )
          )
        case (None, None) =>
          Json.obj(
            "name" -> election.name,
            "description" -> election.description,
            "creatorName" -> election.creatorName,
            "start" -> election.start,
            "end" -> election.end,
            "realtimeResult" -> election.realtimeResult,
            "votingAlgo" -> election.votingAlgo,
            "candidates" -> election.candidates,
            "ballotVisibility" -> election.ballotVisibility,
            "voterListVisibility" -> election.voterListVisibility,
            "createdTime" -> election.createdTime,
            "ballot" -> election.ballot,
            "voterList" -> election.voterList,
            "winners" -> election.winners,
            "isCounted" -> election.isCounted,
            "noVacancies" -> election.noVacancies
          )
      }
    }
  }

  implicit object ElectionReads extends Reads[Election] {
    def reads(json: JsValue): JsResult[Election] = json match {
      case election: JsObject =>
        Try {
          val id = (election \ "_id" \ "$oid").asOpt[String]

          val name = (election \ "name").as[String]
          val description = (election \ "description").as[String]
          val creatorName = (election \ "creatorName").as[String]
          val start = (election \ "start").as[DateTime]
          val end = (election \ "end").as[DateTime]
          val realtimeResult = (election \ "realtimeResult").as[Boolean]
          val votingAlgo = (election \ "votingAlgo").as[String]
          val candidates = (election \ "candidates").as[List[String]]
          val ballotVisibility = (election \ "ballotVisibility").as[String]
          val voterListVisibility = (election \ "voterListVisibility").as[Boolean]
          val createdTime = (election \ "createdTime").as[DateTime]
          val ballot = (election \ "ballot").as[List[Ballot]]
          val voterList = (election \ "voterList").as[List[Voter]]
          val winners = (election \ "winners").as[List[Winner]]
          val isCounted = (election \ "isCounted").as[Boolean]
          val noVacancies = (election \ "noVacancies").as[Int]
          val loginInfo = (election \ "loginInfo").asOpt[LoginInfo]

          JsSuccess(
            new Election(
              id,
              name,
              description,
              creatorName,
              start,
              end,
              realtimeResult,
              votingAlgo,
              candidates,
              ballotVisibility,
              voterListVisibility,
              createdTime,
              ballot,
              voterList,
              winners,
              isCounted,
              noVacancies,
              loginInfo
            )
          )
        } match {
          case Success(value) => value
          case Failure(cause) => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

}
