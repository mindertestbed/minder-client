package gov.tubitak.minder.client

import java.io.{File, FileInputStream}
import java.lang.reflect.Method
import java.util
import java.util.{HashMap, Properties}

import minderengine._
import org.interop.xoola.core._

class MinderClient(val properties: Properties, val classLoader: ClassLoader) extends IMinderClient with ISignalHandler {

  val currentSession = new ThreadLocal[String]

  /**
    * Default constructor
    */
  def this() {
    this({
      MinderClient.readProps
    }, null)
  }

  def this(properties: Properties) {
    this(properties, null)
  }

  def this(classLoader: ClassLoader) {
    this({
      MinderClient.readProps
    }, classLoader)
  }

  properties.setProperty(XoolaProperty.MODE, XoolaTierMode.CLIENT)
  val adapterName = properties.getProperty("ADAPTER_NAME")
  val adapterVersion = properties.getProperty("ADAPTER_VERSION")
  val adapterIdentifier = new AdapterIdentifier()
  adapterIdentifier.setName(adapterName)
  adapterIdentifier.setVersion(adapterVersion)

  //initialize the XOOLA protocol with adapter name
  properties.setProperty(XoolaProperty.CLIENTID, adapterIdentifier.toString)
  val client = Xoola.init(properties)
  //create the minder client, providing the adapter class name
  println("The adapter Identifier " + adapterIdentifier)
  println("The adapter Class is " + properties.getProperty("ADAPTER_CLASS"))
  val clazz: Class[Adapter] =
    if (classLoader == null)
      Class.forName(properties.getProperty("ADAPTER_CLASS")).asInstanceOf[Class[Adapter]]
    else
      classLoader.loadClass(properties.getProperty("ADAPTER_CLASS")).asInstanceOf[Class[Adapter]]

  val adapter = MinderUtils.createAdapter(clazz, this)
  client registerObject("minderClient", this)
  println("Connecting to server")

  var syncRemoteCaller: IMinderServer = null;
  var asyncRemoteCaller: IMinderServer = null;

  //Map all the methods that are annotated as signals and slots.
  //this will be presented to the server. This is good to perform method resolution only once
  var methodMap: HashMap[String, MethodContainer] = null;

  client.addConnectionListener(new XoolaConnectionListener {
    override def connected(xoolaInvocationHandler: XoolaInvocationHandler, xoolaChannelState: XoolaChannelState): Unit = {
      syncRemoteCaller = client.get(classOf[IMinderServer], "minderServer")
      asyncRemoteCaller = client.get(classOf[IMinderServer], "minderServer", true)

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
      syncRemoteCaller hello(adapterIdentifier, keySet)
    }

    override def disconnected(xoolaInvocationHandler: XoolaInvocationHandler, xoolaChannelState: XoolaChannelState): Unit = {

    }
  })

  client start()

  var testSession: TestSession = null


  /**
    * getCurrentTestUserInfo
    * the server will call this method. The method performs name resolution and calls
    * the appropriate slot
    */
  override def callSlot(testSession: TestSession, slotName: String, args: Array[Object]): Object = {
    synchronized {
      adapter.setSessionId(testSession.getSession)
      val method = methodMap.get(slotName.replaceAll("\\s", "")).method
      method.invoke(adapter, args: _*)
    }
  }

  def checkSession(sId: TestSession) {
  }

  /**
    * The SUT is emitting a signal, lets invoke the server
    */
  override def handleSignal(obj: Any, signalMethod: Method, args: Array[Object]): Object = {
    val temp = if (null != adapter.getSessionId) {
      new TestSession(adapter.getSessionId)
    } else {
      testSession;
    }

    if (signalMethod getName() equals ("getCurrentTestUserInfo")) {
      asyncRemoteCaller.getUserInfo(testSession)
    } else {

      val remoteCaller = if (signalMethod.getAnnotation(classOf[Signal]).async())
        asyncRemoteCaller
      else
        syncRemoteCaller
      //now we should check whether there is an error, or this is a regular call.
      val error = adapter.consumeError()
      if (error != null) {
        remoteCaller signalEmitted(temp, adapterIdentifier, MethodContainer.generateMethodKey(signalMethod), new SignalErrorData(error));
      } else {
        remoteCaller signalEmitted(temp, adapterIdentifier, MethodContainer.generateMethodKey(signalMethod), new SignalCallData(args));
      }
    }
  }

  override def startTest(startTestObject: StartTestObject): Unit = {
    println(adapterIdentifier + " starttest " + startTestObject.getSession.getSession)
    this testSession = startTestObject.getSession
    adapter.setDefaultSession(testSession.getSession)
    adapter startTest startTestObject
  }

  override def finishTest(finishTestObject: FinishTestObject): Unit = {
    adapter finishTest finishTestObject;
    this testSession = null
  }

  override def getSUTIdentifiers(): SUTIdentifiers = {
    adapter getSUTIdentifiers
  }
}

object MinderClient {
  def readProps: Properties = {
    val propertyFile = System.getProperty("propertyFile", "adapter.properties")
    val props = new Properties()
    if (propertyFile != null && new File(propertyFile).exists()) {
      println("Reading properties from alternate locatiom: " + propertyFile)
      val ins = new FileInputStream(propertyFile)
      props load ins;
      ins.close()
    } else {
      val res = System.getProperty("propertyResource", "adapter.properties")
      var url = classOf[MinderClient].getResource(res)

      if (url == null) {
        println("Minder client - Try another class loader")
        url = Thread.currentThread().getContextClassLoader().getResource(res)
      }
      if (url == null) {
        throw new scala.IllegalArgumentException("Couldn't load adapter properties resource [" + res + "]")
      }

      val is = url.openStream()
      props load is;
      is close
    }
    props
  }
}
