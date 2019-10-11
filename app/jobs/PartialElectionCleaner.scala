package jobs

import akka.actor.ActorSystem
import com.mohiva.play.silhouette.api.util.Clock
import javax.inject.Inject
import service.PartialElectionService
import scala.concurrent.duration._

import scala.concurrent.{ExecutionContext, Future}

class PartialElectionCleaner @Inject()(actorSystem: ActorSystem, partialElectionService: PartialElectionService, clock: Clock)(implicit executionContext: ExecutionContext) {
  actorSystem.scheduler.schedule(initialDelay = 0 second, interval = 5 minutes) {
    // Delete all expired partial election
    partialElectionService.findExpired(clock.now).flatMap{
      partialElections => Future {
        for (pe <- partialElections) yield { partialElectionService.delete(pe.id)}
      }
    }
  }
}
