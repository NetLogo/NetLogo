package org.nlogo.tortoise.engine

import org.nlogo.tortoise.adt.JS2ScalaConverters

object Trig {

  import JS2ScalaConverters.num2Double

  def squash(x: Double): Double =
    if (StrictMathJS.abs(x).asScala < 3.2e-15)
      0
    else
      x

  def sin(degrees: Double): Double =
    squash(StrictMathJS.sin(StrictMathJS.toRadians(degrees)).asScala)

  def cos(degrees: Double): Double =
    squash(StrictMathJS.cos(StrictMathJS.toRadians(degrees)).asScala)

}
