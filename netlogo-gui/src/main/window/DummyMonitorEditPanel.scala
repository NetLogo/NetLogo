// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.BoxLayout

import org.nlogo.core.I18N
import org.nlogo.swing.{ VerticalStrut, ZoomableBorder }

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

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(name)
  add(new VerticalStrut(6))
  add(decimalPlaces)
  add(new VerticalStrut(6))
  add(oldSize)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, decimalPlaces, oldSize)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
