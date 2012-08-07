// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.PlotInterface

// normally, to create a new Plot, you have to go through PlotManager.newPlot
// this makes sense because the PlotManager then controls compilation
// and running of code, and it needs to know about all the Plots.
// however, when using the HubNetClient editor, you don't want a plot to
// go into the PlotManager, so we have to allow it to just create a new plot.
// its also nice for tests as well though.
// JC - 12/20/10
@SerialVersionUID(0)
class Plot private[nlogo] (var name:String) extends PlotInterface with Serializable {

  import Plot._

  override def toString = "Plot(" + name + ")"

  // this is kind of terrible, but its for
  // AbstractPlotWidget (plot.dirtyListener = Some(this))
  // JC - 12/20/10
  var dirtyListener: Option[Plot.DirtyListener] = None

  var plotListener: Option[PlotListener] = None
  def setPlotListener(plotListener:PlotListener){
    if(plotListener == null) sys.error("null plotListener")
    this.plotListener = Some(plotListener)
  }
  def removePlotListener(){ this.plotListener = None }

  def name(newName:String){ name = newName }

  def makeDirty(){ dirtyListener.foreach{_.makeDirty() }}

  var _pens = List[PlotPen]()
  def pens = _pens
  def pens_=(pens:List[PlotPen]){
    _pens = pens
    currentPen = pens.headOption
  }
  var pensDirty = false

  def addPen(p:PlotPen){
    pens = pens :+ p
    pensDirty = true
  }

  private var _currentPen: Option[PlotPen] = None
  // take the first pen if there is no current pen set
  def currentPen: Option[PlotPen] = _currentPen.orElse(pens.headOption)
  def currentPen_=(p: PlotPen): Unit = currentPen=(if(p==null) None else Some(p))
  def currentPen_=(p: Option[PlotPen]): Unit = {
    this._currentPen = p
     // TODO this line must be cleaned up when we fix up hubnet plotting. the .get here is bad. JC - 6/2/10
    plotListener.foreach(_.currentPen(p.get.name))
  }
  def currentPen_=(penName: String): Unit = { currentPen=(getPen(penName)) }
  def getPen(penName: String): Option[PlotPen] = pens.find(_.name.toLowerCase==penName.toLowerCase)

  // This only affects the UI, not headless operation, but because it is included when a plot is
  // exported, we keep it here rather than in PlotWidget, so that exporting can stay totally
  // headless - ST 3/9/06
  var legendIsOpen = false

  /// default properties
  private var _defaultXMin = 0.0
  def defaultXMin = _defaultXMin
  def defaultXMin_=(defaultXMin: Double){
    _defaultXMin = defaultXMin
    plotListener.foreach(_.defaultXMin(defaultXMin))
  }

  private var _defaultXMax = 10.0
  def defaultXMax = _defaultXMax
  def defaultXMax_=(defaultXMax: Double){
    _defaultXMax = defaultXMax
    plotListener.foreach(_.defaultXMax(defaultXMax))
  }

  private var _defaultYMin = 0.0
  def defaultYMin = _defaultYMin
  def defaultYMin_=(defaultYMin: Double) {
    _defaultYMin = defaultYMin
    plotListener.foreach(_.defaultYMin(defaultYMin))
  }

  private var _defaultYMax = 10.0
  def defaultYMax = _defaultYMax
  def defaultYMax_=(defaultYMax: Double){
    _defaultYMax = defaultYMax
    plotListener.foreach(_.defaultYMax(defaultYMax))
  }

  private var _defaultAutoPlotOn = true
  def defaultAutoPlotOn = _defaultAutoPlotOn
  def defaultAutoPlotOn_=(defaultAutoPlotOn: Boolean){
    _defaultAutoPlotOn = defaultAutoPlotOn
    plotListener.foreach(_.defaultAutoPlotOn(defaultAutoPlotOn))
  }

  /// current properties
  /// (will be copied from defaults at construction time - ST 2/28/06)

  private var _autoPlotOn = false
  def autoPlotOn = _autoPlotOn
  def autoPlotOn_=(autoPlotOn: Boolean){
    _autoPlotOn = autoPlotOn
    plotListener.foreach(_.autoPlotOn(autoPlotOn))
  }

  private var _xMin = 0.0
  def xMin = _xMin
  def xMin_=(xMin: Double){
    _xMin = xMin
    plotListener.foreach(_.xMin(xMin))
  }

  private var _xMax = 0.0
  def xMax = _xMax
  def xMax_=(xMax: Double){
    _xMax = xMax
    plotListener.foreach(_.xMax(xMax))
  }

  private var _yMin = 0.0
  def yMin = _yMin
  def yMin_=(yMin: Double){
    _yMin = yMin
    plotListener.foreach(_.yMin(yMin))
  }

  private var _yMax = 0.0
  def yMax = _yMax
  def yMax_=(yMax: Double){
    _yMax = yMax
    plotListener.foreach(_.yMax(yMax))
  }
  var setupCode: String = ""
  var updateCode:String = ""

