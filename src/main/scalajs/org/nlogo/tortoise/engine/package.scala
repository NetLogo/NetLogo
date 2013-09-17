package org.nlogo.tortoise

import adt.{ AnyJS, JSWrapper }

package object engine {

  implicit class IDJSWrapper(override val value: ID) extends JSWrapper {
    override type ValueType = ID
    override def toJS = AnyJS.fromLong(value.value)
  }

  implicit class PenModeJSWrapper(override val value: PenMode) extends JSWrapper {
    override type ValueType = PenMode
    override def toJS = AnyJS.fromString(if (value == PenUp) "up" else "down")
  }

  implicit class NLColorJSWrapper(override val value: NLColor) extends JSWrapper {
    override type ValueType = NLColor
    override def toJS = AnyJS.fromDouble(value.value)
  }

  implicit class XCorJSWrapper(override val value: XCor) extends JSWrapper {
    override type ValueType = XCor
    override def toJS = AnyJS.fromDouble(value.value)
  }

  implicit class YCorJSWrapper(override val value: YCor) extends JSWrapper {
    override type ValueType = YCor
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

  sealed trait PenMode
  case object PenUp   extends PenMode
  case object PenDown extends PenMode

}

