// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import collection.mutable.Buffer

import java.io.{ Serializable => JSerializable }

import org.nlogo.core.{ I18N, PlotPenState, PlotPenInterface }


object PlotPen {
  // modes (all static)
  // These are integers, not an enum, because modelers actually use
  // these numbers to refer to the modes in their NetLogo yAxisCode.
  // (Why we didn't use strings, I'm not sure.) - ST 3/21/08
  val LINE_MODE = PlotPenInterface.LineMode
  val BAR_MODE = PlotPenInterface.BarMode
  val POINT_MODE = PlotPenInterface.PointMode
  def isValidPlotPenMode(mode: Int) = mode >= 0 && mode <= 2
}

// ideally we'd have "val plot" and "val temporary", not vars, but we need to be able
// to assign them in readObject().  (It seems to me there must be a way to do the
// serialization that doesn't infect everything with "var", but I'm not going to
// tackle it right now.) - ST 3/1/10
@SerialVersionUID(0)
class PlotPen (
        var plot: Plot,
        var name: String,
        var temporary: Boolean,
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
extends org.nlogo.core.PlotPenInterface with JSerializable {

  private var _runtimeError: Option[Exception] = None

  hardReset()

  plot.addPen(this)

  override def toString = "PlotPen("+name+", "+plot+")"

  override def state: PlotPenState =
    PlotPenState(x, _color, _interval, _mode, _isDown, _hidden)

  override def state_=(s: PlotPenState): Unit = {
    x = s.x
    color = s.color
    interval = s.interval
    mode = s.mode
    isDown = s.isDown
    hidden = s.hidden
  }

  var points: Buffer[PlotPoint] = Buffer()

  def color = _color
  def color_=(newColor: Int) {
    if(_color != newColor) {
      _color = newColor
      plot.pensDirty = true
      plot.plotListener.foreach(_.setPenColor(newColor))
    }
  }

  def interval = _interval
  def interval_=(newInterval: Double) {
    _interval = newInterval
    plot.plotListener.foreach(_.setInterval(newInterval))
  }

  def hidden = _hidden
  def hidden_=(newIsHidden: Boolean) {
    if(_hidden != newIsHidden) {
      _hidden = newIsHidden
      plot.pensDirty = true
    }
  }

  def isDown = _isDown
  def isDown_=(newIsDown: Boolean) {
    _isDown = newIsDown
    plot.plotListener.foreach(_.penDown(newIsDown))
  }

  def mode = _mode
  def mode_=(newMode: Int) {
    if( mode != newMode ) {
      penModeChanged = true
      _mode = newMode
      plot.makeDirty() // forces redrawing immediately. closes ticket #1004. JC - 6/7/10
      plot.plotListener.foreach(_.plotPenMode(newMode))
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

  // move this out of hard reset because sometimes hardReset is used as part of a multi-step process
  // and we don't want this to happen until the end ev 1/18/07
  def plotListenerReset(hardReset: Boolean) {
    plot.plotListener.foreach(_.resetPen(hardReset))
  }

  def softReset() {
    x = 0.0
    isDown = true
    points = Buffer()
    runtimeError = None
  }

  def plot(y: Double) {
    if (points.nonEmpty) x += interval
    plot(x, y)
  }

  def plot(x: Double, y: Double) {
    this.x = x
    // note that we add the point even if the pen is up; this may
    // seem useless but it simplifies the painting logic - ST 2/23/06
    points += PlotPoint(x, y, isDown, color)
    if (isDown) plot.perhapsGrowRanges(this, x, y)
    plot.plotListener.foreach(_.plot(x, y))
  }

  def plot(x: Double, y: Double, color: Int, isDown: Boolean) {
    points += PlotPoint(x, y, isDown, color)
  }

  // serialization is for HubNet plot mirroring

  @throws(classOf[java.io.IOException])
  private def writeObject(out: java.io.ObjectOutputStream) {
    out.writeObject(name)
    out.writeBoolean(temporary)
    out.writeDouble(x)
    out.writeInt(color)
    out.writeObject(points)
    out.writeDouble(interval)
    out.writeBoolean(isDown)
    out.writeInt(mode)
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in:java.io.ObjectInputStream) {
    name = in.readObject().asInstanceOf[String]
    temporary = in.readBoolean()
    x = in.readDouble()
    _color = in.readInt()
    points = readPointList(in)
    _interval = in.readDouble()
    _isDown = in.readBoolean()
    _mode = in.readInt()
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[ClassNotFoundException])
  def readPointList(in:java.io.ObjectInputStream) =
    in.readObject().asInstanceOf[Buffer[PlotPoint]]

  def runtimeError: Option[Exception] = _runtimeError
  def runtimeError_=(e: Option[Exception]): Unit = {
    _runtimeError = e
  }
}
