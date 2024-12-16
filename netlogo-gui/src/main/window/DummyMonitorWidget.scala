// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ Editable, Property }
import org.nlogo.core.{ I18N, Monitor => CoreMonitor }

import java.awt.{ Color, Dimension, Graphics, Font }
import java.util.{ List => JList }

object DummyMonitorWidget {
  private val LeftMargin = 5;
  private val RightMargin = 6;
  private val BottomMargin = 6;
  private val FontPadding = 12;
  private val MinWidth = 50;
  private val MaxHeight = 49;
  private val DefaultDecimalPlaces = 3
  private val DefaultFontSize = 11
}

class DummyMonitorWidget
  extends SingleErrorWidget
  with MonitorWidget.ToMonitorModel
  with Editable {

  type WidgetModel = CoreMonitor

  import DummyMonitorWidget._

  private var _name: String = ""
  private var _decimalPlaces: Int = DefaultDecimalPlaces;

  def innerSource = ""
  def fontSize = DefaultFontSize

  setOpaque(true)
  setBackground(InterfaceColors.MONITOR_BACKGROUND)
  setBorder(widgetBorder)
  org.nlogo.awt.Fonts.adjustDefaultFont(this)

  def name: String = _name

  def name(name: String): Unit = {
    _name = name
    displayName = name
  }

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.monitor")

  def propertySet: JList[Property] =
    Properties.dummyMonitor

  override def getMinimumSize: Dimension =
    new Dimension(MinWidth, MaxHeight)

  override def getMaximumSize: Dimension =
    new Dimension(10000, MaxHeight)

  override def getPreferredSize(font: Font): Dimension = {
    val size = getMinimumSize
    val fontMetrics = getFontMetrics(font)
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(displayName) + FontPadding)
    size.height = StrictMath.max(size.height, fontMetrics.getMaxDescent + fontMetrics.getMaxAscent + FontPadding)
    size
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g) // paint background
    val fm = g.getFontMetrics;
    val labelHeight = fm.getMaxDescent + fm.getMaxAscent
    val d = getSize()
    g.setColor(getForeground)
    val boxHeight = StrictMath.ceil(labelHeight * 1.4).toInt

    g.drawString(displayName, LeftMargin,
        d.height - BottomMargin - boxHeight - fm.getMaxDescent - 1)
    g.setColor(Color.WHITE)

    g.fillRect(LeftMargin, d.height - BottomMargin - boxHeight,
        d.width - LeftMargin - RightMargin - 1, boxHeight)
    g.setColor(Color.BLACK);
  }

  def decimalPlaces: Int = _decimalPlaces

  def decimalPlaces(decimalPlaces: Int): Unit = {
    if (decimalPlaces != _decimalPlaces)
      _decimalPlaces = decimalPlaces
  }

  override def load(monitor: WidgetModel): AnyRef = {
    name(monitor.display.optionToPotentiallyEmptyString)
    decimalPlaces(monitor.precision)
    setSize(monitor.width, monitor.height)
    this
  }
}

