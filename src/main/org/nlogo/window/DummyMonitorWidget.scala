// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Font, Graphics }
import org.nlogo.api.{ Editable, I18N, Property }

import java.util.List

object DummyMonitorWidget {
  private val LEFT_MARGIN   = 5
  private val RIGHT_MARGIN  = 6
  private val BOTTOM_MARGIN = 6
  private val MIN_WIDTH  = 50
  private val MAX_HEIGHT = 49
}

class DummyMonitorWidget extends SingleErrorWidget with Editable {
  setOpaque(true)
  setBackground(InterfaceColors.MONITOR_BACKGROUND)
  setBorder(widgetBorder)
  org.nlogo.awt.Fonts.adjustDefaultFont(this)

  private var _name = ""
  def name(__name: String) = _name = __name
  def name = _name
  displayName = _name
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.monitor")
  
  def propertySet = Properties.dummyMonitor
  
  override def getMinimumSize = new Dimension(DummyMonitorWidget.MIN_WIDTH, DummyMonitorWidget.MAX_HEIGHT)
  override def getMaximumSize = new Dimension(10000, DummyMonitorWidget.MAX_HEIGHT)
  override def getPreferredSize(font: Font) = {
    val size = getMinimumSize
    val pad = 12
    val fontMetrics = getFontMetrics(font)
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(displayName) + pad)
    size.height = StrictMath.max(size.height, fontMetrics.getMaxDescent + fontMetrics.getMaxAscent + pad)
    size
  }
  
  override def paintComponent(g: Graphics) = {
    super.paintComponent(g) // paint background
    MonitorPainter.paint(g, getSize, getForeground, displayName, "")
  }

  private var _decimalPlaces = 3
  def decimalPlaces = _decimalPlaces
  def decimalPlaces(__decimalPlaces: Int) = _decimalPlaces = __decimalPlaces
  
  override def save() = "MONITOR\n" +
    s"$getBoundsString${if(name!=null&&name.trim!="") name else "NIL"}\nNIL\n" +
    s"${_decimalPlaces}\n1\n" // the `1` is for compatability
  override def load(strings: scala.collection.Seq[String],helper: Widget.LoadHelper) = {
    val displayName = strings(5)
    name(if(displayName=="NIL") "" else displayName)
    if (strings.size > 7)
      _decimalPlaces = strings(7).toInt
    val x1 = strings(1).toInt
    val y1 = strings(2).toInt
    val x2 = strings(3).toInt
    val y2 = strings(4).toInt
    setSize(x2-x1, y2-y1)
    this
  }
}
