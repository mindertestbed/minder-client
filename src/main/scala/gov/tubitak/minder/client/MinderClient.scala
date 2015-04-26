package gov.tubitak.minder.client

import java.io.FileInputStream
import java.lang.reflect.Method
import java.util
import java.util.HashMap

import minderengine._
import org.interop.xoola.core._

class MinderClient extends IMinderClient with ISignalHandler {
  //load application properties
  val properties = new java.util.Properties();

  val propertyFile = System.getProperty("propertyFile")
  if (propertyFile != null) {
    println("Reading properties from alternate locatiom: " + propertyFile)
    val ins = new FileInputStream(propertyFile)
    properties load ins;
    ins.close()
  } else {
    properties load this.getClass().getResourceAsStream("/app.properites")
  }
  properties.setProperty(XoolaProperty.MODE, XoolaTierMode.CLIENT)
  val wrapperName = properties.getProperty("WRAPPER_NAME")
  properties.setProperty(XoolaProperty.CLIENTID, wrapperName)
  val client = Xoola.init(properties)
  //create the minder client, providing the wrapper class name
  println("The wrapper Name is " + wrapperName)
  println("The wrapper Class is " + properties.getProperty("WRAPPER_CLASS"))
  val clazz: Class[Wrapper] = Class.forName(properties.getProperty("WRAPPER_CLASS")).asInstanceOf[Class[Wrapper]]
  val wrapper = MinderUtils.createWrapper(clazz, this)
  client registerObject("minderClient", this)
  println("Connecting to server")


  var serverObject: IMinderServer = null;
  var nonBlockingServerObject: IMinderServer = null;

  //Map all the methods that are annotated as signals and slots.
  //this will be presented to the server. This is good to perform method resolution only once
  var methodMap: HashMap[String, MethodContainer] = null;

  client.addConnectionListener(new XoolaConnectionListener {
    override def connected(xoolaInvocationHandler: XoolaInvocationHandler, xoolaChannelState: XoolaChannelState): Unit = {
      serverObject = client.get(classOf[IMinderServer], "minderServer")
      nonBlockingServerObject  = client.get(classOf[IMinderServer], "minderServer", true)

      //Map all the methods that are annotated as signals and slots.
      //this will be presented to the server. This is good to perform method resolution only once
      methodMap = new HashMap[String, MethodContainer]()
      for (
        method <- clazz.getMethods() //
        if method.getAnnotation(classOf[Signal]) != null || method.getAnnotation(classOf[Slot]) != null
      ) {
        val mc = new MethodContainer(method)
        methodMap put(mc.methodKey, mc)
      }

      //send the information to the server
      val keySet = new util.HashSet[MethodContainer]();

      import scala.collection.JavaConversions._

      for (x <- methodMap.values()) {
        keySet add x
      }
      serverObject hello(wrapperName, keySet)
    }

    override def disconnected(xoolaInvocationHandler: XoolaInvocationHandler, xoolaChannelState: XoolaChannelState): Unit = {

    }
  })

  client start()

  var sessionId: String = null


  /**
   * getCurrentTestUserInfo
   * the server will call this method. The method performs name resolution and calls
   * the appropriate slot
   */
  override def callSlot(sId: String, slotName: String, args: Array[Object]): Object = {
    checkSession(sId)
    methodMap.get(slotName).method.invoke(wrapper, args: _*)
  }

  def checkSession(sId: String) {
    if (sessionId == null || !(sessionId equals (sId))) {
      throw new MinderException(MinderException.E_INVALID_SESSION)
    }
  }

  /**
   * The SUT is emitting a signal, lets invoke the server
   */
  override def handleSignal(obj: Any, signalMethod: Method, args: Array[Object]): Object = {
    checkSession(sessionId);
    if (signalMethod getName() equals ("getCurrentTestUserInfo")) {
      nonBlockingServerObject.getUserInfo(sessionId)
    } else {
      nonBlockingServerObject signalEmitted(sessionId, wrapperName, MethodContainer.generateMethodKey(signalMethod), new SignalData(args));
    }
  }

  override def startTest(sessionId: String): Unit = {
    println(wrapperName + " starttest " + sessionId)
    this sessionId = sessionId
    wrapper startTest
  }

  override def finishTest(): Unit = {
    wrapper finishTest;
    this sessionId = null
  }
}