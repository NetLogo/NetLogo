// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, EventQueue, Font, Graphics }
import java.awt.event.{ MouseEvent, MouseListener }
import org.nlogo.agent.Observer
import org.nlogo.api.{ AgentKind, Approximate, Dump, Editable, I18N, ModelReader, Property }
import org.nlogo.awt.Fonts
import org.nlogo.nvm.{ Procedure, Reporter }
import org.nlogo.util.MersenneTwisterFast
import scala.collection.Seq

object MonitorWidget {
  private val MIN_WIDTH = 50
}

class MonitorWidget(random: MersenneTwisterFast) extends JobWidget(random) with Editable
    with Events.RuntimeErrorEventHandler with Events.PeriodicUpdateEventHandler
    with Events.JobRemovedEventHandler with MouseListener {
  private var jobRunning = false
  private var hasError = false

  private var _fontSize = getFont.getSize
  def fontSize = _fontSize
  def fontSize_=(size: Int) = {
    _fontSize = size
    // If we are zoomed, we need to zoom the input font size and then
    // set that as our widget font
    if(originalFont != null) {
      val zoomDiff = getFont.getSize - originalFont.getSize
      setFont(getFont.deriveFont((size + zoomDiff).toFloat))
    } else {
      setFont(getFont.deriveFont(size.toFloat))
    }

    if(originalFont != null)
      originalFont = originalFont.deriveFont(size.toFloat)
    // These should reset the cached values in the Zoomer, but
    // after one finishes a property widget edit, it appears to
    // still cache the min height. -- CLB
    resetZoomInfo()
    resetSizeInfo()
  }
  override def setFont(f: Font) =
    if (isZoomed || getFont == null)
      super.setFont(f)
    else
      super.setFont(getFont.deriveFont(fontSize.toFloat))

  setOpaque(true)
  addMouseListener(this)
  setBackground(InterfaceColors.MONITOR_BACKGROUND)
  setBorder(widgetBorder)
  Fonts.adjustDefaultFont(this)

  private var _name = ""
  def name = _name
  def name_=(__name: String) = {
    _name = __name
    chooseDisplayName()
  }

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.monitor")

  override def kind = AgentKind.Observer

  override def ownsPrimaryJobs = false // we own secondary jobs, not primary

  override def procedure_=(procedure: Procedure) = {
    super.procedure = procedure
    setForeground(if(procedure == null) Color.RED else null)
    halt()
    if(procedure != null) {
      hasError = false
      new Events.AddJobEvent(this, agents, procedure).raise(this)
      jobRunning = true
    }
    repaint()
  }

  private var _value: AnyRef = null
  def value = _value
  def value_=(__value: AnyRef) = {
    _value = __value
    val newString = _value match {
      case doubleObj: java.lang.Double =>
        Dump.number(Approximate.approximate(doubleObj.doubleValue, decimalPlaces))
      case _ => Dump.logoObject(value)
    }
    if(newString != valueString) {
      _valueString = newString
      repaint()
    }
  }

  private var _valueString = ""
  def valueString = _valueString

  def propertySet = Properties.monitor

  private def chooseDisplayName() =
    if(name == null || name == "")
      displayName(getSourceName)
    else
      displayName(name)

  // behold the mighty regular expression
  private def getSourceName = innerSource.trim.replaceAll("\\s+", " ")

  override def removeNotify() = {
    // This is a little kludgy.  Normally removeNotify would run on the
    // event thread, but in an applet context, when the applet
    // shuts down, removeNotify can run on some other thread. But
    // actually this stuff doesn't need to happen in the applet,
    // so we can just skip it in that context. - ST 10/12/03, 10/16/03
    if(EventQueue.isDispatchThread)
      halt()
    super.removeNotify()
  }

  override def suppressRecompiles_=(_suppressRecompiles: Boolean) = {
    if(innerSource.trim == "")
      recompilePending = false
    super.suppressRecompiles_=(_suppressRecompiles)
  }

  override def getMinimumSize = {
    val h = (fontSize * 4) + 1
    new Dimension(MonitorWidget.MIN_WIDTH, h)
  }
  override def getMaximumSize = {
    val h = (fontSize * 4) + 1
    new Dimension(10000, h)
  }
  override def getPreferredSize(font: Font) = {
    val size = getMinimumSize
    val pad = 12
    val fontMetrics = getFontMetrics(font)
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(displayName) + pad)
    size
  }

  override def paintComponent(g: Graphics) = {
    super.paintComponent(g) // paint background
    MonitorPainter.paint(g, getSize, getForeground, displayName, valueString)
  }

  private var _decimalPlaces = 17
  def decimalPlaces = _decimalPlaces
  def decimalPlaces_=(__decimalPlaces: Int) = if(__decimalPlaces != _decimalPlaces) {
      _decimalPlaces = __decimalPlaces
      wrapSource(innerSource)
    }

  override def innerSource_=(_innerSource: String) = {
    super.innerSource = _innerSource
    chooseDisplayName()
  }

  // fragile -- depends on the details of what code wrapSource wraps
  // the user's reporter and what the resulting compiled code looks
  // like. model runs code calls this to grab the Reporter to run
  // at tick time - ST 10/11/12
  def reporter: Option[Reporter] = {
    val p = procedure
    Option(if(p == null) null else p.code(0).args(0))
  }

  def wrapSource = innerSource
  def wrapSource(innerSource: String) = {
    if(innerSource.trim == "") {
      source(null, "", null)
      halt()
    } else {
      source("to __monitor [] __observercode loop [ __updatemonitor (",
             innerSource, "\n)] end")
    }
    chooseDisplayName()
  }

  def handle(e: Events.RuntimeErrorEvent) = if(this == e.jobOwner) {
      hasError = true
      halt()
    }

  def handle(e: Events.PeriodicUpdateEvent) = if(!jobRunning && procedure != null) {
      hasError = false
      jobRunning = true
      new Events.AddJobEvent(this, agents, procedure).raise(this)
    }

  def handle(e: Events.JobRemovedEvent) = if(e.owner == this) {
      jobRunning = false
      value = if(hasError) "N/A" else ""
    }

  private def halt() = new Events.RemoveJobEvent(this).raise(this)

  override def save = "MONITOR\n" +
    getBoundsString +
    s"${if(null != name && name.trim != "") name else "NIL"}\n" +
    s"${if(innerSource.trim != "") ModelReader.stripLines(innerSource) else "NIL"}\n" +
    s"$decimalPlaces\n" +
    "1\n" + // for compatability
    s"fontSize\n"

  override def load(strings: Seq[String], helper: Widget.LoadHelper) = {
    val displayName = strings(5)
    val source = ModelReader.restoreLines(strings(6))

    name = if (displayName == "NIL") "" else displayName
    if(strings.size > 7)
      decimalPlaces = strings(7).toInt
    if(strings.size > 9) {
      fontSize = strings(9).toInt
    }
    if(source != "NIL")
      wrapSource(helper.convert(source, true))
    val x1 = strings(1).toInt
    val y1 = strings(2).toInt
    val x2 = strings(3).toInt
    val y2 = strings(4).toInt
    setSize(x2 - x1, y2 - y1)
    chooseDisplayName()
    this
  }
  
  // This is the same as the button so right-click on a monitor w/ error
  // brings up the popup menu not the edit dialog. ev 1/4/06
  
  private var lastMousePressedWasPopupTrigger = false

  def mouseClicked(e: MouseEvent) =
    if (!e.isPopupTrigger && error != null && !lastMousePressedWasPopupTrigger)
      new Events.EditWidgetEvent(this).raise(this)
  def mousePressed(e: MouseEvent) = lastMousePressedWasPopupTrigger = e.isPopupTrigger
  def mouseReleased(e: MouseEvent) = {}
  def mouseEntered(e: MouseEvent)  = {}
  def mouseExited(e: MouseEvent)   = {}
}
