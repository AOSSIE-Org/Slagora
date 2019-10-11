package service

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.security.{SlackUser, Team, User}
import reactivemongo.api.commands.WriteResult

import scala.concurrent.Future

trait UserService extends IdentityService[SlackUser] {

  def get(info: LoginInfo): Future[Option[SlackUser]]

  def delete(info: LoginInfo): Future[Boolean]

  def update(user: SlackUser): Future[Boolean]

  def save(user: SlackUser): Future[Boolean]

  def retrieve(loginInfo: LoginInfo): Future[Option[SlackUser]]
}
