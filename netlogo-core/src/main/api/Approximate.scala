// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object Approximate {
  def approximate(n: Double, places: Int): Double = {
    if (!n.isFinite) {
      n
    } else if (n >= 0) {
      BigDecimal(n).setScale(places, BigDecimal.RoundingMode.HALF_UP).toDouble
    } else {
      BigDecimal(n).setScale(places, BigDecimal.RoundingMode.HALF_DOWN).toDouble
    }
  }

  def approximateCeiling(n: Double, places: Int): Double = {
    if (!n.isFinite) {
      n
    } else {
      BigDecimal(n).setScale(places, BigDecimal.RoundingMode.CEILING).toDouble
    }
  }

  def approximateFloor(n: Double, places: Int) = {
    if (!n.isFinite) {
      n
    } else {
      BigDecimal(n).setScale(places, BigDecimal.RoundingMode.FLOOR).toDouble
    }
  }
}
