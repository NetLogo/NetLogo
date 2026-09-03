// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Insets }
import javax.swing.{ BoxLayout, JLabel, JPanel }

import org.nlogo.core.{ I18N, Monitor => CoreMonitor, Widget => CoreWidget }
import org.nlogo.swing.{ BoxAlign, BoxRow, RoundedBorderPanel, Utils }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class DummyMonitorWidget extends SingleErrorWidget with MonitorWidget.ToMonitorModel with Editable {
  private var _name: String = ""
  private var _decimalPlaces = 3
  private var _units: String = ""

  private val nameLabel = new JLabel(I18N.gui.get("edit.monitor.previewName"))

  private val valuePanel = new JPanel with RoundedBorderPanel with ThemeSync {
    setDiameter(6)

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.displayAreaBackground())
      setBorderColor(InterfaceColors.monitorBorder())
    }
  }

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new AdaptableBorder(new Insets(3, 6, 6, 6), new Insets(6, 8, 8, 8)))

  add(new BoxRow(nameLabel, BoxAlign.Start))
  add(new AdaptableVerticalStrut(0, 6))
  add(valuePanel)

  def innerSource = ""
  def fontSize = 11

  def name: String = _name

  def setDisplayName(name: String): Unit = {
    val suffix = if (_units.isEmpty) "" else (" " + _units)
    _name = name + suffix
    displayName = name

    if (_name.trim.isEmpty) {
      nameLabel.setText(I18N.gui.get("edit.monitor.previewName"))
    } else {
      nameLabel.setText(_name)
    }
  }

  def units: String = _units
  def setUnits(value: String): Unit = {
    _units = value
    revalidate()
    repaint()
  }

  override def classDisplayName: String =
    I18N.gui.get("tabs.run.widgets.monitor")

  override def editPanel: EditPanel = new DummyMonitorEditPanel(this)

  override def getEditable: Option[Editable] = Some(this)

  override def getMinimumSize: Dimension = {
    if (_oldSize) {
      new Dimension(Utils.zoom(50), (fontSize * 4) + Utils.zoomClamped(1))
    } else {
      new Dimension(Utils.zoom(100), Utils.zoom(60))
    }
  }

  override def getPreferredSize: Dimension = {
    if (_oldSize) {
      new Dimension(Utils.zoom(100), getMinimumSize.height)
    } else {
      new Dimension(Utils.zoom(100), Utils.zoom(60))
    }
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.monitorBackground())

    nameLabel.setForeground(InterfaceColors.widgetText())

    valuePanel.syncTheme()
  }

  def decimalPlaces: Int = _decimalPlaces

  def setDecimalPlaces(decimalPlaces: Int): Unit = {
    if (decimalPlaces != _decimalPlaces)
      _decimalPlaces = decimalPlaces
  }

  override def load(model: CoreWidget): Unit = {
    model match {
      case monitor: CoreMonitor =>
        setUnits(monitor.units.getOrElse(""))
        setDisplayName(monitor.display.optionToPotentiallyEmptyString)
        setDecimalPlaces(monitor.precision)
        oldSize(monitor.oldSize)
        setSize(monitor.width, monitor.height)

      case _ =>
    }
  }
}
