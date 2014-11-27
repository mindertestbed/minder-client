package gov.tubitak.minderclient.test

import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import org.specs2.Specification

import javax.script._
import scala.tools.nsc._

object ClientTest {
  def main(args: Array[String]) {
    var i = new Interpreter(new Settings(str => println(str)))
    i.interpret("class Test { def hello = \"Hello World\"}")

    var res = Array[AnyRef](null)
    i.bind("result", "Array[AnyRef]", res)
    i.interpret("result(0) = new Test")
    println(res)
  }
}
