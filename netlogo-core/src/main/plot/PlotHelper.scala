// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import scala.math.{ abs, log10, pow }

import org.nlogo.api.Approximate

object PlotHelper {
  def approximateLimit(rawLimit: Double, range: Double): Double = {
    val places = 3.0 - log10(range).floor
    if (rawLimit < 0) {
      -1 * Approximate.approximate(abs(rawLimit), places.toInt)
    } else {
      Approximate.approximate(rawLimit, places.toInt)
    }
  }

  // This is meant to give values for a new range that mimic how Excel gets it values for charts.
  // -Jeremy B May 2025
  def prettyRange(range: Double): Double = {
    val tmag = pow(10, log10(range).floor - 1) * 2
    (range / tmag).ceil * tmag
  }

  // This gives a new min or max based on a value outside the range.  It's an aesthetic function, so tweaks are welcome
  // and probably non-breaking, just be aware of numerous edge cases like:  very small ranges (10.0005 to 10.0010), min
  // above 0 (10000 to 10050), max below 0 (-100 to -50), and I'm sure many others.  The basic idea is to shift the min
  // over to 0 for the `prettyLimit()` function.  The shift is unapplied at the end.  Note that the `approximateLimit()`
  // is necessary as the last step, since the results of the calcs can give some of the odd-looking floating-point
  // numbers, which we don't want to see in the front-end if we can help it.  -Jeremy B May 2025
  def expandRange(min: Double, max: Double, newValue: Double): Double = {
    val shift     = -min
    val tempMin   = 0 // not necessary, but makes the code clearer to me
    val tempMax   = max + shift
    val tempValue = newValue + shift
    if (tempValue < 0) {
      val tempRange = tempMax - tempValue
      val newRange  = prettyRange(tempRange)
      val rawMin    = (tempMax - newRange) - shift
      val newMin    = approximateLimit(rawMin, tempRange)
      newMin
    } else {
      val tempRange = tempValue - tempMin
      val newRange  = prettyRange(tempRange)
      val rawMax    = (newRange + tempMin) - shift
      val newMax    = approximateLimit(rawMax, tempRange)
      newMax
    }
  }

}
