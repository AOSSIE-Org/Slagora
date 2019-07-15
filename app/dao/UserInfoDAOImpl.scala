package dao

import javax.inject.Inject
import com.mohiva.play.silhouette.api.LoginInfo
import models.security.{SlackUser}
import play.api.libs.json.Json
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.commands.WriteResult
import reactivemongo.play.json._
import reactivemongo.play.json.collection._
import service.UserService

import scala.concurrent.{ExecutionContext, Future}

class UserInfoDAOImpl @Inject()(val reactiveMongoApi: ReactiveMongoApi)(implicit ex: ExecutionContext) extends UserService{

  def users = reactiveMongoApi.database.map(_.collection[JSONCollection]("user"))

  override def get(info: LoginInfo): Future[Option[SlackUser]] = {
    val query = Json.obj("loginInfo" -> info)
    users.flatMap(_.find(query).one[SlackUser])
  }

  override def delete(info: LoginInfo): Future[Boolean] = {
    val query = Json.obj("loginInfo" -> info)
    users.flatMap(_.remove(query))
      .flatMap{
        case result: WriteResult => Future.successful(result.ok)
        case _  => Future.successful(false)
      }
  }

  override def update(user: SlackUser): Future[Boolean] = {
    val query = Json.obj("loginInfo" -> user.loginInfo)
    users.flatMap(_.update(query,user))
      .flatMap{
        case result: WriteResult => Future.successful(result.ok)
        case _  => Future.successful(false)
      }
  }

  override def save(user: SlackUser): Future[Boolean] = {
    val query = Json.obj("loginInfo" -> user.loginInfo)
    users.flatMap(_.update(query,user,upsert = true))
      .flatMap{
        case result: WriteResult => Future.successful(result.ok)
        case _  => Future.successful(false)
      }
  }

  override def retrieve(loginInfo: LoginInfo): Future[Option[SlackUser]] = {
    val query = Json.obj("loginInfo" -> loginInfo)
    users.flatMap(_.find(query).one[SlackUser])
  }
}
