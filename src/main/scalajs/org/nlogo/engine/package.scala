package org.nlogo

import
  scala.{ collection, js },
    collection.mutable.{ LinkedHashMap => LHM },
    js.{ Any => AnyJS, Dynamic, Number => NumberJS },
      NumberJS.toDouble

package object engine {

// Annoying features of ScalaJS mean that I can't use this right now (if ever) --JAB (7/31/13)
//  type RandomType = {
//    def nextDouble(): Double
//  }
//
//  type StrictMathType = {
//    def abs      (d: Double): Double
//    def sin      (d: Double): Double
//    def cos      (d: Double): Double
//    def toRadians(d: Double): Double
//  }

  // Yuck. --JAB (7/31/13)
  // This is a bad idea, Jason, and you know it. --JAB (7/31/13)
  implicit def num2Double(that: Dynamic): Double = toDouble(that.asInstanceOf[NumberJS])
  implicit def num2Int   (that: Dynamic): Int    = num2Double(that).toInt

  type JSW                           = JSWrapper[_]
  type VarMap                        = LHM[String, JSW]
  def  VarMap(items: (String, JSW)*) = LHM(items: _*)

  trait JSWrapper[T] {
    def value: T
    def toJS: AnyJS
  }

  implicit class ArrayJSWrapper(override val value: Array[_]) extends JSWrapper[Array[_]] {
    override def toJS = AnyJS.fromArray(value)
  }

  implicit class BooleanJSWrapper(override val value: Boolean) extends JSWrapper[Boolean] {
    override def toJS = AnyJS.fromBoolean(value)
  }

  implicit class ByteJSWrapper(override val value: Byte) extends JSWrapper[Byte] {
    override def toJS = AnyJS.fromByte(value)
  }

  implicit class DoubleJSWrapper(override val value: Double) extends JSWrapper[Double] {
    override def toJS = AnyJS.fromDouble(value)
  }

  implicit class FloatJSWrapper(override val value: Float) extends JSWrapper[Float] {
    override def toJS = AnyJS.fromFloat(value)
  }

  implicit class IDJSWrapper(override val value: ID) extends JSWrapper[ID] {
    override def toJS = AnyJS.fromLong(value.value)
  }

  implicit class IntJSWrapper(override val value: Int) extends JSWrapper[Int] {
    override def toJS = AnyJS.fromInt(value)
  }

  implicit class LongJSWrapper(override val value: Long) extends JSWrapper[Long] {
    override def toJS = AnyJS.fromLong(value)
  }

  implicit class NLColorJSWrapper(override val value: NLColor) extends JSWrapper[NLColor] {
    override def toJS = AnyJS.fromDouble(value.value)
  }

  implicit class ShortJSWrapper(override val value: Short) extends JSWrapper[Short] {
    override def toJS = AnyJS.fromShort(value)
  }

  implicit class StringJSWrapper(override val value: String) extends JSWrapper[String] {
    override def toJS = AnyJS.fromString(value)
  }

  implicit class UnitJSWrapper(override val value: Unit) extends JSWrapper[Unit] {
    override def toJS = AnyJS.fromUnit(value)
  }

  implicit class XCorJSWrapper(override val value: XCor) extends JSWrapper[XCor] {
    override def toJS = AnyJS.fromDouble(value.value)
  }

  implicit class YCorJSWrapper(override val value: YCor) extends JSWrapper[YCor] {
    override def toJS = AnyJS.fromDouble(value.value)
  }

}

package engine {

  // To be a "universal trait" (and, thus, be usable by value classes), this must extend `Any` --JAB (7/31/13)
  trait ValueClass[T] extends Any { self: AnyVal => def value: T }

  case class ID     (override val value: Long)   extends AnyVal with ValueClass[Long]
  case class NLColor(override val value: Double) extends AnyVal with ValueClass[Double]
  case class XCor   (override val value: Double) extends AnyVal with ValueClass[Double]
  case class YCor   (override val value: Double) extends AnyVal with ValueClass[Double]

}

