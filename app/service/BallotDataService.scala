package service

import models.BallotData
import org.joda.time.DateTime

import scala.concurrent.Future

trait BallotDataService {
  def save(ballot: BallotData): Future[Boolean]

  def get(id: String): Future[Option[BallotData]]

  def delete(id: String) : Future[Unit]

  def update(ballot: BallotData): Future[Boolean]

  def findExpired(dateTime: DateTime): Future[Seq[BallotData]]

}
