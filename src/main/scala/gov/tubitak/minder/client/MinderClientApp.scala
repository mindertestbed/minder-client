package gov.tubitak.minder.client

import org.apache.log4j.PropertyConfigurator
import org.interop.xoola.core.Xoola
import org.interop.xoola.core.XoolaProperty
import org.interop.xoola.core.XoolaTierMode
import minderengine.Wrapper
import minderengine.MinderUtils
import minderengine.IMinderServer

/**
 * The entry point for a minder client application.
 */
object MinderClientApp {
  def main(args: Array[String]) {
    PropertyConfigurator.configure(classOf[MinderClient].getResource("/logging.properties"))
    val minderClient = new MinderClient
  }
}
