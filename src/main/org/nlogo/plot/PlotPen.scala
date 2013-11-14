// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import collection.mutable.Buffer
import org.nlogo.api.{ PlotPenState, PlotPenInterface }

class PlotPen(
  val temporary: Boolean,
  var name: String,
  var setupCode: String = "",
  var updateCode: String = "",
  var inLegend: Boolean = true,
  var defaultState: PlotPenState = PlotPenState())
extends PlotPenInterface {

  override def toString = "PlotPen(" + name + ")"

  var state = defaultState
  var points: Vector[PlotPoint] = Vector()

  hardReset()

  def setupCode(code: String) { setupCode = if(code == null) "" else code }
  def updateCode(code: String) { updateCode = if(code == null) "" else code }
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

  // these are package-private because they don't trigger autoscaling.
  // note that we add the point even if the pen is up; this may
  // seem useless but it simplifies the painting logic - ST 2/23/06

  private[plot] def plot(y: Double) {
    if (points.nonEmpty)
      state = state.copy(x = state.x + state.interval)
    points :+= PlotPoint(state.x, y, state.isDown, state.color)
  }

  private[plot] def plot(x: Double, y: Double) {
    state = state.copy(x = x)
    points :+= PlotPoint(x, y, state.isDown, state.color)
  }

  override def clone: PlotPen = {
    val newPlotPen =
      new PlotPen(temporary, name, setupCode, updateCode, inLegend, defaultState)
    newPlotPen.state = state
    newPlotPen.points = points
    newPlotPen
  }

}
