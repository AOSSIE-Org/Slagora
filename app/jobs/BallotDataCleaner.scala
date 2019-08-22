package jobs

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.util.Clock
import javax.inject.Inject
import service.BallotDataService

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class BallotDataCleaner @Inject()(actorSystem: ActorSystem, ballotDataService: BallotDataService, clock: Clock)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.schedule(initialDelay = 0 second, interval = 5 minutes) {
    // Delete all expired ballot data
    ballotDataService.findExpired(clock.now).flatMap{
      ballots => Future {
        for (b <- ballots) yield { ballotDataService.delete(b.id)}
      }
    }
  }
}