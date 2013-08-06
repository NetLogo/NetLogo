package org.nlogo.tortoise

import scala.collection.mutable.{ LinkedHashMap => LHM }

package object engine {

  type JSW                           = JSWrapper[_]
  type VarMap                        = LHM[String, JSW]
  def  VarMap(items: (String, JSW)*) = LHM(items: _*)

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

