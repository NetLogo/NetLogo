// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import
  org.nlogo.core.{ Color, ColorConstants },
    ColorConstants.White

import
  org.nlogo.api.{ PlotAction, PlotInterface, PlotPenInterface, PlotState }

import
  java.io.{ Serializable => JSerializable }

import scala.math.{ log10, pow }

// normally, to create a new Plot, you have to go through PlotManager.newPlot
// this makes sense because the PlotManager then controls compilation
// and running of code, and it needs to know about all the Plots.
// however, when using the HubNetClient editor, you don't want a plot to
// go into the PlotManager, so we have to allow it to just create a new plot.
// its also nice for tests as well though.
// JC - 12/20/10
@SerialVersionUID(0)
class Plot private[nlogo] (var name:String) extends PlotInterface with JSerializable {

  var state = PlotState()

  // this is kind of terrible, but its for
  // AbstractPlotWidget (plot.dirtyListener = Some(this))
  // JC - 12/20/10
  var dirtyListener: Option[Plot.DirtyListener] = None

  var plotListener: Option[PlotListener] = None

  var _pens = List[PlotPen]()
  var pensDirty = false

  private var _currentPen: Option[PlotPen] = None

  // This only affects the UI, not headless operation, but because it is included when a plot is
  // exported, we keep it here rather than in PlotWidget, so that exporting can stay totally
  // headless - ST 3/9/06
  var legendIsOpen = false

  /// default properties
  private var _defaultXMin = 0.0
  private var _defaultXMax = 10.0
  private var _defaultYMin = 0.0
  private var _defaultYMax = 10.0
  private var _defaultAutoPlotX = true
  private var _defaultAutoPlotY = true
  var setupCode: String = ""
  var updateCode: String = ""

  var runtimeError: Option[Exception] = None

  // NOTE: This almost certainly ought to be a field of `api.PlotState`
  // However, I would prefer a better datatype than `Int` and don't want to add it to
  // the API until I'm sure it's right.
  var backgroundColor: Int = Color.getARGBbyPremodulatedColorNumber(White)

  /// clearing
  clear() // finally after all fields have been initialized, clear. unsure why...

  override def toString = "Plot(" + name + ")"

  def setPlotListener(plotListener:PlotListener){
    if(plotListener == null) sys.error("null plotListener")
    this.plotListener = Some(plotListener)
  }
  def removePlotListener(){ this.plotListener = None }

  def name(newName:String){ name = newName }

  def makeDirty(){ dirtyListener.foreach{_.makeDirty() }}
  def pens = _pens
  def pens_=(pens:List[PlotPen]){
    _pens = pens
    currentPen = pens.headOption
  }

  def addPen(p:PlotPen) = {
    pens = pens :+ p
    pensDirty = true
  }

  // take the first pen if there is no current pen set
  override def currentPen: Option[PlotPen] = _currentPen.orElse(pens.headOption)
  def currentPen_=(p: PlotPen): Unit = currentPen=(if(p==null) None else Some(p))
  def currentPen_=(p: Option[PlotPen]): Unit = {
    this._currentPen = p
     // TODO this line must be cleaned up when we fix up hubnet plotting. the .get here is bad. JC - 6/2/10
    plotListener.foreach(_.currentPen(p.get.name))
  }

  def currentPenByName: String = currentPen.map(_.name).getOrElse(null)
  def currentPenByName_=(penName: String): Unit = { currentPen=(getPen(penName)) }
  def getPen(penName: String): Option[PlotPen] = pens.find(_.name.toLowerCase==penName.toLowerCase)
  def defaultXMin = _defaultXMin
  def defaultXMin_=(defaultXMin: Double){
    _defaultXMin = defaultXMin
    plotListener.foreach(_.defaultXMin(defaultXMin))
  }

  def defaultXMax = _defaultXMax
  def defaultXMax_=(defaultXMax: Double){
    _defaultXMax = defaultXMax
    plotListener.foreach(_.defaultXMax(defaultXMax))
  }

  def defaultYMin = _defaultYMin
  def defaultYMin_=(defaultYMin: Double) {
    _defaultYMin = defaultYMin
    plotListener.foreach(_.defaultYMin(defaultYMin))
  }

  def defaultYMax = _defaultYMax
  def defaultYMax_=(defaultYMax: Double){
    _defaultYMax = defaultYMax
    plotListener.foreach(_.defaultYMax(defaultYMax))
  }

  def defaultAutoPlotX = _defaultAutoPlotX
  def defaultAutoPlotX_=(defaultAutoPlotX: Boolean){
    _defaultAutoPlotX = defaultAutoPlotX
    plotListener.foreach(_.defaultAutoPlotX(defaultAutoPlotX))
  }

  def defaultAutoPlotY = _defaultAutoPlotY
  def defaultAutoPlotY_=(defaultAutoPlotY: Boolean){
    _defaultAutoPlotY = defaultAutoPlotY
    plotListener.foreach(_.defaultAutoPlotY(defaultAutoPlotY))
  }

  /// current properties
  /// (will be copied from defaults at construction time - ST 2/28/06)

  def autoPlotX = state.autoPlotX

  def autoPlotY = state.autoPlotY

  def xMin = state.xMin

  def xMax = state.xMax

  def yMin = state.yMin

