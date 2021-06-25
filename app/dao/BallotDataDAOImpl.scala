package dao

import com.mohiva.play.silhouette.api.util.Clock
import javax.inject.Inject
import models.BallotData
import org.joda.time.{DateTime, DateTimeZone}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.Cursor
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import service.BallotDataService

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.concurrent.{ExecutionContext, Future}

class BallotDataDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi, clock: Clock)(implicit ex: ExecutionContext) extends BallotDataService{
  private def ballotCollection = reactiveMongoApi.database.map(_.collection[JSONCollection]("ballot-data"))

  override def save(ballot: BallotData): Future[Boolean] = {
    val expiry: FiniteDuration = 5 minutes
    val b = ballot.copy(expiresOn = clock.now.withZone(DateTimeZone.UTC).plusSeconds(expiry.toSeconds.toInt))
    ballotCollection.flatMap(_.insert(b)).flatMap {
      data => Future.successful(data.ok)
    }
  }

  override def get(id: String): Future[Option[BallotData]] = {
    val query = Json.obj("id" -> id)
    ballotCollection.flatMap(_.find(query).one[BallotData])
  }

  override def delete(id: String): Future[Unit] = {
    val query = Json.obj("id" -> id)
    ballotCollection.flatMap(_.remove(query)).flatMap {
      _ => Future.successful(())
    }
  }

  def findExpired(dateTime: DateTime): Future[Seq[BallotData]] = {
    ballotCollection.flatMap(_.find(Json.obj()).cursor[BallotData]().collect[Seq](Int.MaxValue,Cursor.FailOnError[Seq[BallotData]]()))
      .flatMap(elections => Future.successful(elections.filter(e => e.expiresOn.isBefore(dateTime))))
  }

  override def update(ballot: BallotData): Future[Boolean] = {
    val query = Json.obj("id" -> ballot.id)
    val modifier = Json.obj("$set" -> Json.toJson(ballot))
    ballotCollection.flatMap(_.update(query, modifier)).flatMap(_ => Future.successful(true))
  }
}