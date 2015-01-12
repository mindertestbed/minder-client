package gov.tubitak.minderclient.test

import scala.collection._

object ClientTest {
  def main2(args: Array[String]) {
    val set = mutable.Set[String]();
    set += "A"
    set += "B"
    set += "C"
    set += "D"
    set.map{ str => println(str) }

    set += "A"
    set.map{ str => println(str) }


    println(Map.getClass)

  }




  def main(args: Array[String]) {
    val set = mutable.LinkedHashMap[String,String]();

    set += "A" -> "AA"
    set += "B" -> "BB"
    set += "C" -> "CC"
    set += "D" -> "DD"

    set.map{
      str =>
        println(str)
    }

    println("===")

    set += "A" -> "XXX"

    set.map{str =>
      println(str)
    }


    for((k,v) <- set){
      println(k + "::::" + v)
    }
  }
}
