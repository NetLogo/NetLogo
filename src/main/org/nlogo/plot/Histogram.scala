// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

// this could even go in org.nlogo.util, it's just abstract logic, nothing to do with the specifics
// of plotting in NetLogo - ST 2/28/06

class Histogram(xMin: Double, interval: Double, val bars: Array[Int]) {
  def this(xMin: Double, xMax: Double, interval: Double) =
    this(xMin, interval, Array.fill(StrictMath.floor((xMax - xMin) / interval).toInt)(0))
  private var _ceiling = 0
  def ceiling = _ceiling
  def nextValue(value: Double) {
    val bar = StrictMath.floor(((value - xMin) / interval)
                               // the division might produce a result like 5.99999999999997,
                               // putting something in bar 5 that should be in bar 6,  so... - ST
                               // 7/28/05 instead of just adding we have to multiply as the error
                               // in floating point math becomes more severe as the numbers get
                               // larger. ev 7/28/05
                               * (1 + 3.2e-15)).toInt
    if(bars.isDefinedAt(bar)) {
      bars(bar) += 1
      _ceiling = _ceiling max bars(bar)
    }
  }
}
