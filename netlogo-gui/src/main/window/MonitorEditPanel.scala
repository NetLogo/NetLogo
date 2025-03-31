// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }
import javax.swing.JLabel

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.theme.InterfaceColors

class MonitorEditPanel(target: MonitorWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val wrapSource =
    new NonEmptyCodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.reporter"),
        () => target.wrapSource,
        target.setWrapSource(_),
        () => apply()),
      colorizer)

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.name"),
        () => target.name,
        target.setDisplayName(_),
        () => apply()))

  private val decimalPlaces =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.decimalPlaces"),
        () => target.decimalPlaces,
        target.setDecimalPlaces(_),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.units"),
        () => target.units,
        target.setUnits(_),
        () => apply()))

  private val decimalLabel = new JLabel(I18N.gui.get("edit.monitor.precision")) {
    setFont(getFont.deriveFont(9.0f))
  }

  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.fontSize"),
        () => target.fontSize,
        target.setFontSize(_),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        target.oldSize(_),
        () => apply()))

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridy = 0
    c.gridwidth = 2
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(wrapSource, c)

    c.gridy = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(name, c)

    c.gridy = 2
    c.gridwidth = 1
    c.insets = new Insets(0, 6, 3, 6)

    add(decimalPlaces, c)

    c.insets = new Insets(0, 0, 3, 6)

    add(units, c)

    c.gridy = 3
    c.insets = new Insets(0, 6, 6, 6)

    add(decimalLabel, c)

    c.gridy = 4
    c.gridwidth = 2

    add(fontSize, c)

    c.gridy = 5

    add(oldSize, c)

    wrapSource.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(wrapSource, name, decimalPlaces, units, fontSize, oldSize)

  override def isResizable: Boolean = true

  override def syncTheme(): Unit = {
    wrapSource.syncTheme()
    name.syncTheme()
    decimalPlaces.syncTheme()
    units.syncTheme()
    fontSize.syncTheme()
    oldSize.syncTheme()

    decimalLabel.setForeground(InterfaceColors.dialogText)
  }
}
