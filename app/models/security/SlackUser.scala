package models.security

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}
import com.mohiva.play.silhouette.impl.providers.{SocialProfile, SocialProfileBuilder}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}

case class SlackUser(
                      loginInfo: LoginInfo,
                      name: String,
                      id: String,
                      email: String,
                      image_24: String,
                      image_32: String,
                      image_48: String,
                      image_72: String,
                      image_192: String,
                      image_512: String,
                      team: Team
                    ) extends SocialProfile with Identity

object SlackUser {
  implicit val slackUserJsonFormat: json.Format[SlackUser] = Json.format[SlackUser]
  implicit val slackUserObjectFormat: OFormat[SlackUser] = Json.format[SlackUser]
}

trait SlackUserBuilder {
  self: SocialProfileBuilder =>

  /**
    * The type of the profile a profile builder is responsible for.
    */
  type Profile = SlackUser
}