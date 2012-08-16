// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import collection.mutable.Buffer
import org.nlogo.api.I18N

object PlotPen {
  // modes (all static)
  // These are integers, not an enum, because modelers actually use
  // these numbers to refer to the modes in their NetLogo yAxisCode.
  // (Why we didn't use strings, I'm not sure.) - ST 3/21/08
  val LINE_MODE = 0
  val BAR_MODE = 1
  val POINT_MODE = 2
  def isValidPlotPenMode(mode: Int) = mode >= 0 && mode <= 2
}

class PlotPen (
        val plot: Plot,
        var name: String,
        val temporary: Boolean,
        var setupCode: String,
        var updateCode: String,
        var x: Double = 0.0,
        var defaultColor: Int = java.awt.Color.BLACK.getRGB,
        private var _color: Int = java.awt.Color.BLACK.getRGB(),
        var inLegend: Boolean = true,
        var defaultInterval: Double = 1.0,
        private var _interval: Double = 1.0,
        var defaultMode: Int = PlotPen.LINE_MODE,
        private var _mode:Int = PlotPen.LINE_MODE,
        var penModeChanged: Boolean = false,
        private var _isDown: Boolean = true,
        private var _hidden: Boolean = false)
extends org.nlogo.api.PlotPenInterface {

  hardReset()
  plot.addPen(this)
  override def toString = "PlotPen("+name+", "+plot+")"

  var points: Vector[PlotPoint] = Vector()

  def color = _color
  def color_=(newColor: Int) {
    if(_color != newColor) {
      _color = newColor
    }
  }

  def interval = _interval
  def interval_=(newInterval: Double) {
    _interval = newInterval
  }

  def hidden = _hidden
  def hidden_=(newIsHidden: Boolean) {
    if(_hidden != newIsHidden) {
      _hidden = newIsHidden
    }
  }

  def isDown = _isDown
  def isDown_=(newIsDown: Boolean) {
    _isDown = newIsDown
  }

  def mode = _mode
  def mode_=(newMode: Int) {
    if( mode != newMode ) {
      penModeChanged = true
      _mode = newMode
    }
  }

  def setupCode(code:String) { setupCode = if(code == null) "" else code }
  def updateCode(code:String) { updateCode = if(code == null) "" else code }
  def saveString = {
    import org.nlogo.api.StringUtils.escapeString
    "\"" + escapeString(setupCode.trim) + "\"" + " " + "\"" + escapeString(updateCode.trim) + "\""
  }

  /// actual functionality
  def hardReset() {
    softReset()
    // temporary pens don't have defaults, so there's no difference between a soft and hard reset
    // for a temporary pen - ST 2/24/06
    if (!temporary) {
      color = defaultColor
      mode = defaultMode
      interval = defaultInterval
    }
  }

  def softReset() {
    x = 0.0
    isDown = true
    points = Vector()
  }

  def plot(y: Double) {
    if (points.nonEmpty) x += interval
    plot(x, y)
  }

  def plot(x: Double, y: Double) {
    this.x = x
    // note that we add the point even if the pen is up; this may
    // seem useless but it simplifies the painting logic - ST 2/23/06
    points :+= PlotPoint(x, y, isDown, color)
    if (isDown) plot.perhapsGrowRanges(this, x, y)
  }

  def plot(x: Double, y: Double, color: Int, isDown: Boolean) {
    points :+= PlotPoint(x, y, isDown, color)
  }

}
