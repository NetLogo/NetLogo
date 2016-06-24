// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel
import org.nlogo.swing.VTextIcon

import org.nlogo.api.Editable
import org.nlogo.core.I18N
import org.nlogo.core.{ Pen => CorePen, Plot => CorePlot }
import org.nlogo.core.model.WidgetReader
import org.nlogo.plot.{PlotManagerInterface, PlotLoader, PlotPen, Plot}

import java.awt.GridBagConstraints.REMAINDER
import java.awt.{List=>AWTList, _}
import image.BufferedImage
import org.nlogo.window.Events.{WidgetRemovedEvent, AfterLoadEvent}

abstract class AbstractPlotWidget(val plot:Plot, val plotManager: PlotManagerInterface)
        extends Widget with Editable with Plot.DirtyListener with
                org.nlogo.window.Events.AfterLoadEvent.Handler with
                org.nlogo.window.Events.WidgetRemovedEvent.Handler with
                org.nlogo.window.Events.CompiledEvent.Handler {

  type WidgetModel = CorePlot

  import AbstractPlotWidget._

  private var fullyConstructed = false
  plot.dirtyListener = Some(this)
  val canvas = new PlotCanvas(plot)
  private val legend = new PlotLegend(plot, this)
  private val nameLabel = new JLabel("", javax.swing.SwingConstants.CENTER)
  private val xAxis = new XAxisLabels()
  private val yAxis = new YAxisLabels()

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

    val gridbag = new java.awt.GridBagLayout()
    setLayout(gridbag)

    val c = new java.awt.GridBagConstraints()
    c.gridwidth = 1
    c.gridheight = 1
    c.weightx = 0.0
    c.weighty = 0.0
    c.fill = java.awt.GridBagConstraints.NONE

    //ROW1
    //-----------------------------------------
    c.insets = new java.awt.Insets(0, 1, 1, 1)

    c.gridx = 1
    c.gridy = 0
    c.gridwidth = 1
    c.anchor = java.awt.GridBagConstraints.CENTER
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    gridbag.setConstraints(nameLabel, c)
    add(nameLabel)
    org.nlogo.awt.Fonts.adjustDefaultFont(nameLabel)
    nameLabel.setFont(nameLabel.getFont().deriveFont(java.awt.Font.BOLD))
    nameLabel.setText(plot.name)

    //ROW2
    //-----------------------------------------
    c.insets = new java.awt.Insets(0, 1, 0, 1)

    c.gridx = java.awt.GridBagConstraints.RELATIVE
    c.gridy = 1
    c.gridwidth = 1
    c.gridheight = java.awt.GridBagConstraints.RELATIVE
    c.weighty = 3.0
    c.anchor = java.awt.GridBagConstraints.WEST
    c.fill = java.awt.GridBagConstraints.VERTICAL
    gridbag.setConstraints(yAxis, c)
    add(yAxis);

    c.gridwidth = java.awt.GridBagConstraints.RELATIVE
    c.weightx = 3.0
    c.anchor = java.awt.GridBagConstraints.CENTER
    c.fill = java.awt.GridBagConstraints.BOTH
    gridbag.setConstraints(canvas, c)
    add(canvas)

    c.gridwidth = REMAINDER
    c.weightx = 0.0
    c.anchor = java.awt.GridBagConstraints.NORTH
    c.fill = java.awt.GridBagConstraints.NONE
    c.insets = new java.awt.Insets(0, 3, 0, 1)
    gridbag.setConstraints(legend, c)
    add(legend)

    //ROW3
    //-----------------------------------------
    c.insets = new java.awt.Insets(0, 0, 0, 0)
    c.gridy = 2

    val filler2 = new javax.swing.JLabel()
    c.weightx = 0.0
    c.weighty = 0.0
    c.gridwidth = 1
    c.gridheight = 1
    c.anchor = java.awt.GridBagConstraints.WEST
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(filler2, c)
    add(filler2)

    c.gridwidth = java.awt.GridBagConstraints.RELATIVE
    c.anchor = java.awt.GridBagConstraints.CENTER
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    gridbag.setConstraints(xAxis, c)
    add(xAxis)

    val filler3 = new javax.swing.JLabel()
    c.weightx = 0.0
    c.weighty = 0.0
    c.gridwidth = 1
    c.gridheight = 1
    c.anchor = java.awt.GridBagConstraints.EAST
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(filler3, c)
    add(filler3)

    // make sure to update the gui components in case
    // something changed underneath ev 8/26/08
    refreshGUI()
  }

  override def paintComponent(g: Graphics) = {
    super.paintComponent(g)
    nameLabel.setToolTipText(
      if (nameLabel.getPreferredSize.width > nameLabel.getSize().width) plotName else null)
  }

  def refreshGUI() {
    def getLabel(d:Double) = if(d.toString.endsWith(".0")) d.toString.dropRight(2) else d.toString
    xAxis.setMin(getLabel(plot.xMin))
    xAxis.setMax(getLabel(plot.xMax))
    yAxis.setMin(getLabel(plot.yMin))
    yAxis.setMax(getLabel(plot.yMax))
    if(plot.pensDirty) {
      legend.refresh()
      plot.pensDirty = false
    }
  }

  /// satisfy the usual obligations of top-level widgets
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.plot")
  override def needsPreferredWidthFudgeFactor = false
  override def zoomSubcomponents = true
  def makeDirty(){
    // yuck! plot calls makeDirty when its being constructed.
    // but canvas isnt created yet.
    if(fullyConstructed) canvas.makeDirty()
  }
  override def helpLink = Some("docs/programming.html#plotting")
  def propertySet = Properties.plot
  def showLegend = legend.open
  def showLegend(open: Boolean){ legend.open=open }

  def runtimeError: Option[Exception] = plot.runtimeError
  def runtimeError(e: Option[Exception]): Unit = {
    plot.runtimeError = e
  }

  /// some stuff relating to plot pen editing
  def editPlotPens: List[PlotPen] = plot.pens
  def editPlotPens(pens: List[PlotPen]){
    if(! (plot.pens eq pens)) plot.pens = pens
  }

  ///
  def togglePenList(){ legend.toggle }
  def clear(){ plot.clear; legend.refresh }

  /// these exist to support editing
  def plotName = plot.name
  def plotName(name: String){
    plot.name(name)
    displayName = plot.name
    nameLabel.setText(name)
  }

  private var _xAxisLabel: String = ""
  def xLabel = xAxis.getLabel
  def xLabel(label: String){
    _xAxisLabel = label
    xAxis.setLabel(_xAxisLabel)
  }

  private var _yAxisLabel: String = ""
  def yLabel = yAxis.getLabel
  def yLabel(label: String){
    _yAxisLabel = label
    yAxis.setLabel(_yAxisLabel)
  }

  def setupCode = plot.setupCode
  def setupCode(setupCode: String){ plot.setupCode=setupCode }

  def updateCode = plot.updateCode
  def updateCode(updateCode: String){ plot.updateCode=updateCode }

  def defaultXMin = plot.defaultXMin
  def defaultXMin(defaultXMin: Double){ plot.defaultXMin=defaultXMin }

  def defaultYMin = plot.defaultYMin
  def defaultYMin(defaultYMin: Double){ plot.defaultYMin=defaultYMin }

  def defaultXMax = plot.defaultXMax
  def defaultXMax(defaultXMax: Double){ plot.defaultXMax=defaultXMax }

  def defaultYMax = plot.defaultYMax
  def defaultYMax(defaultYMax: Double){ plot.defaultYMax=defaultYMax }

  def defaultAutoPlotOn = plot.defaultAutoPlotOn
  def defaultAutoPlotOn(defaultAutoPlotOn: Boolean){ plot.defaultAutoPlotOn=defaultAutoPlotOn }

  /// sizing
  override def getMinimumSize = AbstractPlotWidget.MIN_SIZE
  override def getPreferredSize(font: Font) = AbstractPlotWidget.PREF_SIZE
  override def getMaximumSize: Dimension = null

  def savePens(s: StringBuilder){
    import org.nlogo.api.StringUtils.escapeString
    for (pen <- plot.pens; if (!pen.temporary)) {
      s.append("\"" + escapeString(pen.name) + "\" " +
              pen.defaultInterval + " " + pen.defaultMode + " " +
              pen.defaultColor + " " + pen.inLegend + " " + pen.saveString + "\n")
    }
  }

  override def load(corePlot: WidgetModel): Object = {
    setSize(corePlot.right - corePlot.left, corePlot.bottom - corePlot.top)
    xLabel(corePlot.xAxis.optionToPotentiallyEmptyString)
    yLabel(corePlot.yAxis.optionToPotentiallyEmptyString)
    legend.open = corePlot.legendOn
    PlotLoader.loadPlot(corePlot, plot)
    plotName(plot.name)
    clear()
    this
  }

  override def model: WidgetModel = {
    val b = getBoundsTuple

    val displayName = plotName.potentiallyEmptyStringToOption
    val savedXLabel = xLabel.potentiallyEmptyStringToOption
    val savedYLabel = yLabel.potentiallyEmptyStringToOption

    val pens =
      for (pen <- plot.pens; if (!pen.temporary))
        yield CorePen(display = pen.name, pen.defaultInterval,
          pen.defaultMode, color = pen.defaultColor, inLegend = pen.inLegend,
          pen.setupCode, pen.updateCode)

    CorePlot(displayName,
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      xAxis = savedXLabel, yAxis = savedYLabel,
      xmin = plot.defaultXMin, xmax = plot.defaultXMax,
      ymin = plot.defaultYMin, ymax = plot.defaultYMax,
      autoPlotOn = plot.defaultAutoPlotOn, legendOn = legend.open,
      setupCode = plot.setupCode, updateCode = plot.updateCode,
      pens = pens.toList)
  }

  /// exporting an image of the plot
  def exportGraphics: BufferedImage = {
    val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    paint(image.getGraphics)
    image
  }

  protected def recolor() {
    nameLabel.setForeground(if(anyErrors) java.awt.Color.RED else java.awt.Color.BLACK)
  }

  def handle(e: AfterLoadEvent){
    plotManager.compilePlot(plot)
    recolor()
  }

  def handle(e: WidgetRemovedEvent){ if(e.widget == this){ plotManager.forgetPlot(plot) } }

  def handle(e:org.nlogo.window.Events.CompiledEvent){
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
    nameLabel.setText(plot.name)
    xAxis.setLabel(_xAxisLabel)
    yAxis.setLabel(_yAxisLabel)
    recolor()
    legend.refresh
    true
  }

  fullyConstructed = true
}

