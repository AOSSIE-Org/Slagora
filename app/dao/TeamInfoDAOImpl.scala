package dao

import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import models.security.{SlackTeam, User}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import service.{TeamService, UserService}

import scala.concurrent.{ExecutionContext, Future}

class TeamInfoDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends TeamService{

  def teams = reactiveMongoApi.database.map(_.collection[JSONCollection]("team"))

  override def get(info: LoginInfo): Future[Option[SlackTeam]] = {
    val query = Json.obj("loginInfo" -> info)
    teams.flatMap(_.find(query).one[SlackTeam])
  }

  override def delete(info: LoginInfo): Future[Boolean] = {
    val query = Json.obj("loginInfo" -> info)
    teams.flatMap(_.remove(query))
      .flatMap{
      case result: WriteResult => Future.successful(result.ok)
      case _  => Future.successful(false)
    }
  }

  override def update(team: SlackTeam): Future[Boolean] = {
    val query = Json.obj("loginInfo" -> team.loginInfo)
    teams.flatMap(_.update(query,team))
      .flatMap{
        case result: WriteResult => Future.successful(result.ok)
        case _  => Future.successful(false)
      }
  }

  override def save(team: SlackTeam): Future[Boolean] = {
    val query = Json.obj("loginInfo" -> team.loginInfo)
    teams.flatMap(_.update(query,team,upsert = true))
      .flatMap{
        case result: WriteResult => Future.successful(result.ok)
        case _  => Future.successful(false)
      }
  }
}
