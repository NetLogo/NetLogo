// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import java.math.{ BigDecimal, MathContext }

import javafx.beans.binding.{ Binding, DoubleBinding }
import javafx.beans.property.{ DoubleProperty, SimpleDoubleProperty }

import org.nlogo.api.Approximate.approximate

class SliderData(
  val inputValueProperty: DoubleProperty,
  val minimumProperty:    DoubleProperty,
  val maximumProperty:    DoubleProperty,
  val incrementProperty:  DoubleProperty) {

  def this(initValue: Double, min: Double, max: Double, inc: Double) =
    this(
      new SimpleDoubleProperty(initValue),
      new SimpleDoubleProperty(min),
      new SimpleDoubleProperty(max),
      new SimpleDoubleProperty(inc))

  def inputValue: Double = inputValueProperty.get

  val valueProperty: DoubleProperty = new SimpleDoubleProperty(coerceValue(inputValue))
  def value:         Double         = valueProperty.get

  def minimum   = minimumProperty.get
  def maximum   = maximumProperty.get
  def increment = incrementProperty.get

  private var modelValue: Option[Double] = None

  private val valueBinding =
    new DoubleBinding {
      super.bind(inputValueProperty, minimumProperty, maximumProperty, incrementProperty)

      override protected def computeValue(): Double = {
        val r = modelValue.getOrElse(coerceValue(inputValue))
        modelValue = None
        r
      }
    }

  valueProperty.bind(valueBinding)

  override def toString =
    s"SliderData(${value}, $minimum, $maximum, $increment)"

  def updateFromModel(fromModel: Double) = {
    modelValue = Some(fromModel)
    valueBinding.invalidate()
  }

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
    if (x > effectiveMaximum) effectiveMaximum
    else if (x <= minimum) minimum
    else {
      val p = precision
      val value2 = approximate(x, p)
      val numDivisions = StrictMath.floor((value2 - minimum) / increment)
      val roundedDown = minimum + (numDivisions * increment)
      val roundedUp = minimum + ((numDivisions + 1) * increment)
      if (approximate(roundedUp, p) <= approximate(effectiveMaximum, p)) {
        if (StrictMath.abs(value2 - roundedDown) < StrictMath.abs(roundedUp - value2))
          roundedDown
        else
          roundedUp
      }
      else effectiveMaximum
    }
  }
}
