package utils.auth

import com.mohiva.play.silhouette.api.Env
import com.mohiva.play.silhouette.impl.authenticators.{CookieAuthenticator, JWTAuthenticator}
import models.security.{SlackUser, User}

trait DefaultEnv extends Env {

  type I = SlackUser
  type A = JWTAuthenticator
}
