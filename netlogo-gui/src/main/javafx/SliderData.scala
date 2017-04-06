// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import java.math.{ BigDecimal, MathContext }

import javafx.beans.binding.{ Binding, DoubleBinding }
import javafx.beans.property.{ DoubleProperty, SimpleDoubleProperty }

import org.nlogo.api.Approximate.approximate

class SliderData(val inputValue: DoubleProperty, val minimum: Double, val maximum: Double, val increment: Double) {

  val value: DoubleProperty =
    new SimpleDoubleProperty(inputValue.get)

  value.bind(new DoubleBinding {
    super.bind(inputValue)

    override protected def computeValue(): Double = {
      coerceValue(inputValue.get)
    }
  })

  override def toString =
    s"SliderData(${value.get}, $minimum, $maximum, $increment)"

  private def precisionOf(x: Double): Int = {
    if (x == 0) 0
    else {
      val d = new BigDecimal(x)
      val decimalDigits = d.round(MathContext.DECIMAL64).stripTrailingZeros.precision
      val integerDigits = (StrictMath.floor(StrictMath.log10(x)) + 1)
      (decimalDigits - integerDigits).toInt max 0
    }
  }

  def precision: Int =
    StrictMath.max(precisionOf(minimum), precisionOf(increment))

  def effectiveMaximum = {
    if (minimum >= maximum || increment == 0) minimum
    else {
      val incremented = minimum + (increment * StrictMath.floor((maximum - minimum) / increment))
      // Sometimes rounding error leaves us off by one here, for example if min is 0.1,
      // increment 0.1, max 50, then result is 49.900000000000006 instead of 50 like it
      // should be.  So we explicitly test for this off by one error here. - ST 9/17/02
      val corrected =
        if (approximate(incremented + increment, precision) <= maximum) incremented + increment
        else                                                            incremented
      approximate(corrected, precision)
    }
  }

  private def coerceValue(x: Double): Double = {
    val max = StrictMath.max(effectiveMaximum, minimum)
    val min = StrictMath.min(effectiveMaximum, minimum)
    StrictMath.max(min, StrictMath.min(x, max))
  }
}
