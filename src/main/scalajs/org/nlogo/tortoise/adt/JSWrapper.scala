package org.nlogo.tortoise.adt

trait JSWrapper {
  type ValueType
  def value: ValueType
  def toJS: AnyJS
}

object JSWrapper {

  implicit class BooleanJSWrapper(override val value: Boolean) extends JSWrapper {
    override type ValueType = Boolean
    override def toJS = AnyJS.fromBoolean(value)
  }

  implicit class ByteJSWrapper(override val value: Byte) extends JSWrapper {
    override type ValueType = Byte
    override def toJS = AnyJS.fromByte(value)
  }

  implicit class DoubleJSWrapper(override val value: Double) extends JSWrapper {
    override type ValueType = Double
    override def toJS = AnyJS.fromDouble(value)
  }

  implicit class FloatJSWrapper(override val value: Float) extends JSWrapper {
    override type ValueType = Float
    override def toJS = AnyJS.fromFloat(value)
  }

  implicit class IntJSWrapper(override val value: Int) extends JSWrapper {
    override type ValueType = Int
    override def toJS = AnyJS.fromInt(value)
  }

  implicit class LongJSWrapper(override val value: Long) extends JSWrapper {
    override type ValueType = Long
    override def toJS = AnyJS.fromLong(value)
  }

  implicit class ShortJSWrapper(override val value: Short) extends JSWrapper {
    override type ValueType = Short
    override def toJS = AnyJS.fromShort(value)
  }

  implicit class StringJSWrapper(override val value: String) extends JSWrapper {
    override type ValueType = String
    override def toJS = AnyJS.fromString(value)
  }

  implicit class UnitJSWrapper(override val value: Unit) extends JSWrapper {
    override type ValueType = Unit
    override def toJS = AnyJS.fromUnit(value)
  }

}
