// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Graphics }

import org.nlogo.api.{ Editable, Property }
import org.nlogo.core.{ I18N, Monitor => CoreMonitor }
import org.nlogo.theme.InterfaceColors

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
  private var _decimalPlaces: Int = DefaultDecimalPlaces
  private var _units: String = ""

  def innerSource = ""
  def fontSize = DefaultFontSize

  def name: String = _name

  def name(name: String): Unit = {
    val suffix = if (_units.isEmpty) "" else (" " + _units)
    _name = name + suffix
    displayName = name
  }

  def units: String = _units
  def units(value: String): Unit = {
    _units = value
    revalidate()
    repaint()
  }

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.monitor")

  def propertySet: Seq[Property] =
    Properties.dummyMonitor

  override def getMinimumSize: Dimension =
    new Dimension(MinWidth, MaxHeight)

  override def getPreferredSize: Dimension = {
    val size = getMinimumSize
    val fontMetrics = getFontMetrics(getFont)
    size.width = StrictMath.max(size.width, fontMetrics.stringWidth(displayName) + FontPadding)
    size.height = StrictMath.max(size.height, fontMetrics.getMaxDescent + fontMetrics.getMaxAscent + FontPadding)
    size
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g) // paint background
    val fm = g.getFontMetrics;
    val labelHeight = fm.getMaxDescent + fm.getMaxAscent
    val d = getSize()
    val boxHeight = StrictMath.ceil(labelHeight * 1.4).toInt

    g.setColor(InterfaceColors.widgetText())
    g.drawString(displayName, LeftMargin,
        d.height - BottomMargin - boxHeight - fm.getMaxDescent - 1)

    g.setColor(InterfaceColors.displayAreaBackground())
    g.fillRect(LeftMargin, d.height - BottomMargin - boxHeight,
        d.width - LeftMargin - RightMargin - 1, boxHeight)
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.monitorBackground())
  }

  def decimalPlaces: Int = _decimalPlaces

  def decimalPlaces(decimalPlaces: Int): Unit = {
    if (decimalPlaces != _decimalPlaces)
      _decimalPlaces = decimalPlaces
  }

  override def load(monitor: WidgetModel): AnyRef = {
    units(monitor.units.getOrElse(""))
    name(monitor.display.optionToPotentiallyEmptyString)
    decimalPlaces(monitor.precision)
    oldSize = monitor.oldSize
    setSize(monitor.width, monitor.height)
    this
  }
}

