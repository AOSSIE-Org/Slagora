package slack_api

import agora.model.Candidate
import com.mohiva.play.silhouette.api.util.HTTPLayer
import javax.inject.Inject
import models._
import models.security.{SlackTeam, WebHook}
import models.slack_api.payloads.{ElectionSelectedActionPayload, NewElectionDialogPayload, SimpleActionPayload}
import models.slack_api.{DialogStates, SlashCommandPayLoad}
import org.joda.time.{DateTimeZone, LocalDate}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import service.CountVotes
import spire.math.Rational
import utils.Logger

import scala.collection.mutable
import scala.concurrent.{ExecutionContext, Future}

class SlackAPIProvider @Inject()(
                                  httpLayer: HTTPLayer,
                                  slackAPISettings: SlackSettings,
                                  messagesApi: MessagesApi
                                )(implicit ex: ExecutionContext) extends SlackAPIService with Logger {

  //Define all required methods here
  override def sendElectionDialog(payload: SlashCommandPayLoad, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.openDialog))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.accessToken}")
    http.post(Json.parse(views.txt.slack.dialogs.electionform(payload.triggerId, DialogStates.NEW_ELECTION).body.trim))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendDateMsg(payload: NewElectionDialogPayload, team: SlackTeam, partialElection: PartialElection) = {

    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    //    logger.error(s"${LocalDate.now().toString("yyyy-MM-dd")}")
    http.post(Json.parse(views.txt.slack.dialogs.dateform(partialElection.id, partialElection.name, LocalDate.now().toString("yyyy-MM-dd"), LocalDate.now().plusDays(1).toString("yyyy-MM-dd"), payload.user.id, payload.channel.id).body.trim))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendCompletedElectionMsg(election: Election, response_url: String, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(response_url)
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.completedelection(election.name, election.start.toString("yyyy-MM-dd HH:mm:ss"), election.end.toString("yyyy-MM-dd HH:mm:ss"), election.votingAlgo, election.description, s"@${election.creatorName}").body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendVoteInviteMsg(election: Election, team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue] = {
    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendMsg))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.callforvote(election.id.get, election.name, election.start, election.end, election.votingAlgo, election.description, s"@${simpleActionPayload.user.name}", simpleActionPayload.channel.id).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def deleteMsg(responseUrl: String, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(responseUrl)
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.obj("delete_original" -> "true"))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def userSignUpOnError(channel: String, user: String, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.usermissensignup(channel, user).body.trim))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def userSignUp(webHook: WebHook, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(webHook.url)
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.usersignup().body.trim))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendVoteMsg(election: Election, ballot: BallotData, team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue] = {

    //Dirty hack for dynamic template content. Creating this in the template seems to fail
    //This should be moved into the template see views.txt.slack.messages.vote template
    def createCandidatesOptionsString(candidates: List[String], id: String): String = {
      var options = ""
      for (candidate <- candidates) {
        if (candidate != candidates.last) {
          options = options +
            s"""{
               |    "text": {
               |        "type": "plain_text",
               |        "text": "$candidate",
               |        "emoji": true
               |    },
               |    "value": "$candidate"
               |},
                    """.stripMargin
        } else {
          options = options +
            s"""{
               |    "text": {
               |        "type": "plain_text",
               |        "text": "$candidate",
               |        "emoji": true
               |    },
               |    "value": "$candidate"
               |}
              """.stripMargin
        }
      }
      options
    }

    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.vote(ballot.id, election.name, election.description, s"@${election.creatorName}", simpleActionPayload.user.id, simpleActionPayload.channel.id, createCandidatesOptionsString(election.candidates, election.id.get)).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendVoteCompletedMsgWithNoResults(election: Election, response_url: String, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(response_url)
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.simplevotedmsg(election.id.get).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendVotedAlreadyMsg(election: Election, simpleActionPayload: SimpleActionPayload, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.votedalready(election.id.get, election.name, simpleActionPayload.user.id, simpleActionPayload.channel.id).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendResultsNotAvailableMsg(election: Election, response_url: String, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(response_url)
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.resultsnotready(election.id.get, election.name, election.end.toString("yyyy-MM-dd HH:mm:ss")).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendResultsMsg(election: Election, team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue] = {
    def createCandidateVotersString(candidate: String, ballots: List[Ballot]): String = {
      var string =
        s"""{
           |		"type": "context",
           |		"elements": [
           |			{
           |				"type": "mrkdwn",
           |				"text": "*Candidate:* $candidate"
           |			}
           |		]
           |},
           |{
           |		"type": "context",
           |		"elements": [
        """.stripMargin

      if(ballots.isEmpty) {
        string = string +
                 """{
                   |				"type": "plain_text",
                   |				"emoji": true,
                   |				"text": "No votes"
                   |			}
                   |   ]
                   |	},
                 """.stripMargin
      } else {
        for (ballot <- ballots) {
          if (ballot != ballots.last) {

            string = string +
              s"""{
                 |				"type": "image",
                 |				"image_url": "${ballot.voterImage}",
                 |				"alt_text": "@${ballot.voterName}"
                 |			},
                  """.stripMargin
          } else {
            string = string +
              s"""{
                 |				"type": "image",
                 |				"image_url": "${ballot.voterImage}",
                 |				"alt_text": "@${ballot.voterName}"
                 |			},
                 |			{
                 |				"type": "plain_text",
                 |				"emoji": true,
                 |				"text": "${ballots.size} votes"
                 |			}
                 |   ]
                 |	},
                  """.stripMargin
          }
        }
      }
      string
    }

    def formatWinners(winners: List[(Candidate, Rational)]): String = {
      if (winners.isEmpty) {
        "No winner(s)"
      } else {
        var winnersString = winners.head._1.name
        for (winner <- winners.tail) {
          winnersString = winnersString + s", ${winner._1.name}"
        }
        winnersString
      }
    }

    val winners = CountVotes.countVotesMethod(election.ballot, election.votingAlgo, election.candidates, election.noVacancies)
    var s = ""
    for (candidate <- election.candidates) {
      val bs = election.ballot.filter(b => b.ballotData.equalsIgnoreCase(candidate))
      logger.error(s"\nBallots $bs")
      s = s +
        s"""{
           |		"type": "divider"
           |	},
          """.stripMargin + createCandidateVotersString(candidate, bs)
    }

    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.resultswithpublicballots(simpleActionPayload.channel.id, simpleActionPayload.user.id, election.name, election.start.toString("yyyy-MM-dd HH:mm:ss"), election.end.toString("yyyy-MM-dd HH:mm:ss"), election.votingAlgo, s, formatWinners(winners)).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendElectionsListForView(elections: List[Election], team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue] = {

    var optionsString = ""

    for (election <- elections) {
      if (election != elections.last) {
        optionsString = optionsString +
                        s"""{
                           |					"text": {
                           |						"type": "plain_text",
                           |						"text": "${election.name}",
                           |						"emoji": true
                           |					},
                           |					"value": "${election.id.get}"
                           |				},
                         """.stripMargin
      } else {
        optionsString = optionsString +
          s"""{
             |					"text": {
             |						"type": "plain_text",
             |						"text": "${election.name}",
             |						"emoji": true
             |					},
             |					"value": "${election.id.get}"
             |}
            """.stripMargin
      }
    }

    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    //@(options: String, user: String, channel: String)
    http.post(Json.parse(views.txt.slack.messages.selectelectiontoview(optionsString, simpleActionPayload.user.id, simpleActionPayload.channel.id).body))
      .flatMap { response =>
        Future.successful(response.json)
      }

  }

  override def sendElectionsListForView(elections: List[Election], team: SlackTeam, payLoad: SlashCommandPayLoad): Future[JsValue] = {
    var optionsString = ""

    for (election <- elections) {
      if (election != elections.last) {
        optionsString = optionsString +
          s"""{
             |					"text": {
             |						"type": "plain_text",
             |						"text": "${election.name}",
             |						"emoji": true
             |					},
             |					"value": "${election.id.get}"
             |				},
                         """.stripMargin
      } else {
        optionsString = optionsString +
          s"""{
             |					"text": {
             |						"type": "plain_text",
             |						"text": "${election.name}",
             |						"emoji": true
             |					},
             |					"value": "${election.id.get}"
             |}
            """.stripMargin
      }
    }

    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    //@(options: String, user: String, channel: String)
    http.post(Json.parse(views.txt.slack.messages.selectelectiontoview(optionsString, payLoad.userId, payLoad.channelId).body))
      .flatMap { response =>
        Future.successful(response.json)
      }

  }

  override def sendElectionDetails(election: Election, team: SlackTeam, payload: ElectionSelectedActionPayload): Future[JsValue] = {
    def formatCandidates(candidates: List[String]): String = {
      if (candidates.isEmpty) {
        "No candidate"
      } else {
        var candidatesString = candidates.head
        for (candidate <- candidates.tail) {
          candidatesString = candidatesString + s", $candidate"
        }
        candidatesString
      }
    }

    def formatWinners(winners: List[Winner]): String = {
      if (winners.isEmpty) {
        "No winner(s)"
      } else {
        var winnersString = winners.head.candidate.name
        for (winner <- winners.tail) {
          winnersString = winnersString + s", ${winner.candidate.name}"
        }
        winnersString
      }
    }

    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.electiondetails(
      election.id.get, election.name, election.start.toString("yyyy-MM-dd HH:mm:ss"), election.end.toString("yyyy-MM-dd HH:mm:ss"), election.votingAlgo, formatCandidates(election.candidates), formatWinners(election.winners), election.realtimeResult.toString, election.description, payload.user.id, payload.channel.id).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendNoElectionFound(team: SlackTeam, simpleActionPayload: SimpleActionPayload): Future[JsValue] = {
    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.noelections(simpleActionPayload.user.id, simpleActionPayload.channel.id).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendNoElectionFound(team: SlackTeam, payLoad: SlashCommandPayLoad): Future[JsValue] = {
    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.noelections(payLoad.userId, payLoad.channelId).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendResultsMsg(election: Election, team: SlackTeam): Future[JsValue] = {
    def createCandidateVotersString(candidate: String, ballots: List[Ballot]): String = {
      var string =
        s"""{
           |		"type": "context",
           |		"elements": [
           |			{
           |				"type": "mrkdwn",
           |				"text": "*Candidate:* $candidate"
           |			}
           |		]
           |},
           |{
           |		"type": "context",
           |		"elements": [
        """.stripMargin

      if(ballots.isEmpty) {
        string = string +
          """{
            |				"type": "plain_text",
            |				"emoji": true,
            |				"text": "No votes"
            |			}
            |   ]
            |	},
          """.stripMargin
      } else {
        for (ballot <- ballots) {
          if (ballot != ballots.last) {

            string = string +
              s"""{
                 |				"type": "image",
                 |				"image_url": "${ballot.voterImage}",
                 |				"alt_text": "@${ballot.voterName}"
                 |			},
                  """.stripMargin
          } else {
            string = string +
              s"""{
                 |				"type": "image",
                 |				"image_url": "${ballot.voterImage}",
                 |				"alt_text": "@${ballot.voterName}"
                 |			},
                 |			{
                 |				"type": "plain_text",
                 |				"emoji": true,
                 |				"text": "${ballots.size} votes"
                 |			}
                 |   ]
                 |	},
                  """.stripMargin
          }
        }
      }
      string
    }

    def formatWinners(winners: List[(Candidate, Rational)]): String = {
      if (winners.isEmpty) {
        "No winner(s)"
      } else {
        var winnersString = winners.head._1.name
        for (winner <- winners.tail) {
          winnersString = winnersString + s", ${winner._1.name}"
        }
        winnersString
      }
    }

    val winners = CountVotes.countVotesMethod(election.ballot, election.votingAlgo, election.candidates, election.noVacancies)
    var s = ""
    for (candidate <- election.candidates) {
      val bs = election.ballot.filter(b => b.ballotData.equalsIgnoreCase(candidate))
      logger.error(s"\nBallots $bs")
      s = s +
        s"""{
           |		"type": "divider"
           |	},
          """.stripMargin + createCandidateVotersString(candidate, bs)
    }

    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendMsg))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.bot.bot_access_token}")
    http.post(Json.parse(views.txt.slack.messages.resultswithpublicballotsforteam(election.channelId, election.name, election.start.toString("yyyy-MM-dd HH:mm:ss"), election.end.toString("yyyy-MM-dd HH:mm:ss"), election.votingAlgo, s, formatWinners(winners)).body))
      .flatMap { response =>
        Future.successful(response.json)
      }
  }

  override def sendHelpMsg(channel: String, user: String, team: SlackTeam): Future[JsValue] = {
    val http = httpLayer.url(slackAPISettings.baseUrl.concat(slackAPISettings.sendEphemeral))
      .addHttpHeaders(
        "Content-type" -> "application/json",
        "Authorization" -> s"Bearer ${team.accessToken}")
    http.post(Json.parse(views.txt.slack.messages.help(channel, user).body.trim))
      .flatMap { response =>
        Future.successful{
          logger.error(response.toString)
          response.json
        }
      }
  }
}