package gov.tubitak.minder.client

import org.apache.log4j.PropertyConfigurator

/**
 * The entry point for a minder client application.
 */
object MinderClientApp {
  def main(args: Array[String]) {
    PropertyConfigurator.configure(classOf[MinderClient].getResource("/logging.properties"))
    val minderClient = new MinderClient
  }

}
