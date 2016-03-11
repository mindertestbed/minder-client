package gov.tubitak.minder.client

import minderengine.Wrapper
import org.apache.log4j.PropertyConfigurator

/**
 * The entry point for a minder client application.
 */
object MinderClientApp {

  /**
    * Initialize the adapter mechanism
    *
    * this is deprecated please use init()
 *
    * @param args
    */
  @Deprecated
  def main(args: Array[String]) {
  }


  /**
    * Initialize the adapter mechanism and return the created adapter.
    */
  def init(): Wrapper ={
    PropertyConfigurator.configure(classOf[MinderClient].getResource("/logging.properties"))
    val minderClient = new MinderClient
    minderClient.wrapper
  }

}
