// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummySliderEditPanel(target: DummySliderWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
        () => target.name,
        _.foreach(target.setVarName),
        () => apply()))

  private val min =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.minimum"),
        () => target.min,
        _.foreach(target.setMin),
        () => apply()))

  private val inc =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.increment"),
        () => target.inc,
        _.foreach(target.setInc),
        () => apply()))

  private val max =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.maximum"),
        () => target.max,
        _.foreach(target.setMax),
        () => apply()))

  private val value =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.value"),
        () => target.value,
        _.foreach(target.setValue),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.units"),
        () => target.units,
        _.foreach(target.setUnits),
        () => apply()))

  private val vertical: BooleanEditor =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.vertical"),
        () => target.vertical,
        _.foreach(target.setVertical),
        () => apply(vertical.get.toOption.exists(_ != vertical.originalValue))))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
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
