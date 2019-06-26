package slack_api

import com.mohiva.play.silhouette.api.util.HTTPLayer
import com.mohiva.play.silhouette.impl.providers.OAuth2Settings
import javax.inject.Inject
import models.security.SlackTeam
import models.slack_api.SlashCommandPayLoad
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import utils.Logger

import scala.concurrent.{ExecutionContext, Future}

class SlackAPIProvider @Inject() (
                             httpLayer: HTTPLayer,
                             slackAPISettings:  SlackSettings,
                             messagesApi: MessagesApi
                           )(implicit ex: ExecutionContext) extends SlackAPIService with Logger{

  //Define all required methods here
  override def sendElectionDialog(payload: SlashCommandPayLoad, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.openDialog))
      .addHttpHeaders(
        "Content-type" -> "application/json",
      "Authorization" -> s"Bearer ${team.accessToken}")
      http.post(Json.parse(views.txt.slack.dialogs.electionform(payload.triggerId,"sadasd").body.trim))
      .flatMap { response =>
      Future.successful(response.json)
    }
  }
}
