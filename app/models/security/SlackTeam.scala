package models.security

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.{OAuth2Info, SocialProfile, SocialProfileBuilder}
import play.api.libs.json
import play.api.libs.json.{Json, OFormat}
import slack_auth.SlackTeamProvider.ID

case class SlackTeam(
                      loginInfo: LoginInfo,
                      accessToken: String,
                      scope: String,
                      name: String,
                      id: String,
                      webHook: WebHook,
                      bot: Bot
                    ) extends SocialProfile

object SlackTeam {
  implicit val slackTeamJsonFormat: json.Format[SlackTeam] = Json.format[SlackTeam]
  implicit val slackTeamObjectFormat: OFormat[SlackTeam] = Json.format[SlackTeam]

  def buildLoginInfo(teamId: String): LoginInfo = LoginInfo(ID, s"team_id=${teamId}")

}


trait SlackTeamBuilder {
  self: SocialProfileBuilder =>

  /**
    * The type of the profile a profile builder is responsible for.
    */
  type Profile = SlackTeam
}