package org.nlogo.tortoise

import scala.collection.mutable.{ LinkedHashMap => LHM }

package object engine {

  type AnyJS      = scala.js.Any
  type ArrayJS[T] = scala.js.Array[T]
  type BooleanJS  = scala.js.Boolean
  type NumberJS   = scala.js.Number
  type ObjectJS   = scala.js.Object
  type StringJS   = scala.js.String

  val AnyJS     = scala.js.Any
  val ArrayJS   = scala.js.Array
  val BooleanJS = scala.js.Boolean
  val NumberJS  = scala.js.Number
  val ObjectJS  = scala.js.Object
  val StringJS  = scala.js.String

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

