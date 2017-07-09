// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

class TickCounter {
  private var _ticks = -1.0  // sentinel value for uninitialized counter
  def ticks = _ticks
  def ticks_=(ticks: Double) {
    // only ever set this in one place, for easier debugging/logging - ST 3/4/10
    _ticks = ticks
  }
  def tick(amount: Double = 1) { ticks_=(ticks + amount) }
  def clear() { ticks_=(-1) }
  def reset() { ticks_=(0) }
}
