// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ AgentKind, AgentKindJ, I18N, Monitor => CoreMonitor }
import org.nlogo.api.{ Dump, Editable, MersenneTwisterFast, Property }
import org.nlogo.awt.{ Fonts => NlogoFonts }
import org.nlogo.nvm.Procedure
import org.nlogo.window.Events.{ AddJobEvent, EditWidgetEvent,
  RuntimeErrorEvent, PeriodicUpdateEvent, JobRemovedEvent, RemoveJobEvent }

import java.awt.event.MouseEvent
import java.awt.{ Component, EventQueue,
  Font, Graphics, Dimension, Color => AwtColor }
import java.util.{ List => JList }


object MonitorWidget {
  private val LeftMargin = 5
  private val RightMargin = 6
  private val BottomMargin = 6
  private val InsideBottomMargin = 3
  private val MinWidth = 50
  private val DefaultDecimalPlaces = 17
  private val DefaultFontSize = 11
  private val PreferredSizePad = 12

  trait ToMonitorModel { self: Widget with Component =>
    def decimalPlaces: Int
    def fontSize: Int
    def innerSource: String
    def name: String

    override def model: CoreMonitor = {
      val b       = getUnzoomedBounds
      val display = name.potentiallyEmptyStringToOption
      val src     = innerSource.potentiallyEmptyStringToOption

      CoreMonitor(display = display,
        x = b.x, y = b.y, width = b.width, height = b.height,
        source   = src, precision = decimalPlaces,
        fontSize = fontSize)
    }
  }
}

import MonitorWidget._

