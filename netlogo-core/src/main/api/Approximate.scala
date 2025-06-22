// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object Approximate {
  def approximate(n: Double, places: Int): Double =
    BigDecimal(n).setScale(places, BigDecimal.RoundingMode.HALF_UP).toDouble

  def approximateCeiling(n: Double, places: Int): Double =
    BigDecimal(n).setScale(places, BigDecimal.RoundingMode.CEILING).toDouble

  def approximateFloor(n: Double, places: Int) =
    BigDecimal(n).setScale(places, BigDecimal.RoundingMode.FLOOR).toDouble
}
