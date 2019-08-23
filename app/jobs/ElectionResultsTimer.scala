package jobs

import akka.actor.ActorSystem
import javax.inject.Inject
import models.security.{SlackTeam, SlackUser}
import service.{ElectionService, TeamService, UserService}
import slack_api.SlackAPIService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ElectionResultsTimer @Inject()(actorSystem: ActorSystem, electionService: ElectionService, slackAPIService: SlackAPIService, teamService: TeamService, userService: UserService)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.schedule(initialDelay = 0 second, interval = 5 minutes) {
    // Delete send due election results to channels
    electionService.getNonCounted().flatMap{
      elections => Future {
        for (election <- elections) yield {
          if(election.end.isBeforeNow) {
            for {
              user <- userService.get(election.loginInfo.get)
              team <- teamService.get(SlackTeam.buildLoginInfo(user.get.team.id))
            } yield {
                if(user.isDefined && team.isDefined) {
                  slackAPIService.sendResultsMsg(election, team.get).flatMap(_ => electionService.setIsCounted(election.id.get))
                }
            }
          }
        }
      }
    }
  }
}
