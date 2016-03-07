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
  val wrapperName = properties.getProperty("WRAPPER_NAME")
  val wrapperVersion = properties.getProperty("WRAPPER_VERSION")
  val wrapperIdentifier = new AdapterIdentifier()
  wrapperIdentifier.setName(wrapperName)
  wrapperIdentifier.setVersion(wrapperVersion)

  //initialize the XOOLA protocol with wrapper name
  properties.setProperty(XoolaProperty.CLIENTID, wrapperIdentifier.toString)
  val client = Xoola.init(properties)
  //create the minder client, providing the wrapper class name
  println("The wrapper Identifier " + wrapperIdentifier)
  println("The wrapper Class is " + properties.getProperty("WRAPPER_CLASS"))
  val clazz: Class[Wrapper] =
    if (classLoader == null)
      Class.forName(properties.getProperty("WRAPPER_CLASS")).asInstanceOf[Class[Wrapper]]
    else
      classLoader.loadClass(properties.getProperty("WRAPPER_CLASS")).asInstanceOf[Class[Wrapper]]
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
      nonBlockingServerObject = client.get(classOf[IMinderServer], "minderServer", true)

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
      serverObject hello(wrapperIdentifier, keySet)
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
    checkSession(testSession)
    wrapper.setSessionId(testSession.getSession)
    methodMap.get(slotName.replaceAll("\\s", "")).method.invoke(wrapper, args: _*)
  }

  def checkSession(sId: TestSession) {
    if (testSession == null || testSession != sId) {
      throw new MinderException(MinderException.E_INVALID_SESSION)
    }
  }

  /**
    * The SUT is emitting a signal, lets invoke the server
    */
  override def handleSignal(obj: Any, signalMethod: Method, args: Array[Object]): Object = {
    checkSession(testSession);
    if (signalMethod getName() equals ("getCurrentTestUserInfo")) {
      nonBlockingServerObject.getUserInfo(testSession)
    } else {

      //now we should check whether there is an error, or this is a regular call.
      val error = wrapper.consumeError()
      if (error != null) {
        nonBlockingServerObject signalEmitted(testSession, wrapperIdentifier, MethodContainer.generateMethodKey(signalMethod), new SignalErrorData(error));
      } else {
        nonBlockingServerObject signalEmitted(testSession, wrapperIdentifier, MethodContainer.generateMethodKey(signalMethod), new SignalCallData(args));
      }
    }
  }

  override def startTest(startTestObject: StartTestObject): Unit = {
    println(wrapperIdentifier + " starttest " + startTestObject.getSession.getSession)
    this testSession =  startTestObject.getSession
    wrapper startTest startTestObject
  }

  override def finishTest(finishTestObject: FinishTestObject): Unit = {
    wrapper finishTest finishTestObject;
    this testSession = null
  }

  override def getSUTIdentifiers(): SUTIdentifiers = {
    wrapper getSUTIdentifiers
  }
}

object MinderClient {
  def readProps: Properties = {
    val propertyFile = System.getProperty("propertyFile", "wrapper.properties")
    val props = new Properties()
    if (propertyFile != null && new File(propertyFile).exists()) {
      println("Reading properties from alternate locatiom: " + propertyFile)
      val ins = new FileInputStream(propertyFile)
      props load ins;
      ins.close()
    } else {
      val res = System.getProperty("propertyResource", "wrapper.properties")
      var url = classOf[MinderClient].getResource(res)

      if (url == null) {
        println("Minder client - Try another class loader")
        url = Thread.currentThread().getContextClassLoader().getResource(res)
      }
      if (url == null) {
        throw new scala.IllegalArgumentException("Couldn't load wrapper properties resource [" + res + "]")
      }

      val is = url.openStream()
      props load is;
      is close
    }
    props
  }
}
