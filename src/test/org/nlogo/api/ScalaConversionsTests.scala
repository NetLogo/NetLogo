package org.nlogo.api

import org.scalatest.FunSuite
import java.lang.{ Double => JDouble, Integer => JInteger }

class ScalaConversionsTests extends FunSuite {

  import ScalaConversions._

  //case x: Boolean => java.lang.Boolean.valueOf(x)
  test("Boolean") { testConversion(true -> java.lang.Boolean.TRUE) }

  //case x: Char => java.lang.String.valueOf(x)
  test("Char") { testConversion('c' -> "c") }

  //case x: java.lang.Character => x.toString
  test("String") { testConversion("x" -> "x") }

  // scala number types

  //case x: Byte => java.lang.Double.valueOf(x)
  test("Byte") { testConversion(5.toByte -> 5d) }
  //case x: Short => java.lang.Double.valueOf(x)
  test("Short") { testConversion(5.toShort -> 5d) }
  //case x: Int => java.lang.Double.valueOf(x)
  test("Int") { testConversion(5 -> 5d) }
  //case x: Float => java.lang.Double.valueOf(x)
  test("Float") { testConversion(5f -> 5d) }
  //case x: Double => java.lang.Double.valueOf(x)
  test("Double") { testConversion(5d -> 5d) }
  //case x: Long => java.lang.Double.valueOf(x)
  test("Long") { testConversion(5L -> 5d) }

  // java number types

  //case x: java.lang.Byte => java.lang.Double.valueOf(x.doubleValue)
  test("JavaByte") { testConversion(java.lang.Byte.valueOf(7.toByte) -> 7d) }
  //case x: java.lang.Short => java.lang.Double.valueOf(x.doubleValue)
  test("JavaShort") { testConversion(java.lang.Short.valueOf(7.toShort) -> 7d) }
  //case x: java.lang.Integer => java.lang.Double.valueOf(x.doubleValue)
  test("JavaInteger") { testConversion(java.lang.Integer.valueOf(7) -> 7d) }
  //case x: java.lang.Float => java.lang.Double.valueOf(x.doubleValue)
  test("JavaFloat") { testConversion(java.lang.Float.valueOf(7.toFloat) -> 7d) }
  //case x: java.lang.Long => java.lang.Double.valueOf(x.doubleValue)
  test("JavaLong") { testConversion(java.lang.Long.valueOf(7.toLong) -> 7d) }

  //case x: Array[_] => new LogoList(x map {toLogoObject _}: _*)
  test("ArrayOfInts") {
    testConversion(Array(1, 2, 3) -> LogoList(JDouble.valueOf(1.0), JDouble.valueOf(2.0), JDouble.valueOf(3.0)))
  }

  //case x: Seq[_] => new LogoList(x map {toLogoObject _}: _*)
  test("SeqOfInts") {
    testConversion(List(1, 2, 3) -> LogoList(JDouble.valueOf(1.0), JDouble.valueOf(2.0), JDouble.valueOf(3.0)))
  }

  val nestedLogoList =
    LogoList(
      LogoList(JDouble.valueOf(1.0), JDouble.valueOf(2.0), JDouble.valueOf(3.0)),
      LogoList(JDouble.valueOf(1.0), JDouble.valueOf(2.0), JDouble.valueOf(3.0)),
      LogoList(JDouble.valueOf(1.0), JDouble.valueOf(2.0), JDouble.valueOf(3.0)))

  test("ArrayOfArraysOfInts") {
    testConversion(Array(Array(1, 2, 3), Array(1, 2, 3), Array(1, 2, 3)) -> nestedLogoList)
  }

  test("ListOfListsOfInts") {
    testConversion(nestedLogoList -> nestedLogoList)
  }

  test("LogoListOfIntegers") {
    testConversion(LogoList(JInteger.valueOf(1),
      JInteger.valueOf(2),
      JInteger.valueOf(3))
      -> LogoList(JDouble.valueOf(1.0),
        JDouble.valueOf(2.0),
        JDouble.valueOf(3.0)))
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
    testConversion(List(1, "test", 'c') -> LogoList(JDouble.valueOf(1.0), "test", "c"))
  }

  test("NonConvertableObject") {
    case class Point(x: Int, y: Int)
    intercept[RuntimeException] {
      testConversion(Point(4, 2) -> "Error")
    }
  }

  private def testConversion(vals: Tuple2[Any, Any]) {
    testConversion(vals._1, vals._2)
  }

  private def testConversion(input: Any, expected: Any) {
    val actual = input.toLogoObject
    expect(expected)(actual)
  }

}
