// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.scalatest.FunSuite
import java.lang.{ Double => JDouble, Integer => JInteger }

class ScalaConversionsTests extends FunSuite {

  import ScalaConversions._

  //case x: Boolean => Boolean.box(x)
  test("Boolean") { testConversion(true -> java.lang.Boolean.TRUE) }

  //case x: Char => x.toString
  test("Char") { testConversion('c' -> "c") }

  //case x: java.lang.Character => x.toString
  test("String") { testConversion("x" -> "x") }

  // scala number types

  //case x: Byte => Double.box(x)
  test("Byte") { testConversion(5.toByte -> 5d) }
  //case x: Short => Double.box(x)
  test("Short") { testConversion(5.toShort -> 5d) }
  //case x: Int => Double.box(x)
  test("Int") { testConversion(5 -> 5d) }
  //case x: Float => Double.box(x)
  test("Float") { testConversion(5f -> 5d) }
  //case x: Double => Double.box(x)
  test("Double") { testConversion(5d -> 5d) }
  //case x: Long => Double.box(x)
  test("Long") { testConversion(5L -> 5d) }

  // java number types

  //case x: java.lang.Byte => Double.box(x.doubleValue)
  test("JavaByte") { testConversion(Byte.box(7.toByte) -> 7d) }
  //case x: java.lang.Short => Double.box(x.doubleValue)
  test("JavaShort") { testConversion(Short.box(7.toShort) -> 7d) }
  //case x: java.lang.Integer => Double.box(x.doubleValue)
  test("JavaInteger") { testConversion(Int.box(7) -> 7d) }
  //case x: java.lang.Float => Double.box(x.doubleValue)
  test("JavaFloat") { testConversion(Float.box(7.toFloat) -> 7d) }
  //case x: java.lang.Long => Double.box(x.doubleValue)
  test("JavaLong") { testConversion(Long.box(7.toLong) -> 7d) }

  //case x: Array[_] => new LogoList(x map {toLogoObject _}: _*)
  test("ArrayOfInts") {
    testConversion(Array(1, 2, 3) -> LogoList(Double.box(1.0), Double.box(2.0), Double.box(3.0)))
  }

  //case x: Seq[_] => new LogoList(x map {toLogoObject _}: _*)
  test("SeqOfInts") {
    testConversion(List(1, 2, 3) -> LogoList(Double.box(1.0), Double.box(2.0), Double.box(3.0)))
  }

  val nestedLogoList =
    LogoList(
      LogoList(Double.box(1.0), Double.box(2.0), Double.box(3.0)),
      LogoList(Double.box(1.0), Double.box(2.0), Double.box(3.0)),
      LogoList(Double.box(1.0), Double.box(2.0), Double.box(3.0)))

  test("ArrayOfArraysOfInts") {
    testConversion(Array(Array(1, 2, 3), Array(1, 2, 3), Array(1, 2, 3)) -> nestedLogoList)
  }

  test("ListOfListsOfInts") {
    testConversion(nestedLogoList -> nestedLogoList)
  }

  test("LogoListOfIntegers") {
    testConversion(LogoList(Int.box(1),
      Int.box(2),
      Int.box(3))
      -> LogoList(Double.box(1.0),
        Double.box(2.0),
        Double.box(3.0)))
  }

  test("SeqOfSeqsOfInts") {
    testConversion(List(List(1, 2, 3), List(1, 2, 3), List(1, 2, 3)) -> nestedLogoList)
  }

  test("ArrayOfSeqsOfInts") {
    testConversion(Array(List(1, 2, 3), List(1, 2, 3), List(1, 2, 3)) -> nestedLogoList)
  }

  test("SeqOfArraysOfInts") {
    testConversion(List(Array(1, 2, 3), Array(1, 2, 3), Array(1, 2, 3)) -> nestedLogoList)
  }

  test("HeterogenousSeq") {
    testConversion(List(1, "test", 'c') -> LogoList(Double.box(1.0), "test", "c"))
  }

  test("NonConvertableObject") {
    case class Point(x: Int, y: Int)
    intercept[RuntimeException] {
      testConversion(Point(4, 2) -> "Error")
    }
  }

  def testConversion(vals: Tuple2[Any, Any]) {
    testConversion(vals._1, vals._2)
  }

  def testConversion(input: Any, expected: Any) {
    val actual = input.toLogoObject
    assertResult(expected)(actual)
  }

}
