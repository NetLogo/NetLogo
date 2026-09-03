// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Graphics, Insets }
import java.awt.image.BufferedImage
import java.util.Locale
import javax.swing.{ Box, BoxLayout, JLabel, JPanel, SwingConstants }

import org.nlogo.core.{ I18N, Pen => CorePen, Plot => CorePlot, Widget => CoreWidget }
import org.nlogo.plot.{ PenListener, PlotManagerInterface, PlotLoader, PlotPen, Plot }
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, RoundedBorderPanel, Utils, VerticalStrut, Zoomable,
                         ZoomableBorder }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.Events.{ AfterLoadEvent, CompiledEvent, WidgetErrorEvent, WidgetRemovedEvent }

import scala.math.Pi

abstract class AbstractPlotWidget(val plot: Plot, val plotManager: PlotManagerInterface)
  extends Widget with Editable with Plot.DirtyListener
  with AfterLoadEvent.Handler
  with WidgetRemovedEvent.Handler
  with CompiledEvent.Handler {

  import AbstractPlotWidget._

  private class CanvasPanel(canvas: PlotCanvas)
    extends BoxRow(Seq(canvas)) with RoundedBorderPanel with Zoomable with ThemeSync {

    setBorder(new ZoomableBorder(3, 3, 3, 3))

    override def zoomComponent(): Unit = {
      setDiameter(Utils.zoom(6))
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(new Color(plot.backgroundColor))
      setBorderColor(InterfaceColors.plotBorder())
    }
  }

  private val originalName = plot.name

  private var fullyConstructed = false
  plot.dirtyListener = Some(this)
  val canvas = new PlotCanvas(plot)
  private val canvasPanel = new CanvasPanel(canvas)

  private val legend = new PlotLegend(this) {
    setBoldState(_boldState)
  }

  private val nameLabel = new JLabel(originalName) with Zoomable {
    setText(plot.name)
    setBaseFont(getFont.deriveFont(_boldState))
  }

  private val xAxis = new XAxisLabels(this) {
    setBoldState(_boldState)
  }

  private val yAxis = new YAxisLabels(this) {
    setBoldState(_boldState)
  }

  plot.addPenListener(new PenListener {
    override def penAdded(): Unit = {
      legend.refresh()
    }
  })

  displayName(originalName)

  plot.clear() // set current values to defaults

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new AdaptableBorder(new Insets(3, 6, 6, 6), new Insets(8, 10, 8, 10)))

  add(new BoxRow(nameLabel, BoxAlign.Center))
  add(new AdaptableVerticalStrut(6, 8))
  add(new BoxRow(Seq(yAxis, canvasPanel), 3))
  add(new VerticalStrut(3))
  add(xAxis)
  add(new VerticalStrut(2))
  add(new BoxRow(legend, BoxAlign.Center))

  // make sure to update the gui components in case
  // something changed underneath ev 8/26/08
  refreshGUI()

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
  override def helpLink = Some(("programming", "plotting"))

  def legendHeight: Int =
    legend.getHeight

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

    if (name.isEmpty) {
      displayName(originalName)
    } else {
      displayName(plot.name)
    }

    nameLabel.setText(displayName)
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

  def compile(): Unit = {
    plotManager.compilePlot(plot)
  }

  /// sizing
  override def getMinimumSize: Dimension =
    Utils.zoomSize(new Dimension(160, 120))

  override def getPreferredSize: Dimension =
    Utils.zoomSize(new Dimension(230, 175))

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
    val b = unzoomedBounds

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
    if (key == "setupCode") {
      plotManager.getPlotSetupError(plot)
    } else if (key == "updateCode") {
      plotManager.getPlotUpdateError(plot)
    } else {
      None
    }
  }

  def error(key: Object, e: Exception): Unit = { throw new UnsupportedOperationException }

  override def errorString: Option[String] = {
    val hasDuplicatedName =
      widgetContainer.map(_.allWidgets).getOrElse(Seq()).collect {
        case p: CorePlot if p.display.map(_.toUpperCase(Locale.ENGLISH))
                                     .getOrElse("") == plotName.toUpperCase(Locale.ENGLISH) => p
      }.length > 1
    if (hasDuplicatedName) {
      Some(I18N.gui.getN("edit.plot.name.duplicate", plotName.toUpperCase(Locale.ENGLISH)))
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
  class XAxisLabels(plot: AbstractPlotWidget) extends BoxRow {
    private val min = new JLabel with Zoomable
    private val label = new JLabel("", SwingConstants.CENTER) with Zoomable
    private val max = new JLabel with Zoomable

    add(min)
    add(Box.createHorizontalGlue)
    add(label)
    add(Box.createHorizontalGlue)
    add(max)

    def setBoldState(state: Int): Unit = {
      min.setBaseFont(min.getBaseFont.deriveFont(state))
      label.setBaseFont(label.getBaseFont.deriveFont(state))
      max.setBaseFont(max.getBaseFont.deriveFont(state))
    }

    override def paintComponent(g: Graphics) = {
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

  class YAxisLabels(plot: AbstractPlotWidget) extends BoxColumn {
    private val label = new VerticalLabel
    private val max = new JLabel with Zoomable
    private val min = new JLabel with Zoomable

    add(new BoxRow(max, BoxAlign.End))
    add(Box.createVerticalGlue)
    add(label)
    add(Box.createVerticalGlue)
    add(new BoxRow(min, BoxAlign.End))

    def setBoldState(state: Int): Unit = {
      min.setBaseFont(min.getBaseFont.deriveFont(state))
      label.setBaseFont(label.getBaseFont.deriveFont(state))
      max.setBaseFont(max.getBaseFont.deriveFont(state))
    }

    override def paintComponent(g: Graphics) = {
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

  private class VerticalLabel extends JPanel with Zoomable {
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
