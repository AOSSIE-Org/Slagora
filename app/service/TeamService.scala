package service

import com.mohiva.play.silhouette.api.LoginInfo
import models.security.{SlackTeam => Team}

import scala.concurrent.Future

trait TeamService {

  def get(info: LoginInfo): Future[Option[Team]]

  def delete(info: LoginInfo): Future[Boolean]

  def update(team: Team): Future[Boolean]

  def save(team: Team): Future[Boolean]
}
