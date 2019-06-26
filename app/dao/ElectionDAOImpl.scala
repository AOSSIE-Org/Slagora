package dao

import com.mohiva.play.silhouette.api.LoginInfo
import javax.inject.Inject
import models.Election._
import models.{Ballot, Election, Voter, Winner}
import org.joda.time.DateTime
import play.api.libs.json._
import play.modules.reactivemongo._
import reactivemongo.api.ReadPreference
import play.modules.reactivemongo.json._
import reactivemongo.play.json.collection.JSONCollection
import service.ElectionService

import scala.collection.mutable.ListBuffer
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.Breaks._

class ElectionDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends ElectionService{
  /**
    * The data store for the elections.
    */
  private def electionsCollection = reactiveMongoApi.database.map(_.collection[JSONCollection]("election"))

  /**
    * Saves an Election.
    *
    * @return The saved Election.
    */
  override def save(election: Election): Future[Election] = {
    electionsCollection.flatMap(_.insert(election)).flatMap {
      _ => Future.successful(election)
    }
  }

  override def get(id: String): Future[Option[Election]] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    electionsCollection.flatMap(_.find(query).one[Election])
  }

  override def delete(id: String): Future[Unit] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    electionsCollection.flatMap(_.remove(query)).flatMap {
      _ => Future.successful(())
    }
  }

  override def userElectionList(loginInfo: LoginInfo): Future[List[Election]] = {
    val query = Json.obj("loginInfo" -> loginInfo)
    electionsCollection.flatMap(_.find(query).cursor[Election]().collect[List]())
  }

  override def getCandidates(id: String): Future[List[String]] = {
    get(id).flatMap {
      case Some(elect) => Future.successful(elect.candidates)
      case _ => Future.successful(List.empty[String])
    }
  }

  override def vote(id: String, ballot: Ballot): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    var ballotList = ListBuffer[Ballot]()
    ballotList += ballot
    getBallots(id).flatMap {
      result =>
        val ballots = ballotList.toList.:::(result)
        val modifier = Json.obj("$set" -> Json.obj("ballot" -> Json.toJson(ballots)))
        electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
    }
  }

  override def getBallots(id: String): Future[List[Ballot]] = {
    get(id).flatMap {
      case Some(elect) => Future.successful(elect.ballot)
      case _ => Future.successful(List.empty[Ballot])
    }
  }

  override def getVoterList(id: String): Future[List[Voter]] = {
    get(id).flatMap {
      case Some(elect) => Future.successful(elect.voterList)
      case _ => Future.successful(List.empty[Voter])
    }
  }

  private def isVoterInList(voter: Voter, list: List[Voter]): Boolean = {
    for (voterD <- list) {
      if(voterD.voterId.providerID == voter.voterId.providerID)
        return true
    }
    false
  }

  private def isVoterInBallot(voter: Voter, list: List[Ballot]): Boolean = {
    for (voterD <- list) {
      if (voterD.voterId.providerID == voter.voterId.providerID)
        return true
    }
    false
  }

  override def addVoter(id: String, voter: Voter): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    getVoterList(id).flatMap {
      result =>
        if (!isVoterInList(voter, result)) {
          getBallots(id).flatMap {
            ballotResult =>
              if (!isVoterInBallot(voter, ballotResult)) {
                val voterList = ListBuffer[Voter]()
                voterList += voter
                val voters = voterList.toList.:::(result)
                val modifier = Json.obj("$set" -> Json.obj("voterList" -> Json.toJson(voters)))
                electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
              }
              else {
                Future.successful(false)
              }
          }
        } else {
          Future.successful(false)
        }
    }
  }

  override def addVoters(id: String, voters: List[Voter]): Future[List[Voter]] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    getVoterList(id).flatMap {
      result =>
        val filteredList = voters.filter(voter => !isVoterInList(voter, result))
        val allVoters = filteredList.:::(result)
        val modifier = Json.obj("$set" -> Json.obj("voterList" -> Json.toJson(allVoters)))
        electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(filteredList))
    }
  }

  override def removeVoter(id: String, info: LoginInfo): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> id))
    getVoterList(id).flatMap {
      result =>
        val voters = result.filter(v => v.voterId.providerID != info.providerID)
        val modifier = Json.obj("$set" -> Json.obj("voterList" -> Json.toJson(voters)))
        electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
    }
  }


  override def getStartDate(id: String): Future[DateTime] = {
    get(id).flatMap {
      case Some(elect) => Future.successful(elect.start)
    }
  }

  override def getEndDate(id: String): Future[DateTime] = {
    get(id).flatMap {
      case Some(elect) => Future.successful(elect.end)
    }
  }

  override def getBallotVisibility(id: String): Future[Option[Boolean]] = {
    get(id).flatMap {
      case Some(result) => Future.successful(Some(result.ballotVisibility))
      case _ => Future.successful(None)
    }
  }

  override def update(election: Election): Future[Boolean] = {
    val query = Json.obj("_id" -> Json.obj("$oid" -> election.id.get))
    val modifier = Json.obj("$set" -> Json.toJson(election.copy(id = None)))
    electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
  }

  override def getWinners(id: String): Future[List[Winner]] = {
    get(id).flatMap {
      case Some(result) => Future.successful(result.winners)
      case _ => Future.successful(List.empty[Winner])
    }
  }

  override def updateWinner(result: List[Winner], id: String) = {
    electionsCollection.flatMap(_.update(Json.obj("_id" -> Json.obj("$oid" -> id)),
      Json.obj("$set" -> Json.obj("winners" -> Json.toJson(result)))))
  }

  override def getCreatorName(id: String): Future[String] = {
    get(id).flatMap {
      case Some(elect) => Future.successful(elect.name)
      case _ => Future.successful("")
    }
  }

}
