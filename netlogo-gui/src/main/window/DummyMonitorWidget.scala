// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JLabel, JPanel }

import org.nlogo.core.{ I18N, Monitor => CoreMonitor, Widget => CoreWidget }
import org.nlogo.swing.RoundedBorderPanel
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class DummyMonitorWidget extends SingleErrorWidget with MonitorWidget.ToMonitorModel with Editable {
  private var _name: String = ""
  private var _decimalPlaces = 3
  private var _units: String = ""

  private val nameLabel = new JLabel(I18N.gui.get("edit.monitor.previewName"))

  private val valuePanel = new JPanel(new GridBagLayout) with RoundedBorderPanel with ThemeSync {
    override def paintComponent(g: Graphics): Unit = {
      setDiameter(zoom(6))

      super.paintComponent(g)
    }

    override def syncTheme(): Unit = {
      setBackgroundColor(InterfaceColors.displayAreaBackground())
      setBorderColor(InterfaceColors.monitorBorder())
    }
  }

  setLayout(new GridBagLayout)

  initGUI()

  override def initGUI(): Unit = {
    removeAll()

    val c = new GridBagConstraints

    c.gridx = 0
    c.gridwidth = 2
    c.weightx = 1
    c.anchor = GridBagConstraints.NORTHWEST
    c.insets = {
      if (_oldSize) {
        new Insets(zoom(3), zoom(6), 0, zoom(6))
      } else {
        new Insets(zoom(6), zoom(8), zoom(6), zoom(8))
      }
    }

    add(nameLabel, c)

    nameLabel.setFont(nameLabel.getFont.deriveFont(_boldState))

    c.gridwidth = 1
    c.fill = GridBagConstraints.BOTH
    c.weighty = 1
    c.insets = {
      if (_oldSize) {
        new Insets(0, zoom(6), zoom(6), zoom(6))
      } else {
        new Insets(0, zoom(8), zoom(8), zoom(8))
      }
    }

    add(valuePanel, c)
  }

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
      new Dimension(50, (fontSize * 4) + 1)
    } else {
      new Dimension(100, 60)
    }
  }

  override def getPreferredSize: Dimension = {
    if (_oldSize) {
      new Dimension(100, getMinimumSize.height)
    } else {
      new Dimension(100, 60)
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
