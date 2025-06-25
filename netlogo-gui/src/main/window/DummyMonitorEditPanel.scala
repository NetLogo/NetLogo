// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N

class DummyMonitorEditPanel(target: DummyMonitorWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
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

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(name, c)

    c.fill = GridBagConstraints.NONE
    c.insets = new Insets(0, 6, 6, 6)

    add(decimalPlaces, c)
    add(oldSize, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, decimalPlaces, oldSize)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
