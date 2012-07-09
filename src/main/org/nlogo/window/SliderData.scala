// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.ConstantSliderConstraint
import org.nlogo.agent.SliderConstraint
import org.nlogo.api.Approximate.approximate

class SliderData(var minimum:Double = 0, var maximum:Double=100, var increment: Double = 1) {

  private var _value = 50d
  def value = _value
  def value_=(value: Double) {valueSetter(value)}
  def value_=(value: Double, buttonRelease: Boolean) {valueSetter(value)}
  def valueSetter(v: Double): Boolean = {
    if (v!=_value) {
      this._value = v
      true
    } else false
  }

  // The slider constraint defines the bounds of the slider.  We
  // have local copies of the minimum/maximum/increment values so we
  // don't run the thunks for a DynamicSliderConstraint every time
  // we repaint.  To set or modify the constraints, you use
  // setSliderConstraint, passing in a SliderConstraint object.
  var constraint: SliderConstraint = new ConstantSliderConstraint(minimum, maximum, increment)
  setSliderConstraint(constraint)

  // constraint runtime errors intentionally not handled here.
  // slider widgets can handle them, because we don't know what to do here.
  def setSliderConstraint(con: SliderConstraint): Boolean = {
    this.constraint = con
    // If the values change, coerce the value to the new range
    val (newmin,newmax,newinc) = (con.minimum, con.maximum, con.increment)
    if (newmin != minimum || newmax != maximum || newinc != increment) {
      minimum = newmin
      maximum = newmax
      increment = newinc
      // re-coerce the existing value, makes sure its within bounds
      value = coerceValue(value)
      true
    } else false
  }

  /// calculations helpers

  // if the increment doesn't evenly divide the difference between min and max,
  // then the effective maximum is the largest value less than max that
  // makes the division come out even
  def effectiveMaximum: Double = {
    if (minimum >= maximum) minimum
    else if (increment == 0) minimum
    else {
      var result = minimum + (increment * StrictMath.floor((maximum - minimum) / increment))
      // Sometimes rounding error leaves us off by one here, for example if min is 0.1,
      // increment 0.1, max 50, then result is 49.900000000000006 instead of 50 like it
      // should be.  So we explicitly test for this off by one error here. - ST 9/17/02
      if (approximate(result + increment, precision) <= maximum) result += increment
      approximate(result, precision)
    }
  }

  def precision: Int = {
    def precisionHelper(n: Double): Int = {
      val (sWithoutE, eValue) = {
        val s = n.toString
        var place: Int = s.indexOf('E')
        if (place == -1) (s,0)
        else (s.substring(0,place), s.substring(place + 1).toInt)
      }
      // get the number of digits after the dot.
      val digitsAfterDot = {
        val sAfterDot = sWithoutE.substring(sWithoutE.indexOf('.') + 1)
        // whack the trailing zeros and count the digits
        sAfterDot.reverse.dropWhile(_ == '0').length
      }
      // if the E value is greater than the number of digits after the .
      // ex: 1.23E4 = 12300
      // then the precision is simply 0 (can't be negative)
      // but if the digits after the . are more than E
      // ex: 1.23456E2 = 123.456
      // then the precision is 3 (because of .456)
      math.max(0, (digitsAfterDot - eValue))
    }
    math.max(precisionHelper(minimum), precisionHelper(increment))
  }

  def coerceValue(value: Double): Double = {
    // When coercing, we honor the effective maximum, which is the
    // max multiple of the increment value which is equal to or
    // less than the specified maximum.  --CLB
    val p = precision
    val newVal =
      if (value > effectiveMaximum) effectiveMaximum
      else if (value <= minimum) minimum
      else {
        val value2 = approximate(value, p)
        val numDivisions = StrictMath.floor((value2 - minimum) / increment)
        val roundedDown = minimum + (numDivisions * increment)
        val roundedUp = minimum + ((numDivisions + 1) * increment)
        if (approximate(roundedUp, p) <= approximate(effectiveMaximum, p)) {
          if (StrictMath.abs(value2 - roundedDown) < StrictMath.abs(roundedUp - value2)) roundedDown
          else roundedUp
        }
        else effectiveMaximum
      }
    approximate(newVal, p)
  }

  override def toString = "Slider(min=" + minimum + ", max=" + maximum + ", inc=" + increment +")"
}
