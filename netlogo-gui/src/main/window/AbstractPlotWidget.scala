// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.image.BufferedImage
import javax.swing.{ JLabel, JPanel, SwingConstants }

import org.nlogo.core.{ I18N, Pen => CorePen, Plot => CorePlot, Widget => CoreWidget }
import org.nlogo.plot.{ PlotManagerInterface, PlotLoader, PlotPen, Plot }
import org.nlogo.swing.{ RoundedBorderPanel, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ AfterLoadEvent, CompiledEvent, WidgetErrorEvent, WidgetRemovedEvent }

import scala.math.Pi

abstract class AbstractPlotWidget(val plot:Plot, val plotManager: PlotManagerInterface)
  extends Widget with Editable with Plot.DirtyListener
  with AfterLoadEvent.Handler
  with WidgetRemovedEvent.Handler
  with CompiledEvent.Handler {

  import AbstractPlotWidget._

  private class CanvasPanel(canvas: PlotCanvas) extends JPanel with RoundedBorderPanel with ThemeSync {
    setLayout(new GridBagLayout)

    locally {
      val c = new GridBagConstraints

      c.weightx = 1
      c.weighty = 1
      c.fill = GridBagConstraints.BOTH
      c.insets = new Insets(zoom(3), zoom(3), zoom(3), zoom(3))

      add(canvas, c)
    }

    override def paintComponent(g: Graphics): Unit = {
      setDiameter(zoom(6))

      super.paintComponent(g)
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(Color.WHITE)
      setBorderColor(InterfaceColors.plotBorder())
    }
  }

  private var fullyConstructed = false
  plot.dirtyListener = Some(this)
  val canvas = new PlotCanvas(plot)
  private val canvasPanel = new CanvasPanel(canvas)
  private val legend = new PlotLegend(this)
  private val nameLabel = new JLabel(I18N.gui.get("edit.plot.previewName"))
  private val xAxis = new XAxisLabels(this)
  private val yAxis = new YAxisLabels(this)

  displayName = plot.name

  plot.clear() // set current values to defaults

  setLayout(new GridBagLayout)

  initGUI()

  override def initGUI(): Unit = {
    removeAll()

    val c = new GridBagConstraints

    //ROW1
    //-----------------------------------------
    c.insets = {
      if (_oldSize) {
        new Insets(zoom(3), zoom(6), zoom(6), zoom(6))
      } else {
        new Insets(zoom(8), zoom(10), zoom(8), zoom(10))
      }
    }

    c.gridx = 1
    c.gridy = 0
    c.anchor = GridBagConstraints.CENTER

    add(nameLabel, c)

    nameLabel.setText(plot.name)
    nameLabel.setFont(nameLabel.getFont.deriveFont(_boldState))

    //ROW2
    //-----------------------------------------
    c.insets = new Insets(0, zoom(3), zoom(3), zoom(8))

    c.gridx = 0
    c.gridy = 1
    c.fill = GridBagConstraints.VERTICAL

    add(yAxis, c)

    yAxis.setBoldState(_boldState)

    c.gridx = 1
    c.weightx = 1
    c.weighty = 1
    c.fill = GridBagConstraints.BOTH
    c.insets = new Insets(0, 0, zoom(3), zoom(8))

    add(canvasPanel, c)

    //ROW3
    //-----------------------------------------
    c.insets = new Insets(0, zoom(3), 0, zoom(3))

    c.gridy = 2
    c.weightx = 0
    c.weighty = 0
    c.fill = GridBagConstraints.HORIZONTAL

    add(xAxis, c)

    xAxis.setBoldState(_boldState)

    //ROW4
    //-----------------------------------------
    c.gridx = 0
    c.gridy = 3
    c.gridwidth = 2
    c.insets = new Insets(zoom(8), zoom(10), zoom(8), zoom(10))

    add(legend, c)

    legend.setBoldState(_boldState)

    // make sure to update the gui components in case
    // something changed underneath ev 8/26/08
    refreshGUI()
  }

  override def paintComponent(g: Graphics) = {
    setBackgroundColor(InterfaceColors.plotBackground())

    recolor()

    super.paintComponent(g)
    nameLabel.setToolTipText(
      if (nameLabel.getPreferredSize.width > nameLabel.getSize().width) plotName else null)
  }

  def refreshGUI(): Unit = {
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
  def makeDirty(): Unit ={
    // yuck! plot calls makeDirty when its being constructed.
    // but canvas isnt created yet.
    if(fullyConstructed) canvas.makeDirty()
  }
  override def helpLink = Some("programming.html#plotting")

  def showLegend = legend.open
  def setShowLegend(open: Boolean): Unit = { legend.open = open }

  def runtimeError: Option[Exception] = plot.runtimeError
  def setRuntimeError(e: Option[Exception]): Unit = {
    plot.runtimeError = e
  }

  /// some stuff relating to plot pen editing
  def editPlotPens: List[PlotPen] = plot.pens
  def setEditPlotPens(pens: List[PlotPen]): Unit = {
    if(! (plot.pens eq pens)) plot.pens = pens
  }

  ///
  def togglePenList(): Unit ={ legend.toggle() }
  def clear(): Unit ={ plot.clear(); legend.refresh() }

  /// these exist to support editing
  def plotName = plot.name
  def setPlotName(name: String): Unit = {
    plot.name(name)
    displayName = plot.name
    nameLabel.setText(name)
  }

  private var _xAxisLabel: String = ""
  def xLabel = xAxis.getLabel
  def setXLabel(label: String): Unit = {
    _xAxisLabel = label
    xAxis.setLabel(_xAxisLabel)
  }

  private var _yAxisLabel: String = ""
  def yLabel = yAxis.getLabel
  def setYLabel(label: String): Unit = {
    _yAxisLabel = label
    yAxis.setLabel(_yAxisLabel)
  }

  def setupCode = plot.setupCode
  def setSetupCode(setupCode: String): Unit = { plot.setupCode=setupCode }

  def updateCode = plot.updateCode
  def setUpdateCode(updateCode: String): Unit = { plot.updateCode=updateCode }

  def defaultXMin = plot.defaultXMin
  def setDefaultXMin(defaultXMin: Double): Unit = { plot.defaultXMin=defaultXMin }

  def defaultYMin = plot.defaultYMin
  def setDefaultYMin(defaultYMin: Double): Unit = { plot.defaultYMin=defaultYMin }

  def defaultXMax = plot.defaultXMax
  def setDefaultXMax(defaultXMax: Double): Unit = { plot.defaultXMax=defaultXMax }

  def defaultYMax = plot.defaultYMax
  def setDefaultYMax(defaultYMax: Double): Unit = { plot.defaultYMax=defaultYMax }

  def defaultAutoPlotX = plot.defaultAutoPlotX
  def setDefaultAutoPlotX(defaultAutoPlotX: Boolean): Unit = { plot.defaultAutoPlotX = defaultAutoPlotX }

  def defaultAutoPlotY = plot.defaultAutoPlotY
  def setDefaultAutoPlotY(defaultAutoPlotY: Boolean): Unit = { plot.defaultAutoPlotY = defaultAutoPlotY }

  /// sizing
  override def getMinimumSize = AbstractPlotWidget.MIN_SIZE
  override def getPreferredSize = AbstractPlotWidget.PREF_SIZE

  override def setBounds(x: Int, y: Int, width: Int, height: Int): Unit = {
    super.setBounds(x, y, width, height)

    // without this call the legend doesn't wrap correctly after the plot is resized (Isaac B 6/15/25)
    legend.revalidate()
  }

  override def syncTheme(): Unit = {
    canvasPanel.syncTheme()
  }

  def savePens(s: StringBuilder): Unit ={
    import org.nlogo.api.StringUtils.escapeString
    for (pen <- plot.pens; if (!pen.temporary)) {
      s.append("\"" + escapeString(pen.name) + "\" " +
              pen.defaultInterval + " " + pen.defaultMode + " " +
              pen.defaultColor + " " + pen.inLegend + " " + pen.saveString + "\n")
    }
  }

  override def load(model: CoreWidget): Unit = {
    model match {
      case corePlot: CorePlot =>
        oldSize(corePlot.oldSize)
        setSize(corePlot.width, corePlot.height)
        setXLabel(corePlot.xAxis.optionToPotentiallyEmptyString)
        setYLabel(corePlot.yAxis.optionToPotentiallyEmptyString)
        legend.open = corePlot.legendOn
        PlotLoader.loadPlot(corePlot, plot)
        setPlotName(plot.name)
        clear()

      case _ =>
    }
  }

  override def model: CoreWidget = {
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
      oldSize = _oldSize,
      xAxis = savedXLabel, yAxis = savedYLabel,
      xmin = plot.defaultXMin, xmax = plot.defaultXMax,
      ymin = plot.defaultYMin, ymax = plot.defaultYMax,
      autoPlotX = plot.defaultAutoPlotX, autoPlotY = plot.defaultAutoPlotY, legendOn = legend.open,
      setupCode = plot.setupCode, updateCode = plot.updateCode,
      pens = pens.toList)
  }

  /// exporting an image of the plot
  def exportGraphics: BufferedImage = {
    val image = new BufferedImage(getWidth, getHeight, BufferedImage.TYPE_INT_ARGB)
    paint(image.getGraphics)
    image
  }

  protected def recolor(): Unit = {
    nameLabel.setForeground(if (anyErrors) InterfaceColors.widgetTextError() else InterfaceColors.widgetText())

    if (error("setupCode").isDefined)
      new WidgetErrorEvent(this, error("setupCode")).raise(this)
    else if (error("updateCode").isDefined)
      new WidgetErrorEvent(this, error("updateCode")).raise(this)
    else
      new WidgetErrorEvent(this, None).raise(this)
  }

  def handle(e: AfterLoadEvent): Unit ={
    plotManager.compilePlot(plot)
    recolor()
  }

  def handle(e: WidgetRemovedEvent): Unit ={ if(e.widget == this){ plotManager.forgetPlot(plot) } }

  def handle(e:org.nlogo.window.Events.CompiledEvent): Unit ={
    if(e.sourceOwner.isInstanceOf[ProceduresInterface]){
      plotManager.compilePlot(plot)
      recolor()
    }
  }

  // error handling
  def anyErrors: Boolean = plotManager.hasErrors(plot)
  def removeAllErrors() = throw new UnsupportedOperationException
  def error(key: Object): Option[Exception] = {
    if (key == I18N.gui.get("edit.plot.setupCode")) {
      plotManager.getPlotSetupError(plot)
    } else if (key == I18N.gui.get("edit.plot.updateCode")) {
      plotManager.getPlotUpdateError(plot)
    } else {
      None
    }
  }

  def error(key: Object, e: Exception): Unit = { throw new UnsupportedOperationException }

  override def errorString: Option[String] = {
    val hasDuplicatedName =
      findWidgetContainer.allWidgets.collect {
        case p: CorePlot if p.display.map(_.toUpperCase).getOrElse("") == plotName.toUpperCase => p
      }.length > 1
    if (hasDuplicatedName) {
      Some(I18N.gui.getN("edit.plot.name.duplicate", plotName.toUpperCase))
    } else {
      None
    }
  }

  override def editFinished(): Boolean = {
    super.editFinished()
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

  class XAxisLabels(plot: AbstractPlotWidget) extends JPanel {
    private val min: JLabel = new JLabel()
    private val label: JLabel = new JLabel("", SwingConstants.CENTER)
    private val max: JLabel = new JLabel()

    val gridbag: GridBagLayout = new GridBagLayout
    setLayout(gridbag)
    val c: GridBagConstraints = new GridBagConstraints
    c.insets = new Insets(0, 0, 0, plot.zoom(3))
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

    def setBoldState(state: Int): Unit = {
      min.setFont(min.getFont.deriveFont(state))
      label.setFont(label.getFont.deriveFont(state))
      max.setFont(max.getFont.deriveFont(state))
    }

    override def paintComponent(g: Graphics) = {
      setBackground(InterfaceColors.plotBackground())

      min.setForeground(InterfaceColors.widgetText())
      label.setForeground(InterfaceColors.widgetText())
      max.setForeground(InterfaceColors.widgetText())

      label.setToolTipText(
        if (label.getPreferredSize.width > label.getSize().width) getLabel else null)

      super.paintComponent(g)
    }

    def setLabel(text: String) = label.setText(text)
    def setMax(text: String) = max.setText(text)
    def setMin(text: String) = min.setText(text)
    def getLabel = label.getText
  }

  class YAxisLabels(plot: AbstractPlotWidget) extends JPanel {
    private val label = new VerticalLabel
    private val max: JLabel = new JLabel()
    private val min: JLabel = new JLabel()

    val gridbag: GridBagLayout = new GridBagLayout
    setLayout(gridbag)
    val c: GridBagConstraints = new GridBagConstraints
    c.insets = new Insets(plot.zoom(3), 0, 0, 0)
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

    def setBoldState(state: Int): Unit = {
      min.setFont(min.getFont.deriveFont(state))
      label.setFont(label.getFont.deriveFont(state))
      max.setFont(max.getFont.deriveFont(state))
    }

    override def paintComponent(g: Graphics) = {
      setBackground(InterfaceColors.plotBackground())

      min.setForeground(InterfaceColors.widgetText())
      max.setForeground(InterfaceColors.widgetText())

      if (label.getPreferredSize.width > label.getWidth)
        label.setToolTipText(label.getText)
      else
        label.setToolTipText(null)

      super.paintComponent(g)
    }

    def setMin(text: String): Unit = {min.setText(text)}
    def setMax(text: String): Unit = {max.setText(text)}
    def getLabel: String = label.getText
    def setLabel(text: String): Unit = {
      label.setText(text)
      label.revalidate()
      label.repaint()
    }
  }

  private class VerticalLabel extends JPanel {
    private var text = ""

    def getText: String = text
    def setText(text: String): Unit = {
      this.text = text
    }

    override def getPreferredSize: Dimension =
      new Dimension(getFont.getSize, super.getPreferredSize.height)

    override def paintComponent(g: Graphics): Unit = {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.widgetText())
      g2d.setFont(getFont)

      val metrics = g2d.getFontMetrics

      g2d.rotate(-Pi / 2)
      g2d.translate(-getHeight, metrics.getAscent - metrics.getDescent)

      val finalText = {
        if (metrics.stringWidth(text) > getHeight) {
          var shortened = text

          while (shortened.nonEmpty && metrics.stringWidth(shortened + "...") > getHeight)
            shortened = shortened.dropRight(1)

          shortened + "..."
        } else {
          text
        }
      }

      g2d.drawString(finalText, getHeight / 2 - metrics.stringWidth(finalText) / 2, 0)
    }
  }
}
