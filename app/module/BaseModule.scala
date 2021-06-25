package module

import com.google.inject.{AbstractModule, Provides}
import com.mohiva.play.silhouette.impl.providers.OAuth2Settings
import dao._
import net.codingwell.scalaguice.ScalaModule
import play.api.Configuration
import service._
import slack_api.{SlackAPIProvider, SlackAPIService, SlackSettings}

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  override def configure(): Unit = {
    bind[ElectionService].to[ElectionDAOImpl]
    bind[PartialElectionService].to[PartialElectionDAOImpl]
    bind[BallotDataService].to[BallotDataDAOImpl]
    bind[SlackAPIService].to[SlackAPIProvider]
  }
}
