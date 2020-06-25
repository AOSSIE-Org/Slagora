package dao


import com.mohiva.play.silhouette.api.util.Clock
import javax.inject.Inject
import models.PartialElection
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.play.json.collection.JSONCollection
import service.PartialElectionService
import reactivemongo.play.json._
import reactivemongo.play.json.collection._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}

class PartialElectionDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi, clock: Clock)(implicit ex: ExecutionContext) extends PartialElectionService{
  private def electionsCollection = reactiveMongoApi.database.map(_.collection[JSONCollection]("partial-elections"))

  override def save(election: PartialElection): Future[Boolean] = {
    val expiry: FiniteDuration = 5 minutes
    val partialElection = election.copy(expiresOn = clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt))
    electionsCollection.flatMap(_.insert(partialElection)).flatMap {
      data => Future.successful(data.ok)
    }
  }

  override def get(id: String): Future[Option[PartialElection]] = {
    val query = Json.obj("id" -> id)
    electionsCollection.flatMap(_.find(query).one[PartialElection])
  }

  override def delete(id: String): Future[Unit] = {
    val query = Json.obj("id" -> id)
    electionsCollection.flatMap(_.remove(query)).flatMap {
      _ => Future.successful(())
    }
  }

  def findExpired(dateTime: DateTime): Future[Seq[PartialElection]] = {
    electionsCollection.flatMap(_.find(Json.obj()).cursor[PartialElection]().collect[Seq](Int.MaxValue,Cursor.FailOnError[Seq[PartialElection]]()))
      .flatMap(elections => Future.successful(elections.filter(e => e.expiresOn.isBefore(dateTime))))
  }

  override def update(partialElection: PartialElection): Future[Boolean] = {
      val query = Json.obj("id" -> partialElection.id)
      val modifier = Json.obj("$set" -> Json.toJson(partialElection))
      electionsCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
  }
}
