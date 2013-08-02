package org.nlogo.engine

import scala.js.Dynamic.{ global => g }

object Trig {

  def squash(x: Double): Double =
    if ((g.StrictMath.abs(x): Double) < 3.2e-15)
      0
    else
      x

  def sin(degrees: Double): Double =
    squash(g.StrictMath.sin(g.StrictMath.toRadians(degrees)))

  def cos(degrees: Double): Double =
    squash(g.StrictMath.cos(g.StrictMath.toRadians(degrees)))

}
