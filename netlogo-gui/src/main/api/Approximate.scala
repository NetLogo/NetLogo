// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import annotation.strictfp

object Approximate {

  @strictfp
  def approximate(n: Double, places: Int): Double =
    // the 17 here was not arrived at through any deep understanding of IEEE 754 or anything like
    // that, but just by entering different expressions into NetLogo and noting that I couldn't seem
    // to come up with an expression that would make more than 17 decimal places print; an example
    // that makes 17 places print is "show 0.1 - 0.00000000000000001" -- I think there may still be
    // theoretical correctness issues here, perhaps involving very large or very small numbers, but
    // for now this'll have to do - ST 5/3/02
    if (places >= 17)
      n
    else {
      val multiplier = StrictMath.pow(10, places)
      val result = StrictMath.floor(n * multiplier + 0.5) / multiplier
      if (places > 0)
        result
      else
        StrictMath.round(result)
    }

  @strictfp
  def approximateCeiling(n: Double, places: Int): Double =
    if (places >= 17)
      n
    else {
      val multiplier = StrictMath.pow(10, places)
      val result = StrictMath.ceil(n * multiplier) / multiplier
      if (places > 0)
        result
      else
        StrictMath.round(result)
    }

  @strictfp
  def approximateFloor(n: Double, places: Int) =
    if (places >= 17)
      n
    else {
      val multiplier = StrictMath.pow(10, places)
      val result = StrictMath.floor(n * multiplier) / multiplier
      if (places > 0)
        result
      else
        StrictMath.round(result)
    }

}