  def yMax = state.yMax

  def saveString = {
    import org.nlogo.api.StringUtils.escapeString
    "\"" + escapeString(setupCode.trim) + "\"" + " " + "\"" + escapeString(updateCode.trim) + "\""
  }

  override def plot(y: Double): Unit = {
    currentPen.foreach { p =>
      plot(p, y)
      makeDirty()
    }
  }

  override def plot(x: Double, y: Double) {
    currentPen.foreach { p =>
      plot(p, x, y)
      makeDirty()
    }
  }

  def plot(pen: PlotPen, y: Double) {
    pen.plot(y)
    if (pen.state.isDown)
      perhapsGrowRanges(pen, pen.state.x, y)
  }

  def plot(pen: PlotPen, x: Double, y: Double) {
    pen.plot(x, y)
    if (pen.state.isDown)
      perhapsGrowRanges(pen, x, y)
  }

  def clear() {
    pens = pens.filterNot(_.temporary)
    currentPen = pens.headOption
    pens.foreach(_.hardReset())
    state = PlotState(defaultAutoPlotX, defaultAutoPlotY, defaultXMin, defaultXMax, defaultYMin, defaultYMax)
    runtimeError = None
    backgroundColor = Color.getARGBbyPremodulatedColorNumber(White)
    makeDirty()
    plotListener.foreach(_.clear)
    pensDirty = true
  }

  def createPlotPen(name: String, temporary: Boolean): PlotPen = {
    this.createPlotPen(name, temporary, "", "")
  }

  def createPlotPen(name: String, temporary: Boolean, setupCode: String, updateCode: String): PlotPen = {
    new PlotPen(this, name, temporary, setupCode, updateCode)
  }

  // NOTE: this is for conformance to a shared api with netlogo-headless.
  // At some point, this and client code should be changed to use this method properly.
  def histogramActions(pen: PlotPenInterface, values: Seq[Double]): scala.collection.immutable.Seq[PlotAction] = ???

  def perhapsGrowRanges(pen: PlotPen, x: Double, y: Double): Unit = {
    if (autoPlotX) {
      if (pen.mode == PlotPen.BAR_MODE) {
        // allow extra room on the right for bar
        growRangeX(x + pen.interval)
      }

      // calling growRanges() twice is sometimes redundant,
      // but it's the easiest way to ensure that both the
      // left and right edges of the bar become visible
      // (consider the case where the bar is causing the
      // min to decrease) - ST 2/23/06
      growRangeX(x)
    }

    if (autoPlotY)
      growRangeY(y)
  }

  private def prettyRange(range: Double): Double = {
    if (range < 0) {
      if (range > -1)
        return -1

      val tmag = pow(10, log10(-range).floor - 1) * 2

      (range / tmag).floor * tmag
    } else {
      if (range < 1)
        return 1

      val tmag = pow(10, log10(range).floor - 1) * 2

      (range / tmag).ceil * tmag
    }
  }

  private def growRangeX(x: Double): Unit = {
    if (x > xMax)
      state = state.copy(xMax = prettyRange(x))

    if (x < xMin)
      state = state.copy(xMin = prettyRange(x))
  }

  private def growRangeY(y: Double): Unit = {
    if (y > yMax)
      state = state.copy(yMax = prettyRange(y))

    if (y < yMin)
      state = state.copy(yMin = prettyRange(y))
  }

  /// histograms
  def setHistogramNumBars(pen: PlotPen, numBars: Int) {
    pen.interval = (xMax - xMin) / numBars
    plotListener.foreach(_.setHistogramNumBars(numBars))
  }

  var histogram: Option[Histogram] = None

  def beginHistogram(pen:PlotPen) {
    histogram = Some(new Histogram(xMin, xMax, pen.interval))
  }

  def beginHistogram(pen:PlotPen, bars:Array[Int]){
    histogram = Some(new Histogram(xMin, pen.interval, bars))
  }

  def nextHistogramValue(value:Double) = histogram.get.nextValue(value)

  // this leaves the pen down, regardless of its previous state
  // historgram cannot be None when entering this method, or boom. - Josh 11/2/09
  def endHistogram(pen: PlotPen): Unit = {
    pen.softReset()

    if (autoPlotY) {
      // note that we pass extraRoom as false; we know the exact height
      // of the histogram so there's no point in leaving any extra empty
      // space like we normally do when growing the ranges;
      // note also that we never grow the x range, only the y range,
      // because it's the current x range that determined the extent
      // of the histogram in the first place - ST 2/23/06
      growRangeY(histogram.get.ceiling)
    }

    for((bar, barNumber) <- histogram.get.bars.zipWithIndex) {
      // there is a design decision here not to generate points corresponding to empty bins.  not
      // sure what the right thing is in general, but in the GasLab models we use the histogram
      // command three times to produce a histogram with three different bar colors, and it looks
      // funny in that model if the bars we aren't histogramming have a horizontal line along the
      // axis - ST 2/24/06
      if (bar > 0)
        // compute the x coordinates by multiplication instead of repeated adding so that floating
        // point error doesn't accumulate - ST 2/23/06
        pen.plot(xMin + barNumber * pen.interval, bar)
    }

    histogram = None
  }
}

object Plot {
  trait DirtyListener {
    def makeDirty(): Unit
  }
}
