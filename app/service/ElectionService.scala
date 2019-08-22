package service

import com.mohiva.play.silhouette.api.LoginInfo
import models._
import org.joda.time.DateTime

import scala.concurrent.Future

trait ElectionService {

  def save(election: Election): Future[Election]

  def get(id: String): Future[Option[Election]]

  def delete(id: String) : Future[Unit]

  def userElectionList(loginInfo : LoginInfo): Future[List[Election]]

  def getCandidates(id: String): Future[List[String]]

  def vote(id: String, ballot: Ballot): Future[Boolean]

  def getBallots(id: String): Future[List[Ballot]]

  def getVoterList(id: String): Future[List[Voter]]

  def addVoter(id: String , voter : Voter ): Future[Boolean]

  def addVoters(id: String, voters: List[Voter]) : Future[List[Voter]]

  def getCreatorName(id: String): Future[String]

  def removeVoter(id : String, info: LoginInfo ): Future[Boolean]

  def getStartDate(id: String): Future[DateTime]

  def getEndDate(id: String): Future[DateTime]

  def getBallotVisibility(id : String) : Future[Option[Boolean]]

  def update(election : Election) : Future[Boolean]

  def getWinners(id : String) : Future[List[Winner]]

  def updateWinner(result : List[Winner], id : String)

}
