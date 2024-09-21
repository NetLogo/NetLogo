// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.event.MouseEvent
import java.awt.{ Color, Component, EventQueue, Font, Graphics, GridBagConstraints, GridBagLayout, Insets, Dimension }
import java.util.{ List => JList }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.core.{ AgentKind, AgentKindJ, I18N, Monitor => CoreMonitor }
import org.nlogo.api.{ Dump, Editable, MersenneTwisterFast, Property }
import org.nlogo.nvm.Procedure
import org.nlogo.swing.Utils
import org.nlogo.window.Events.{ AddJobEvent, EditWidgetEvent,
  RuntimeErrorEvent, PeriodicUpdateEvent, JobRemovedEvent, RemoveJobEvent }

object MonitorWidget {
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
      val b       = getBoundsTuple
      val display = name.potentiallyEmptyStringToOption
      val src     = innerSource.potentiallyEmptyStringToOption

      CoreMonitor(display = display,
        left = b._1, top = b._2, right = b._3, bottom = b._4,
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

  private class ValuePanel(label: JLabel) extends JPanel {
    setBackground(InterfaceColors.TRANSPARENT)

    setLayout(new GridBagLayout)

    locally {
      val c = new GridBagConstraints

      c.weightx = 1
      c.anchor = GridBagConstraints.WEST
      c.insets = new Insets(0, 6, 0, 6)

      add(label, c)
    }

    override def paintComponent(g: Graphics) {
      val g2d = Utils.initGraphics2D(g)
      g2d.setColor(Color.WHITE)
      g2d.fillRoundRect(0, 0, getWidth, getHeight, (6 * zoomFactor).toInt, (6 * zoomFactor).toInt)
      g2d.setColor(InterfaceColors.MONITOR_BORDER)
      g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, (6 * zoomFactor).toInt, (6 * zoomFactor).toInt)
      super.paintComponent(g)
    }
  }

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

  private val nameLabel = new JLabel
  private val valueLabel = new JLabel

  nameLabel.setForeground(InterfaceColors.WIDGET_TEXT)

  addMouseListener(this)

  setLayout(new GridBagLayout)

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.weightx = 1
    c.anchor = GridBagConstraints.NORTHWEST
    c.insets =
      if (preserveWidgetSizes)
        new Insets(3, 6, 0, 6)
      else
        new Insets(6, 12, 6, 12)

    add(nameLabel, c)

    c.weighty = 1
    c.fill = GridBagConstraints.BOTH
    c.insets =
      if (preserveWidgetSizes)
        new Insets(0, 6, 6, 6)
      else
        new Insets(0, 12, 6, 12)

    add(new ValuePanel(valueLabel), c)
  }

  def name(name: String): Unit = {
    _name = name
    chooseDisplayName()
    repaint()
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
    halt()
    if (procedure != null) {
      hasError = false
      new AddJobEvent(this, agents, procedure).raise(this)
      jobRunning = true
    }
    nameLabel.setForeground(if (procedure == null) Color.RED else InterfaceColors.WIDGET_TEXT)
    repaint()
  }

  def value: AnyRef = _value.orNull

  def value(value: AnyRef): Unit = {
    this._value = Option(value);
    val newString = Dump.logoObject(value)
    if (newString != valueString) {
      valueString = newString
      valueLabel.setText(valueString)
      repaint()
    }
  }

  def propertySet: JList[Property] =
    Properties.monitor

  def chooseDisplayName() {
    if (name == null || name == "")
      displayName(getSourceName)
    else
      displayName(name)
    
    nameLabel.setText(displayName)
  }

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

  override def paintComponent(g: Graphics) {
    backgroundColor = InterfaceColors.MONITOR_BACKGROUND

    if (nameLabel.getPreferredSize.width > nameLabel.getWidth)
      nameLabel.setToolTipText(nameLabel.getText)
    else
      nameLabel.setToolTipText(null)

    super.paintComponent(g)
  }

  override def getMinimumSize: Dimension =
    if (preserveWidgetSizes)
      new Dimension(MinWidth, (fontSize * 4) + 1)
    else
      new Dimension(100, 60)

  override def getMaximumSize: Dimension =
    if (preserveWidgetSizes)
      new Dimension(10000, (fontSize * 4) + 1)
    else
      new Dimension(10000, 60)

  override def getPreferredSize: Dimension =
    if (preserveWidgetSizes)
      new Dimension(getMinimumSize.width.max(nameLabel.getWidth + PreferredSizePad), getMinimumSize.height)
    else
      new Dimension(100, 60)

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

    setSize(model.right - model.left, model.bottom - model.top)
    chooseDisplayName()
    this
  }

  def mouseClicked(e: MouseEvent): Unit = {
    if (!e.isPopupTrigger && error() != null && !lastMousePressedWasPopupTrigger)
      new EditWidgetEvent(this).raise(this)
  }

  def mousePressed(e: MouseEvent): Unit = {
    lastMousePressedWasPopupTrigger = e.isPopupTrigger
  }

  def mouseReleased(e: MouseEvent): Unit = { }

  def mouseEntered(e: MouseEvent): Unit = { }

  def mouseExited(e: MouseEvent): Unit = { }
}