  def saveString = {
    import org.nlogo.api.StringUtils.escapeString
    "\"" + escapeString(setupCode.trim) + "\"" + " " + "\"" + escapeString(updateCode.trim) + "\""
  }

  /// clearing
  clear() // finally after all fields have been initialized, clear. unsure why...

  def clear() {
    pens = pens.filterNot(_.temporary)
    currentPen = pens.headOption
    pens.foreach(_.hardReset())
    xMin=defaultXMin
    xMax=defaultXMax
    yMin=defaultYMin
    yMax=defaultYMax
    autoPlotOn=defaultAutoPlotOn
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

  def perhapsGrowRanges(pen:PlotPen, x:Double, y: Double){
    if(autoPlotOn){
      if(pen.mode == PlotPen.BAR_MODE){
        // allow extra room on the right for bar
        growRanges(x + pen.interval, y, true)
      }
      // calling growRanges() twice is sometimes redundant,
      // but it's the easiest way to ensure that both the
      // left and right edges of the bar become visible
      // (consider the case where the bar is causing the
      // min to decrease) - ST 2/23/06
      growRanges(x, y, true)
    }
  }

  private def growRanges(x:Double, y:Double, extraRoom:Boolean){
    def adjust(d:Double, factor: Double) = d * (if(extraRoom) factor else 1)
    if(x > xMax){
      val newRange = adjust(x - xMin, AUTOPLOT_X_FACTOR)
      xMax=newBound(xMin + newRange, newRange)
    }
    if(x < xMin){
      val newRange = adjust(xMax - x, AUTOPLOT_X_FACTOR)
      xMin=newBound(xMax - newRange, newRange)
    }
    if(y > yMax){
      val newRange = adjust(y - yMin, AUTOPLOT_Y_FACTOR)
      yMax=newBound(yMin + newRange, newRange)
    }
    if(y < yMin){
      val newRange = adjust(yMax - y, AUTOPLOT_Y_FACTOR)
      yMin=newBound(yMax - newRange, newRange)
    }
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
  def endHistogram(pen:PlotPen){
    pen.softReset()
    if(autoPlotOn){
      // note that we pass extraRoom as false; we know the exact height
      // of the histogram so there's no point in leaving any extra empty
      // space like we normally do when growing the ranges;
      // note also that we never grow the x range, only the y range,
      // because it's the current x range that determined the extent
      // of the histogram in the first place - ST 2/23/06
      growRanges(xMin, histogram.get.ceiling, false)
    }
    for((bar, barNumber) <- histogram.get.bars.zipWithIndex) {
      // there is a design decision here not to generate points corresponding to empty bins.  not
      // sure what the right thing is in general, but in the GasLab models we use the histogram
      // command three times to produce a histogram with three different bar colors, and it looks
      // funny in that model if the bars we aren't histogramming have a horizontal line along the
      // axis - ST 2/24/06
      if(bar > 0)
        // compute the x coordinates by multiplication instead of repeated adding so that floating
        // point error doesn't accumulate - ST 2/23/06
        pen.plot(xMin + barNumber * pen.interval, bar)
    }
    histogram = None
  }

  @throws(classOf[java.io.IOException])
  private def writeObject(out:java.io.ObjectOutputStream){
    out.writeObject(name)
    out.writeBoolean(autoPlotOn)
    out.writeDouble(xMin)
    out.writeDouble(xMax)
    out.writeDouble(yMin)
    out.writeDouble(yMax)
    out.writeObject(pens)
    out.writeObject(currentPen.map(_.name))
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in:java.io.ObjectInputStream){
    name = in.readObject().toString
    _autoPlotOn = in.readBoolean()
    _xMin = in.readDouble()
    _xMax = in.readDouble()
    _yMin = in.readDouble()
    _yMax = in.readDouble()
    plotListener = None
    pens = in.readObject().asInstanceOf[List[PlotPen]]
    val currentPenName = in.readObject().asInstanceOf[Option[String]]
    currentPenName.foreach{ name => _currentPen = pens.find(_.name == name) }
    histogram = None
    pens.foreach(_.plot = this)
  }
}

object Plot {

  /// autoplot
  val AUTOPLOT_X_FACTOR = 1.25
  val AUTOPLOT_Y_FACTOR = 1.10

  // The purpose of this is to make the new bounds land on nice
  // numbers like 12.4 instead of long ones like 12.33333333, so
  // that displaying them in the axis labels doesn't use up a lot of
  // screen real estate.  (Thus, the x and y growth factors are only
  // approximate.) - ST 2/23/06
  def newBound(bound:Double, range:Double): Double = {
    val places = 2.0 - StrictMath.floor(StrictMath.log(range) / StrictMath.log(10))
    org.nlogo.api.Approximate.approximate(bound, places.toInt)
  }

  trait DirtyListener {
    def makeDirty(): Unit
  }
}
