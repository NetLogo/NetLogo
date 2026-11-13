// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummySliderEditPanel(target: DummySliderWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Tag",
        () => target.name,
        _.foreach(target.setVarName),
        () => apply()))

  private val min =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        loc"Minimum",
        () => target.min,
        _.foreach(target.setMin),
        () => apply()))

  private val inc =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        loc"Increment",
        () => target.inc,
        _.foreach(target.setInc),
        () => apply()))

  private val max =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        loc"Maximum",
        () => target.max,
        _.foreach(target.setMax),
        () => apply()))

  private val value =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        loc"Value",
        () => target.value,
        _.foreach(target.setValue),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Units (optional)",
        () => target.units,
        _.foreach(target.setUnits),
        () => apply()))

  private val vertical: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"vertical?",
        () => target.vertical,
        _.foreach(target.setVertical),
        () => apply(vertical.get.toOption.exists(_ != vertical.originalValue))))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Use old sizing",
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  add(name)
  add(new BoxRow(Seq(min, inc, max), 6))
  add(new BoxRow(Seq(value, units), 6))
  add(new BoxRow(vertical, BoxAlign.Start))
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, min, inc, max, value, units, vertical, oldSize)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
