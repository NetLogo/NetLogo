package org.nlogo

import scala.collection.mutable.{ LinkedHashMap => LHM }

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

}

