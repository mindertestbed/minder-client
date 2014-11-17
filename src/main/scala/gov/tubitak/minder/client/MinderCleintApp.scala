package gov.tubitak.minder.client

import org.interop.xoola.core.Xoola
import org.interop.xoola.core.XoolaProperty
import org.interop.xoola.core.XoolaTierMode
import minderengine.Wrapper
import minderengine.MinderUtils
import minderengine.IMinderServer

/**
 * The entry point for a minder client application.
 */
object MinderClientApp extends App {
  val minderClient  = new MinderClient
}