object AbstractPlotWidget {
  /// sizing
  val MIN_SIZE = new Dimension(160, 120)
  val PREF_SIZE = new Dimension(200, 150)

  class XAxisLabels extends javax.swing.JPanel {
    private val min: JLabel = new JLabel()
    private val label: JLabel = new JLabel("", javax.swing.SwingConstants.CENTER)
    private val max: JLabel = new JLabel()

    setBackground(InterfaceColors.PLOT_BACKGROUND)
    val gridbag: GridBagLayout = new GridBagLayout
    setLayout(gridbag)
    val c: GridBagConstraints = new GridBagConstraints
    c.insets = new Insets(0, 0, 0, 3)
    c.gridheight = 1
    c.weighty = 0.0
    c.fill = java.awt.GridBagConstraints.NONE
    c.gridwidth = 1
    c.weightx = 0.0
    c.anchor = java.awt.GridBagConstraints.WEST
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(min, c)
    add(min)
    c.weightx = 100.0
    c.anchor = java.awt.GridBagConstraints.CENTER
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    gridbag.setConstraints(label, c)
    add(label)
    c.gridwidth = REMAINDER
    c.weightx = 0.0
    c.anchor = java.awt.GridBagConstraints.EAST
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(max, c)
    add(max)
    org.nlogo.awt.Fonts.adjustDefaultFont(min)
    org.nlogo.awt.Fonts.adjustDefaultFont(label)
    org.nlogo.awt.Fonts.adjustDefaultFont(max)

