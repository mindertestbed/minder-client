package gov.tubitak.minder.client

import minderengine.Adapter

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
  def init(): Adapter = {
    val minderClient = new MinderClient
    minderClient.adapter
  }

}
