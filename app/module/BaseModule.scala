package module

import com.google.inject.AbstractModule
import dao._
import net.codingwell.scalaguice.ScalaModule
import service._

/**
 * The base Guice module.
 */
class BaseModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure(): Unit = {
    bind[ElectionService].to[ElectionDAOImpl]
  }
}