    override def paintComponent(g: Graphics) = {
      super.paintComponent(g)
      label.setToolTipText(
        if (label.getPreferredSize.width > label.getSize().width) getLabel else null)
    }

    def setLabel(text: String) = label.setText(text)
    def setMax(text: String) = max.setText(text)
    def setMin(text: String) = min.setText(text)
    def getLabel = label.getText
  }

  class YAxisLabels extends javax.swing.JPanel {
    private val label: JLabel = new JLabel()
    private var labelText: String = ""
    private val max: JLabel = new JLabel()
    private val labelIcon: VTextIcon = new VTextIcon(label, "", org.nlogo.swing.VTextIcon.ROTATE_LEFT)
    private val min: JLabel = new JLabel()

    setBackground(InterfaceColors.PLOT_BACKGROUND)
    label.setIcon(labelIcon)
    val gridbag: GridBagLayout = new GridBagLayout
    setLayout(gridbag)
    val c: GridBagConstraints = new GridBagConstraints
    c.insets = new Insets(3, 0, 0, 0)
    c.gridwidth = REMAINDER
    c.gridheight = 1
    c.weightx = 1.0
    c.weighty = 0.0
    c.anchor = java.awt.GridBagConstraints.EAST
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(max, c)
    add(max)
    c.weighty = 100.0
    c.fill = java.awt.GridBagConstraints.VERTICAL
    gridbag.setConstraints(label, c)
    add(label)
    c.gridheight = REMAINDER
    c.weighty = 0.0
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(min, c)
    add(min)
    org.nlogo.awt.Fonts.adjustDefaultFont(min)
    org.nlogo.awt.Fonts.adjustDefaultFont(label)
    org.nlogo.awt.Fonts.adjustDefaultFont(max)

    override def paintComponent(g: Graphics) = {
      super.paintComponent(g)
      label.setToolTipText(
        if (label.getPreferredSize.height > label.getSize().height) getLabel else null)
    }

    def setMin(text: String) {min.setText(text)}
    def setMax(text: String): Unit = {max.setText(text)}
    def getLabel = labelText
    def setLabel(text: String) {
      labelText = text
      labelIcon.setLabel(labelText)
      label.repaint()
    }
  }

}
