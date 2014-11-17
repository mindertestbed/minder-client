package gov.tubitak.minder.client

import minderengine._
import java.lang.reflect.Method
import java.util.HashMap
import org.interop.xoola.core.XoolaProperty
import org.interop.xoola.core.Xoola
import org.interop.xoola.core.XoolaTierMode

class MinderClient extends IMinderClient with ISignalHandler {
  //load application properties
  val properties = new java.util.Properties();
  properties.setProperty(XoolaProperty.MODE, XoolaTierMode.CLIENT)
  properties.setProperty(XoolaProperty.CLIENTID, properties.getProperty("GUID"))
  properties load this.getClass().getResourceAsStream("/app.properites")
  val client = Xoola.init(properties)
  //create the minder client, providing the wrapper class name
  val clazz: Class[_] = Class.forName(properties.getProperty("WRAPPER_CLASS"))
  val wrapper = MinderUtils.createWrapper(clazz, this)
  client registerObject ("minderClient", this)
  println("Connecting to server")
  client start ()
  client waitForConnection ()

  val serverObject: IMinderServer = client.get(classOf[IMinderServer], "minderServer")
  val asyncServerObject: IMinderServer = client.get(classOf[IMinderServer], "minderServer", true)

  //Map all the methods that are annotated as signals and slots.
  //this will be presented to the server. This is good to perform method resolution only once
  val methodMap: HashMap[String, MethodContainer] = new HashMap[String, MethodContainer]()
  for (
    method <- clazz.getMethods() //
    if method.getAnnotation(classOf[Signal]) != null || method.getAnnotation(classOf[Slot]) != null
  ) {
    var mc = new MethodContainer(method)
    methodMap put (mc.methodKey, mc)
  }

  //send the information to the server
  serverObject hello methodMap

  /**
   * the server will call this method. The method performs name resolution and calls
   * the appropriate slot
   */
  override def callSlot(slotName: String, args: Array[Object]) : Object = {
    methodMap.get(slotName).method.invoke(wrapper, args)
  }

  /**
   * The SUT is emitting a signal, lets invoke the server
   */
  override def handleSignal(obj: Any, signalMethod: Method, args: Array[Object]) = {
    asyncServerObject signalEmitted (MethodContainer.generateMethodKey(signalMethod), args);
  }
}