// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.{ PlotAction, PlotInterface, PlotPenInterface, PlotState }
import PlotAction.{ PlotXY, SoftResetPen }
import org.nlogo.core.PlotPenInterface
import scala.collection.immutable
import scala.collection.immutable.VectorBuilder
import scala.math.{ log10, pow }

// normally, to create a new Plot, you have to go through PlotManager.newPlot
// this makes sense because the PlotManager then controls compilation
// and running of code, and it needs to know about all the Plots.
// but having an accessible constructor is nice for tests.
// JC - 12/20/10, ST 8/16/12
// Also used by the clone method, which is itself used for model runs,
// and by ModelRun.Frame.apply when starting a new run
// NP 2012-12-17
class Plot(var name: String, var defaultState: PlotState = PlotState())
extends PlotInterface {

  var state = defaultState
  var dirty = true

  override def toString = "Plot(" + name + ")"

  def name(newName:String){ name = newName }

  var _pens = List[PlotPen]()
  def pens = _pens
  def pens_=(pens:List[PlotPen]){
    _pens = pens
    currentPen = pens.headOption
  }

  def addPen(p:PlotPen){
    pens = pens :+ p
  }

  private var _currentPen: Option[PlotPen] = None
  // take the first pen if there is no current pen set
  def currentPen: Option[PlotPen] = _currentPen.orElse(pens.headOption)
  def currentPen_=(p: PlotPen): Unit = currentPen=(if(p==null) None else Some(p))
  def currentPen_=(p: Option[PlotPen]): Unit = {
    this._currentPen = p
  }
  def currentPenByName: String = currentPen.map(_.name).getOrElse(null)
  def currentPenByName_=(penName: String): Unit = { currentPen=(getPen(penName)) }
  def getPen(penName: String): Option[PlotPen] = pens.find(_.name.toLowerCase==penName.toLowerCase)

  // This only affects the UI, not headless operation, but because it is included when a plot is
  // exported, we keep it here rather than in PlotWidget, so that exporting can stay totally
  // headless - ST 3/9/06
  var legendIsOpen = false

  /// current properties
  /// (will be copied from defaults at construction time - ST 2/28/06)

  var setupCode: String = ""
  var updateCode:String = ""

  var runtimeError: Option[Exception] = None

  def saveString = {
    import org.nlogo.core.StringEscaper.escapeString
    "\"" + escapeString(setupCode.trim) + "\"" + " " + "\"" + escapeString(updateCode.trim) + "\""
  }

  /// clearing
  clear() // finally after all fields have been initialized, clear. unsure why...

  def clear() {
    pens = pens.filterNot(_.temporary)
    currentPen = pens.headOption
    pens.foreach(_.hardReset())
    state = defaultState
  }

  def createPlotPen(name: String, temporary: Boolean = false, setupCode: String = "", updateCode: String = ""): PlotPen = {
    val pen = new PlotPen(temporary, name, setupCode, updateCode)
    addPen(pen)
    pen
  }

  override def plot(y: Double) {
    currentPen.foreach(plot(_, y))
  }

  def plot(pen: PlotPen, y: Double) {
    pen.plot(y)
    if (pen.state.isDown)
      perhapsGrowRanges(pen, pen.state.x, y)
  }

  override def plot(x: Double, y: Double) {
    currentPen.foreach(plot(_, x, y))
  }

  def plot(pen: PlotPen, x: Double, y: Double) {
    pen.plot(x, y)
    if (pen.state.isDown)
      perhapsGrowRanges(pen, x, y)
  }

  def perhapsGrowRanges(pen: PlotPen, x: Double, y: Double){
    if(state.autoPlotOn){
      if(pen.state.mode == PlotPenInterface.BarMode){
        // allow extra room on the right for bar
        growRanges(x + pen.state.interval, y)
      }
      // calling growRanges() twice is sometimes redundant,
      // but it's the easiest way to ensure that both the
      // left and right edges of the bar become visible
      // (consider the case where the bar is causing the
      // min to decrease) - ST 2/23/06
      growRanges(x, y)
    }
  }

  private def prettyRange(range: Double): Double = {
    if (range < 0) {
      val tmag = pow(10, log10(-range).floor - 1) * 2

      (range / tmag).floor * tmag
    }

    else {
      val tmag = pow(10, log10(range).floor - 1) * 2

      (range / tmag).ceil * tmag
    }
  }

  def growRanges(x: Double, y: Double) {
    if (x > state.xMax)
      state = state.copy(xMax = prettyRange(x))
    if (x < xMin)
      state = state.copy(xMin = prettyRange(x))
    if (y > yMax)
      state = state.copy(yMax = prettyRange(y))
    if (y < yMin)
      state = state.copy(yMin = prettyRange(y))
  }

  def histogramActions(pen: PlotPenInterface, values: Seq[Double]): immutable.Seq[PlotAction] = {
    val histogram = new Histogram(state.xMin, state.xMax, pen.state.interval)
    values.foreach(histogram.nextValue)
    val actions = new VectorBuilder[PlotAction]
    actions += SoftResetPen(this.name, pen.name)
    if (state.autoPlotOn)
      // note that we pass extraRoom as false; we know the exact height
      // of the histogram so there's no point in leaving any extra empty
      // space like we normally do when growing the ranges;
      // note also that we never grow the x range, only the y range,
      // because it's the current x range that determined the extent
      // of the histogram in the first place - ST 2/23/06
      growRanges(state.xMin, histogram.ceiling)
    actions ++= (for {
      (barHeight, barNumber) <- histogram.bars.zipWithIndex
      // there is a design decision here not to generate points corresponding to empty bins.  not
      // sure what the right thing is in general, but in the GasLab models we use the histogram
      // command three times to produce a histogram with three different bar colors, and it looks
      // funny in that model if the bars we aren't histogramming have a horizontal line along the
      // axis - ST 2/24/06
      if (barHeight > 0)
      // compute the x coordinates by multiplication instead of repeated adding so that floating
      // point error doesn't accumulate - ST 2/23/06
      x = state.xMin + barNumber * pen.state.interval
    } yield PlotXY(this.name, pen.name, x, barHeight))
    actions.result
  }

  override def clone = {
    val newPlot = new Plot(name, defaultState)
    newPlot.state = state
    newPlot._pens = _pens.map(_.clone)
    newPlot._currentPen = currentPen.flatMap { p =>
      newPlot._pens.find(_.name == p.name)
    }
    newPlot.legendIsOpen = legendIsOpen
    newPlot.setupCode = setupCode
    newPlot.updateCode = updateCode
    // newPlot.dirty will be true by default, which is fine
    newPlot
  }

}
