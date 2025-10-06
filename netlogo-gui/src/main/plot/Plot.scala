// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import java.io.{ IOException, ObjectInputStream, ObjectOutputStream, Serializable => JSerializable }

import org.nlogo.api.{ Color, PlotAction, PlotInterface, PlotListener, PlotPenInterface, PlotState }
import org.nlogo.core.ColorConstants.White

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

  private var penListeners = Set[PenListener]()

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

  override def setPlotListener(plotListener:PlotListener): Unit ={
    if(plotListener == null) sys.error("null plotListener")
    this.plotListener = Some(plotListener)
  }

  override def removePlotListener(): Unit ={ this.plotListener = None }

  def addPenListener(listener: PenListener): Unit = {
    penListeners += listener
  }

  def name(newName:String): Unit ={ name = newName }

  def makeDirty(): Unit ={ dirtyListener.foreach{_.makeDirty() }}
  def pens: List[PlotPen] = _pens
  def pens_=(pens:List[PlotPen]): Unit ={
    _pens = pens
    currentPen = _pens.headOption
  }

  def addPen(p:PlotPen) = {
    pens = _pens :+ p
    pensDirty = true
    penListeners.foreach(_.penAdded())
  }

  // take the first pen if there is no current pen set
  override def currentPen: Option[PlotPen] = _currentPen.orElse(_pens.headOption)
  def currentPen_=(p: PlotPen): Unit = currentPen=(if(p==null) None else Some(p))
  def currentPen_=(p: Option[PlotPen]): Unit = {
    this._currentPen = p
     // TODO this line must be cleaned up when we fix up hubnet plotting. the .get here is bad. JC - 6/2/10
    plotListener.foreach(_.currentPen(p.get.name))
  }

  def currentPenByName: String = currentPen.map(_.name).getOrElse(null)
  def currentPenByName_=(penName: String): Unit = { currentPen=(getPen(penName)) }
  def getPen(penName: String): Option[PlotPen] = _pens.find(_.name.toLowerCase==penName.toLowerCase)
  def defaultXMin = _defaultXMin
  def defaultXMin_=(defaultXMin: Double): Unit ={
    _defaultXMin = defaultXMin
    plotListener.foreach(_.defaultXMin(defaultXMin))
  }

  def defaultXMax = _defaultXMax
  def defaultXMax_=(defaultXMax: Double): Unit ={
    _defaultXMax = defaultXMax
    plotListener.foreach(_.defaultXMax(defaultXMax))
  }

  def defaultYMin = _defaultYMin
  def defaultYMin_=(defaultYMin: Double): Unit = {
    _defaultYMin = defaultYMin
    plotListener.foreach(_.defaultYMin(defaultYMin))
  }

  def defaultYMax = _defaultYMax
  def defaultYMax_=(defaultYMax: Double): Unit ={
    _defaultYMax = defaultYMax
    plotListener.foreach(_.defaultYMax(defaultYMax))
  }

  def defaultAutoPlotX = _defaultAutoPlotX
  def defaultAutoPlotX_=(defaultAutoPlotX: Boolean): Unit ={
    _defaultAutoPlotX = defaultAutoPlotX
    plotListener.foreach(_.defaultAutoPlotX(defaultAutoPlotX))
  }

  def defaultAutoPlotY = _defaultAutoPlotY
  def defaultAutoPlotY_=(defaultAutoPlotY: Boolean): Unit ={
    _defaultAutoPlotY = defaultAutoPlotY
    plotListener.foreach(_.defaultAutoPlotY(defaultAutoPlotY))
  }

  /// current properties
  /// (will be copied from defaults at construction time - ST 2/28/06)

  def autoPlotX = state.autoPlotX

  def autoPlotY = state.autoPlotY

  override def xMin = state.xMin

  override def xMax = state.xMax

  override def yMin = state.yMin

  override def yMax = state.yMax

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

  override def plot(x: Double, y: Double): Unit = {
    currentPen.foreach { p =>
      plot(p, x, y)
      makeDirty()
    }
  }

  def plot(pen: PlotPen, y: Double): Unit = {
    pen.plot(y)
    if (pen.state.isDown)
      perhapsGrowRanges(pen, pen.state.x, y)
  }

  def plot(pen: PlotPen, x: Double, y: Double): Unit = {
    pen.plot(x, y)
    if (pen.state.isDown)
      perhapsGrowRanges(pen, x, y)
  }

  def clear(): Unit = {
    pens = _pens.filterNot(_.temporary)
    currentPen = _pens.headOption
    _pens.foreach(_.hardReset())
    state = PlotState(defaultAutoPlotX, defaultAutoPlotY, defaultXMin, defaultXMax, defaultYMin, defaultYMax)
    runtimeError = None
    backgroundColor = Color.getARGBbyPremodulatedColorNumber(White)
    makeDirty()
    plotListener.foreach(_.clear())
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

      // calling growRangeX() twice is sometimes redundant,
      // but it's the easiest way to ensure that both the
      // left and right edges of the bar become visible
      // (consider the case where the bar is causing the
      // min to decrease) - ST 2/23/06
      growRangeX(x)
    }

    if (autoPlotY)
      growRangeY(y)
  }

  private def growRangeX(x: Double): Unit = {
    if (x > xMax)
      state = state.copy(xMax = PlotHelper.expandRange(xMin, xMax, x))

    if (x < xMin)
      state = state.copy(xMin = PlotHelper.expandRange(xMin, xMax, x))
  }

  private def growRangeY(y: Double): Unit = {
    if (y > yMax)
      state = state.copy(yMax = PlotHelper.expandRange(yMin, yMax, y))

    if (y < yMin)
      state = state.copy(yMin = PlotHelper.expandRange(yMin, yMax, y))
  }

  /// histograms
  def setHistogramNumBars(pen: PlotPen, numBars: Int): Unit = {
    pen.interval = (xMax - xMin) / numBars
    plotListener.foreach(_.setHistogramNumBars(numBars))
  }

  var histogram: Option[Histogram] = None

  def beginHistogram(pen:PlotPen): Unit = {
    histogram = Some(new Histogram(xMin, xMax, pen.interval))
  }

  def beginHistogram(pen:PlotPen, bars:Array[Int]): Unit ={
    histogram = Some(new Histogram(xMin, pen.interval, bars))
  }

  def nextHistogramValue(value:Double) = histogram.get.nextValue(value)

  // this leaves the pen down, regardless of its previous state
  // histogram cannot be None when entering this method, or boom. - Josh 11/2/09
  def endHistogram(pen: PlotPen): Unit = {
    pen.softReset()

    if (autoPlotY) {
      // note that we never grow the x range, only the y range,
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

  override def clone = {
    val newPlot = new Plot(name)
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

  @throws(classOf[IOException])
  private def writeObject(out: ObjectOutputStream): Unit = {
    out.writeObject(name)
    out.writeBoolean(autoPlotX)
    out.writeBoolean(autoPlotY)
    out.writeDouble(xMin)
    out.writeDouble(xMax)
    out.writeDouble(yMin)
    out.writeDouble(yMax)
    out.writeObject(pens)
    out.writeObject(currentPen.map(_.name))
  }

  @throws(classOf[IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in: ObjectInputStream): Unit = {
    name = in.readObject().toString
    val autoPlotX = in.readBoolean
    val autoPlotY = in.readBoolean
    val xMin = in.readDouble
    val xMax = in.readDouble
    val yMin = in.readDouble
    val yMax = in.readDouble
    state = PlotState(autoPlotX, autoPlotY, xMin, xMax, yMin, yMax)
    plotListener = None
    pens = in.readObject.asInstanceOf[List[PlotPen]]
    val currentPenName = in.readObject.asInstanceOf[Option[String]]
    currentPenName.foreach{ name => _currentPen = pens.find(_.name == name) }
    histogram = None
    pens.foreach(_.plot = this)
  }
}

object Plot {
  trait DirtyListener {
    def makeDirty(): Unit
  }
}

// allows GUI plot to notify its parent widget when a pen is added so it
// can repaint its legend (Isaac B 7/13/25)
trait PenListener {
  def penAdded(): Unit
}
