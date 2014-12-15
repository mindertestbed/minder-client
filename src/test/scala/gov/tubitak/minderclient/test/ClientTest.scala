package gov.tubitak.minderclient.test

import javax.script._
import scala.tools.nsc._

object ClientTest {
  def main(args: Array[String]) {
    val st = new Settings(str => println(str));
    st.usejavacp.value = true;
    val i = new Interpreter(st)
    i.interpret("class Test { def hello = \"Hello World\"}")

    var res = Array[AnyRef](null)
    i.bind("result", "Array[AnyRef]", res)
    i.interpret("result(0) = new Test")
    println(res)
  }
}
