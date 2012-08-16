// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import collection.mutable.Buffer
import org.nlogo.api.{ I18N, PlotPenState }

class PlotPen(
  val plot: Plot,
  val temporary: Boolean,
  var name: String,
  var setupCode: String = "",
  var updateCode: String = "",
  var inLegend: Boolean = true,
  var defaultState: PlotPenState = PlotPenState())
extends org.nlogo.api.PlotPenInterface {

  override def toString = "PlotPen("+name+", "+plot+")"

  var state = defaultState
  var points: Vector[PlotPoint] = Vector()

  hardReset()
  plot.addPen(this)

  def setupCode(code:String) { setupCode = if(code == null) "" else code }
  def updateCode(code:String) { updateCode = if(code == null) "" else code }
  def saveString = {
    import org.nlogo.api.StringUtils.escapeString
    "\"" + escapeString(setupCode.trim) + "\"" + " " + "\"" + escapeString(updateCode.trim) + "\""
  }

  def hardReset() {
    if (temporary)
      softReset()
    else {
      state = defaultState
      points = Vector()
    }
  }

  def softReset() {
    state = state.copy(
      x = 0.0,
      isDown = true)
    points = Vector()
  }

  def plot(y: Double) {
    if (points.nonEmpty)
      state = state.copy(x = state.x + state.interval)
    plot(state.x, y)
  }

  def plot(x: Double, y: Double) {
    state = state.copy(x = x)
    // note that we add the point even if the pen is up; this may
    // seem useless but it simplifies the painting logic - ST 2/23/06
    points :+= PlotPoint(x, y, state.isDown, state.color)
    if (state.isDown)
      plot.perhapsGrowRanges(this, x, y)
  }

  def plot(x: Double, y: Double, color: Int, isDown: Boolean) {
    points :+= PlotPoint(x, y, state.isDown, state.color)
  }

}
