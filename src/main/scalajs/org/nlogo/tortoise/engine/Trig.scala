package org.nlogo.tortoise.engine

import scala.js.Dynamic.{ global => g }

object Trig {

  import Dynamic2ScalaConverters.num2Double

  def squash(x: Double): Double =
    if ((g.StrictMath.abs(x).asScala) < 3.2e-15)
      0
    else
      x

  def sin(degrees: Double): Double =
    squash(g.StrictMath.sin(g.StrictMath.toRadians(degrees)).asScala)

  def cos(degrees: Double): Double =
    squash(g.StrictMath.cos(g.StrictMath.toRadians(degrees)).asScala)

}