class MonitorWidget(random: MersenneTwisterFast)
    extends JobWidget(random)
    with Editable
    with ToMonitorModel
    with RuntimeErrorEvent.Handler
    with PeriodicUpdateEvent.Handler
    with JobRemovedEvent.Handler
    with java.awt.event.MouseListener {

  type WidgetModel = CoreMonitor

  private var jobRunning: Boolean = false
  private var hasError: Boolean = false
  private var _name: String = ""
  private var _value: Option[AnyRef] = Option.empty[AnyRef]
  private var valueString: String = ""
  private var _decimalPlaces: Int = DefaultDecimalPlaces

  // This is the same as the button so right-click on a monitor w/ error
  // brings up the popup menu not the edit dialog. ev 1/4/06
  private var lastMousePressedWasPopupTrigger: Boolean = false;

  private var _fontSize = DefaultFontSize

  setOpaque(true)
  addMouseListener(this)
  setBackground(InterfaceColors.MONITOR_BACKGROUND)
  setBorder(widgetBorder)
  NlogoFonts.adjustDefaultFont(this)

  def name(name: String): Unit = {
    _name = name
    chooseDisplayName()
  }

  def name: String = _name

  def fontSize(size: Int): Unit = {
    _fontSize = size
    // If we are zoomed, we need to zoom the input font size and then
    // set that as our widget font
    val newFontSize =
      if (originalFont != null) {
        val zoomDiff = getFont.getSize - originalFont.getSize
        zoomDiff + size
      } else
        size

    setFont(getFont.deriveFont(newFontSize.toFloat))

    if (originalFont != null)
      this.originalFont = originalFont.deriveFont(size.toFloat)

    // These should reset the cached values in the Zoomer, but
    // after one finishes a property widget edit, it appears to
    // still cache the min height. -- CLB
    resetZoomInfo()
    resetSizeInfo()
  }

  override def setFont(f: Font): Unit = {
    if (isZoomed || getFont == null)
      super.setFont(f)
    else
      super.setFont(getFont.deriveFont(fontSize.toFloat))
  }

  def fontSize: Int = _fontSize

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.monitor")

  override def kind: AgentKind = AgentKindJ.Observer

  override def ownsPrimaryJobs: Boolean =
    false

  override def procedure_=(procedure: Procedure): Unit = {
    super.procedure = procedure
    setForeground(if (procedure == null) AwtColor.RED else null)
    halt()
    if (procedure != null) {
      hasError = false
      new AddJobEvent(this, agents, procedure).raise(this)
      jobRunning = true
    }
    repaint()
  }

  def value: AnyRef = _value.orNull

  def value(value: AnyRef): Unit = {
    this._value = Option(value);
    val newString = Dump.logoObject(value)
    if (newString != valueString) {
      valueString = newString
      repaint()
    }
  }

  def propertySet: JList[Property] =
    Properties.monitor

  def chooseDisplayName(): Unit =
    if (name == null || name == "")
      displayName(getSourceName)
    else
      displayName(name)

  // behold the mighty regular expression
  private def getSourceName: String =
    innerSource.trim.replaceAll("\\s+", " ")

  override def removeNotify(): Unit = {
    // This is a little kludgy.  Normally removeNotify would run on the
    // event thread, but in an applet context, when the applet
    // shuts down, removeNotify can run on some other thread. But
    // actually this stuff doesn't need to happen in the applet,
    // so we can just skip it in that context. - ST 10/12/03, 10/16/03
    if (EventQueue.isDispatchThread)
      halt()

    super.removeNotify()
  }

  override def suppressRecompiles(suppressRecompiles: Boolean): Unit = {
    if (innerSource.trim == "")
      recompilePending(false)

    super.suppressRecompiles(suppressRecompiles)
  }

  override def getMinimumSize: Dimension = {
    val h = (fontSize * 4) + 1
    new Dimension(MinWidth, h)
  }

  override def getMaximumSize: Dimension = {
    val h = (fontSize * 4) + 1
    new Dimension(10000, h)
  }

  override def getPreferredSize(font: Font): Dimension = {
    val size = getMinimumSize
    val fontMetrics = getFontMetrics(font)
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(displayName) + PreferredSizePad)
    size
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val fm = g.getFontMetrics
    val labelHeight = fm.getMaxDescent + fm.getMaxAscent
    val d = getSize()
    g.setColor(getForeground);
    val boxHeight = StrictMath.ceil(labelHeight * 1.4).toInt
    val shortString = NlogoFonts.shortenStringToFit(
      displayName, d.width - LeftMargin - RightMargin, fm);
    g.drawString(shortString, LeftMargin,
        d.height - BottomMargin - boxHeight - fm.getMaxDescent - 1);
    g.setColor(AwtColor.WHITE)
    g.fillRect(LeftMargin, d.height - BottomMargin - boxHeight,
        d.width - LeftMargin - RightMargin - 1, boxHeight)
    g.setColor(AwtColor.BLACK)
    if (valueString != "")
      g.drawString(valueString,
          LeftMargin + 5,
          d.height - BottomMargin - InsideBottomMargin - fm.getMaxDescent)
    setToolTipText(if (shortString != displayName) displayName else null)
  }

  def decimalPlaces: Int = _decimalPlaces

  def decimalPlaces(decimalPlaces: Int): Unit = {
    if (decimalPlaces != _decimalPlaces) {
      _decimalPlaces = decimalPlaces
      wrapSource(innerSource)
    }
  }

  override def innerSource_=(innerSource: String): Unit = {
    super.innerSource = innerSource
    chooseDisplayName()
  }

  def wrapSource(innerSource: String): Unit = {
    if (innerSource.trim == "") {
      source("", "", "")
      halt()
    } else {
      source(
        "to __monitor [] __observercode loop [ __updatemonitor __monitorprecision (",
          innerSource,
          "\n) " + decimalPlaces + " ] end")
    }
    chooseDisplayName()
  }

  def wrapSource: String = innerSource

  def handle(e: RuntimeErrorEvent): Unit =
    if (this == e.jobOwner) {
      hasError = true
      halt()
    }

  def handle(e: PeriodicUpdateEvent): Unit =
    if (!jobRunning && procedure != null) {
      hasError = false
      jobRunning = true
      new AddJobEvent(this, agents, procedure).raise(this);
    }

  def handle(e: JobRemovedEvent): Unit =
    if (e.owner == this) {
      jobRunning = false
      value(if (hasError) I18N.gui.get("tabs.run.widgets.monitor.notApplicable") else "")
    }

  def halt(): Unit = {
    new RemoveJobEvent(this).raise(this)
  }

  override def load(model: WidgetModel): AnyRef = {
    name(model.display.getOrElse(""))
    _decimalPlaces = model.precision
    fontSize(model.fontSize)

    model.source.foreach(wrapSource)

    setSize(model.width, model.height)
    chooseDisplayName()
    this
  }

  def mouseClicked(e: MouseEvent): Unit = {
    if (!e.isPopupTrigger && error != null && !lastMousePressedWasPopupTrigger)
      new EditWidgetEvent(this).raise(this)
  }

  def mousePressed(e: MouseEvent): Unit = {
    lastMousePressedWasPopupTrigger = e.isPopupTrigger
  }

  def mouseReleased(e: MouseEvent): Unit = { }

  def mouseEntered(e: MouseEvent): Unit = { }

  def mouseExited(e: MouseEvent): Unit = { }
}
