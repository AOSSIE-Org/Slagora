package service

import models.PartialElection
import org.joda.time.DateTime

import scala.concurrent.Future

trait PartialElectionService {

  def save(election: PartialElection): Future[Boolean]

  def get(id: String): Future[Option[PartialElection]]

  def delete(id: String) : Future[Unit]

  def update(partialElection: PartialElection): Future[Boolean]

  def findExpired(dateTime: DateTime): Future[Seq[PartialElection]]
}
