// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel
import org.nlogo.swing.VTextIcon

import org.nlogo.api.{I18N, Editable}
import org.nlogo.plot.{PlotManagerInterface, PlotLoader, PlotPen, Plot}

import java.awt.GridBagConstraints.REMAINDER
import java.awt.{List=>AWTList, _}
import image.BufferedImage
import Events.{WidgetRemovedEvent, AfterLoadEvent}

abstract class AbstractPlotWidget(val plot:Plot, val plotManager: PlotManagerInterface)
        extends Widget with Editable with
                Events.AfterLoadEventHandler with
                Events.WidgetRemovedEventHandler with
                Events.CompiledEventHandler {

  import AbstractPlotWidget._

  private var fullyConstructed = false

  val gui = new PlotWidgetGUI(plot, this)

  locally {
    displayName = plot.name

    setBorder(widgetBorder)
    setOpaque(true)
    // this is needed because the PlotLegend is going to use us to
    // get a font - ST 9/2/04
    // since the PlotLegend is added and removed from the widget
    // when it is shown or hidden, the usual way of just letting Zoomer
    // zoom the font size won't work, hence the fontSource stuff in
    // PlotLegend - ST 2/22/06
    org.nlogo.awt.Fonts.adjustDefaultFont(this)

    setBackground(InterfaceColors.PLOT_BACKGROUND)
    plot.clear() // set current values to defaults
    gui.addToPanel(this)
  }

  /// satisfy the usual obligations of top-level widgets
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.plot")
  override def needsPreferredWidthFudgeFactor = false
  override def zoomSubcomponents = true
  override def helpLink = Some("docs/programming.html#plotting")
  def propertySet = Properties.plot
  def showLegend = gui.legend.open
  def showLegend(open: Boolean) { gui.legend.open = open }

  /// some stuff relating to plot pen editing
  def editPlotPens: List[PlotPen] = plot.pens
  def editPlotPens(pens: List[PlotPen]){
    if(! (plot.pens eq pens)) plot.pens = pens
  }

  ///
  def togglePenList(){ gui.legend.toggle }
  def clear(){ plot.clear; gui.legend.refresh }

  /// these exist to support editing
  def plotName = plot.name
  def plotName(name: String){
    plot.name(name)
    displayName = plot.name
    gui.nameLabel.setText(name)
  }

  private var _xAxisLabel: String = ""
  def xLabel = gui.xAxis.getLabel
  def xLabel(label: String){
    _xAxisLabel = label
    gui.xAxis.setLabel(_xAxisLabel)
  }

  private var _yAxisLabel: String = ""
  def yLabel = gui.yAxis.getLabel
  def yLabel(label: String){
    _yAxisLabel = label
    gui.yAxis.setLabel(_yAxisLabel)
  }

  def setupCode = plot.setupCode
  def setupCode(setupCode: String){ plot.setupCode=setupCode }

  def updateCode = plot.updateCode
  def updateCode(updateCode: String){ plot.updateCode=updateCode }

  def defaultXMin = plot.defaultState.xMin
  def defaultXMin(defaultXMin: Double) {
    plot.defaultState = plot.defaultState.copy(xMin = defaultXMin) }

  def defaultXMax = plot.defaultState.xMax
  def defaultXMax(defaultXMax: Double) {
    plot.defaultState = plot.defaultState.copy(xMax = defaultXMax) }

  def defaultYMin = plot.defaultState.yMin
  def defaultYMin(defaultYMin: Double) {
    plot.defaultState = plot.defaultState.copy(yMin = defaultYMin) }

  def defaultYMax = plot.defaultState.yMax
  def defaultYMax(defaultYMax: Double) {
    plot.defaultState = plot.defaultState.copy(yMax = defaultYMax) }

  def defaultAutoPlotOn = plot.defaultState.autoPlotOn
  def defaultAutoPlotOn(defaultAutoPlotOn: Boolean) {
    plot.defaultState = plot.defaultState.copy(autoPlotOn = defaultAutoPlotOn) }

  /// sizing
  override def getMinimumSize = AbstractPlotWidget.MIN_SIZE
  override def getPreferredSize(font: Font) = AbstractPlotWidget.PREF_SIZE
  override def getMaximumSize: Dimension = null

  /// saving and loading
  override def save: String = {
    val s: StringBuilder = new StringBuilder
    s.append("PLOT\n")
    s.append(getBoundsString)
    s.append((if (null != plotName && plotName.trim != "") plotName else "NIL") + "\n")
    s.append((if (null != xLabel && xLabel.trim != "") xLabel else "NIL") + "\n")
    s.append((if (null != yLabel && yLabel.trim != "") yLabel else "NIL") + "\n")
    s.append(plot.defaultState.xMin + "\n")
    s.append(plot.defaultState.xMax + "\n")
    s.append(plot.defaultState.yMin + "\n")
    s.append(plot.defaultState.yMax + "\n")
    s.append(plot.defaultState.autoPlotOn + "\n")
    s.append(gui.legend.open + "\n")
    s.append(plot.saveString + "\n")
    s.append("PENS\n")
    savePens(s)
    s.toString
  }

  def savePens(s: StringBuilder){
    import org.nlogo.api.StringUtils.escapeString
    for (pen <- plot.pens; if (!pen.temporary)) {
      s.append("\"" + escapeString(pen.name) + "\" " +
              pen.defaultState.interval + " " + pen.defaultState.mode + " " +
              pen.defaultState.color + " " + pen.inLegend + " " + pen.saveString + "\n")
    }
  }

  def load(strings: Seq[String], helper: Widget.LoadHelper): Object = {
    val List(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt).toList
    setSize(x2 - x1, y2 - y1)
    if (strings.length > 7) {
      xLabel(if (strings(6) == "NIL") "" else strings(6))
      yLabel(if (strings(7) == "NIL") "" else strings(7))
    }
    if (strings.length > 13) { gui.legend.open=strings(13).toBoolean }
    PlotLoader.parsePlot(strings.toArray, plot, helper.convert(_, false))
    plotName(plot.name)
    clear()
    this
  }

  /// exporting an image of the plot
  def exportGraphics: BufferedImage = {
    val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    paint(image.getGraphics)
    image
  }

  private def recolor() {
    gui.nameLabel.setForeground(if(anyErrors) java.awt.Color.RED else java.awt.Color.BLACK)
  }

  def handle(e: AfterLoadEvent){
    plotManager.compilePlot(plot)
    recolor()
  }

  def handle(e: WidgetRemovedEvent){ if(e.widget == this){ plotManager.forgetPlot(plot) } }

  def handle(e: Events.CompiledEvent){
    if(e.sourceOwner.isInstanceOf[ProceduresInterface]){
      plotManager.compilePlot(plot)
      recolor()
    }
  }

  // error handling
  def anyErrors: Boolean = plotManager.hasErrors(plot)
  def removeAllErrors() = throw new UnsupportedOperationException
  def error(key: Object): Exception = (key match {
    case "setupCode" => plotManager.getPlotSetupError(plot)
    case "updateCode" => plotManager.getPlotUpdateError(plot)
  }).orNull
  def error(key: Object, e: Exception) { throw new UnsupportedOperationException }

  override def editFinished: Boolean = {
    super.editFinished
    plotManager.compilePlot(plot)
    gui.nameLabel.setText(plot.name)
    gui.xAxis.setLabel(_xAxisLabel)
    gui.yAxis.setLabel(_yAxisLabel)
    recolor()
    gui.legend.refresh
    true
  }

  fullyConstructed = true
}

object AbstractPlotWidget {
  /// sizing
  val MIN_SIZE = new Dimension(160, 120)
  val PREF_SIZE = new Dimension(200, 150)
}
