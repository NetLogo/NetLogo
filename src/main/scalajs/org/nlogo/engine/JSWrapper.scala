package org.nlogo.engine

import scala.js.{ Any => AnyJS }

trait JSWrapper[T] {
  def value: T
  def toJS: AnyJS
}

object JSWrapper {

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
