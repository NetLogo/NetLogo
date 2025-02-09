// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ JLabel, JPanel, SwingConstants }
import java.awt.{ Color, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.image.BufferedImage

import org.nlogo.api.Editable
import org.nlogo.core.{ I18N, Pen => CorePen, Plot => CorePlot }
import org.nlogo.plot.{ PlotManagerInterface, PlotLoader, PlotPen, Plot }
import org.nlogo.swing.{ RoundedBorderPanel, VTextIcon }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ WidgetRemovedEvent, AfterLoadEvent, WidgetErrorEvent }

abstract class AbstractPlotWidget(val plot:Plot, val plotManager: PlotManagerInterface)
        extends Widget with Editable with Plot.DirtyListener with
                org.nlogo.window.Events.AfterLoadEvent.Handler with
                org.nlogo.window.Events.WidgetRemovedEvent.Handler with
                org.nlogo.window.Events.CompiledEvent.Handler {

  type WidgetModel = CorePlot

  import AbstractPlotWidget._

  private class CanvasPanel(canvas: PlotCanvas) extends JPanel with RoundedBorderPanel with ThemeSync {
    setLayout(new GridBagLayout)

    locally {
      val c = new GridBagConstraints

      c.weightx = 1
      c.weighty = 1
      c.fill = GridBagConstraints.BOTH
      c.insets = new Insets(3, 3, 3, 3)

      add(canvas, c)
    }

    override def paintComponent(g: Graphics) {
      setDiameter(6 * zoomFactor)

      super.paintComponent(g)
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(Color.WHITE)
      setBorderColor(InterfaceColors.PLOT_BORDER)
    }
  }

  private var fullyConstructed = false
  plot.dirtyListener = Some(this)
  val canvas = new PlotCanvas(plot)
  private val canvasPanel = new CanvasPanel(canvas)
  private val legend = new PlotLegend(plot)
  private val nameLabel = new JLabel(I18N.gui.get("edit.plot.previewName"))
  private val xAxis = new XAxisLabels()
  private val yAxis = new YAxisLabels()

  displayName = plot.name

  plot.clear() // set current values to defaults

  setLayout(new GridBagLayout)

  locally {
    val c = new GridBagConstraints

    //ROW1
    //-----------------------------------------
    c.insets =
      if (preserveWidgetSizes)
        new Insets(3, 6, 6, 6)
      else
        new Insets(6, 12, 6, 12)

    c.gridx = 0
    c.gridy = 0
    c.gridwidth = GridBagConstraints.REMAINDER
    c.anchor = GridBagConstraints.CENTER

    add(nameLabel, c)

    nameLabel.setText(plot.name)

    //ROW2
    //-----------------------------------------
    c.insets = new Insets(0, 3, 3, 3)

    c.gridx = GridBagConstraints.RELATIVE
    c.gridy = 1
    c.gridwidth = 1
    c.gridheight = 1
    c.weighty = 3.0
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.VERTICAL

    add(yAxis, c)

    c.gridwidth = GridBagConstraints.RELATIVE
    c.weightx = 3.0
    c.anchor = GridBagConstraints.CENTER
    c.fill = GridBagConstraints.BOTH

    add(canvasPanel, c)

    //ROW3
    //-----------------------------------------
    c.insets = new Insets(0, 3, 3, 3)
    c.gridy = 2

    c.weightx = 0.0
    c.weighty = 0.0
    c.gridwidth = 1
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.NONE

    add(new JLabel, c)

    c.gridwidth = GridBagConstraints.RELATIVE
    c.anchor = GridBagConstraints.CENTER
    c.fill = GridBagConstraints.HORIZONTAL

    add(xAxis, c)

    c.weightx = 0.0
    c.weighty = 0.0
    c.gridwidth = 1
    c.gridheight = 1
    c.anchor = GridBagConstraints.EAST
    c.fill = GridBagConstraints.NONE

    add(new JLabel, c)

    //ROW4
    //-----------------------------------------

    c.gridx = 0
    c.gridy = 3
    c.gridwidth = GridBagConstraints.REMAINDER
    c.weightx = 1
    c.anchor = GridBagConstraints.CENTER
    c.insets = new Insets(0, 0, 0, 0)

    add(legend, c)

    // make sure to update the gui components in case
    // something changed underneath ev 8/26/08
    refreshGUI()
  }

  override def paintComponent(g: Graphics) = {
    setBackgroundColor(InterfaceColors.PLOT_BACKGROUND)

    recolor()

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
  def makeDirty(){
    // yuck! plot calls makeDirty when its being constructed.
    // but canvas isnt created yet.
    if(fullyConstructed) canvas.makeDirty()
  }
  override def helpLink = Some("programming.html#plotting")
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
  override def getPreferredSize = AbstractPlotWidget.PREF_SIZE
  override def getMaximumSize: Dimension = null

  override def syncTheme(): Unit = {
    canvasPanel.syncTheme()
  }

  def savePens(s: StringBuilder){
    import org.nlogo.api.StringUtils.escapeString
    for (pen <- plot.pens; if (!pen.temporary)) {
      s.append("\"" + escapeString(pen.name) + "\" " +
              pen.defaultInterval + " " + pen.defaultMode + " " +
              pen.defaultColor + " " + pen.inLegend + " " + pen.saveString + "\n")
    }
  }

  override def load(corePlot: WidgetModel): Object = {
    setSize(corePlot.width, corePlot.height)
    xLabel(corePlot.xAxis.optionToPotentiallyEmptyString)
    yLabel(corePlot.yAxis.optionToPotentiallyEmptyString)
    legend.open = corePlot.legendOn
    PlotLoader.loadPlot(corePlot, plot)
    plotName(plot.name)
    clear()
    this
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds

    val displayName = plotName.potentiallyEmptyStringToOption
    val savedXLabel = xLabel.potentiallyEmptyStringToOption
    val savedYLabel = yLabel.potentiallyEmptyStringToOption

    val pens =
      for (pen <- plot.pens; if (!pen.temporary))
        yield CorePen(display = pen.name, pen.defaultInterval,
          pen.defaultMode, color = pen.defaultColor, inLegend = pen.inLegend,
          pen.setupCode, pen.updateCode)

    CorePlot(displayName,
      x = b.x, y = b.y, width = b.width, height = b.height,
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
    nameLabel.setForeground(if (anyErrors) InterfaceColors.WIDGET_TEXT_ERROR else InterfaceColors.WIDGET_TEXT)

    if (error("setupCode") != null)
      new WidgetErrorEvent(this, error("setupCode")).raise(this)
    else if (error("updateCode") != null)
      new WidgetErrorEvent(this, error("updateCode")).raise(this)
    else
      new WidgetErrorEvent(this, null).raise(this)
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

  override def invalidSettings: Seq[(String, String)] = {
    val hasDuplicatedName =
      findWidgetContainer.allWidgets.collect {
        case p: CorePlot if p.display.map(_.toUpperCase).getOrElse("") == plotName.toUpperCase => p
      }.length > 1
    if (hasDuplicatedName) Seq("plotName" -> I18N.gui.getN("edit.plot.name.duplicate", plotName.toUpperCase))
    else                   Seq.empty[(String, String)]
  }

  override def editFinished: Boolean = {
    super.editFinished
    plotManager.compilePlot(plot)
    nameLabel.setText(plot.name)
    xAxis.setLabel(_xAxisLabel)
    yAxis.setLabel(_yAxisLabel)
    recolor()
    clear()
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
    private val label: JLabel = new JLabel("", SwingConstants.CENTER)
    private val max: JLabel = new JLabel()

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
    c.gridwidth = GridBagConstraints.REMAINDER
    c.weightx = 0.0
    c.anchor = java.awt.GridBagConstraints.EAST
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(max, c)
    add(max)

    override def paintComponent(g: Graphics) = {
      setBackground(InterfaceColors.PLOT_BACKGROUND)

      min.setForeground(InterfaceColors.WIDGET_TEXT)
      label.setForeground(InterfaceColors.WIDGET_TEXT)
      max.setForeground(InterfaceColors.WIDGET_TEXT)

      label.setToolTipText(
        if (label.getPreferredSize.width > label.getSize().width) getLabel else null)

      super.paintComponent(g)
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

    label.setIcon(labelIcon)
    val gridbag: GridBagLayout = new GridBagLayout
    setLayout(gridbag)
    val c: GridBagConstraints = new GridBagConstraints
    c.insets = new Insets(3, 0, 0, 0)
    c.gridwidth = GridBagConstraints.REMAINDER
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
    c.gridheight = GridBagConstraints.REMAINDER
    c.weighty = 0.0
    c.fill = java.awt.GridBagConstraints.NONE
    gridbag.setConstraints(min, c)
    add(min)

    override def paintComponent(g: Graphics) = {
      setBackground(InterfaceColors.PLOT_BACKGROUND)

      min.setForeground(InterfaceColors.WIDGET_TEXT)
      label.setForeground(InterfaceColors.WIDGET_TEXT)
      max.setForeground(InterfaceColors.WIDGET_TEXT)

      if (label.getPreferredSize.width > label.getWidth)
        label.setToolTipText(label.getText)
      else
        label.setToolTipText(null)

      super.paintComponent(g)
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
