package module

import jobs.{BallotDataCleaner, PartialElectionCleaner}
import play.api.inject._
import play.api.inject.SimpleModule

class JobModule extends SimpleModule(
  bind[BallotDataCleaner].toSelf.eagerly(),
  bind[PartialElectionCleaner].toSelf.eagerly()
)
