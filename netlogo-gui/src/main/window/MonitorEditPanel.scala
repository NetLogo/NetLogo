// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer

class MonitorEditPanel(target: MonitorWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val wrapSource =
    new NonEmptyCodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.reporter"),
        () => target.wrapSource,
        name => target.setWrapSource(name.getOrElse("")),
        () => apply()),
      colorizer)

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.name"),
        () => target.name,
        _.foreach(target.setDisplayName),
        () => apply()))

  private val decimalPlaces =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.decimalPlaces"),
        () => target.decimalPlaces,
        _.foreach(target.setDecimalPlaces),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.units"),
        () => target.units,
        _.foreach(target.setUnits),
        () => apply()))

  private val decimalLabeled = new LabeledEditor(decimalPlaces, I18N.gui.get("edit.monitor.precision"))

  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.monitor.fontSize"),
        () => target.fontSize,
        _.foreach(target.setFontSize),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  locally {
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
    c.insets = new Insets(0, 6, 6, 6)

    add(decimalLabeled, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(units, c)

    c.gridy = 3
    c.gridwidth = 2
    c.insets = new Insets(0, 6, 6, 6)

    add(fontSize, c)

    c.gridy = 4

    add(oldSize, c)

    wrapSource.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(wrapSource, name, decimalPlaces, units, fontSize, oldSize)

  override def isResizable: Boolean = true

  override def syncExtraComponents(): Unit = {
    decimalLabeled.syncTheme()
  }
}
