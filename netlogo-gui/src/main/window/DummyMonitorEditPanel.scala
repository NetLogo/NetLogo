// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummyMonitorEditPanel(target: DummyMonitorWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Tag",
        () => target.name,
        _.foreach(target.setDisplayName),
        () => apply()))

  private val decimalPlaces =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        loc"Decimal places",
        () => target.decimalPlaces,
        _.foreach(target.setDecimalPlaces),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Use old sizing",
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))


  add(name)
  add(decimalPlaces)
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, decimalPlaces, oldSize)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
