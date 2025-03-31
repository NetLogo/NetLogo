// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N

class DummySliderEditPanel(target: DummySliderWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
        () => target.name,
        target.setVarName(_),
        () => apply()))

  private val min =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.minimum"),
        () => target.min,
        target.setMin(_),
        () => apply()))

  private val inc =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.increment"),
        () => target.inc,
        target.setInc(_),
        () => apply()))

  private val max =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.maximum"),
        () => target.max,
        target.setMax(_),
        () => apply()))

  private val value =
    new DoubleEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.value"),
        () => target.value,
        target.setValue(_),
        () => apply()))

  private val units =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.units"),
        () => target.units,
        target.setUnits(_),
        () => apply()))

  private val vertical =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.slider.vertical"),
        () => target.vertical,
        target.setVertical(_),
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
    val c = new GridBagConstraints

    c.gridy = 0
    c.gridwidth = 3
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(name, c)

    c.gridy = 1
    c.gridwidth = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(min, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(inc, c)
    add(max, c)

    c.gridy = 2
    c.gridwidth = 2
    c.insets = new Insets(0, 6, 6, 6)

    add(value, c)

    c.gridwidth = 1
    c.insets = new Insets(0, 0, 6, 6)

    add(units, c)

    c.gridy = 3
    c.gridwidth = 3
    c.insets = new Insets(0, 6, 6, 6)

    add(vertical, c)

    c.gridy = 4

    add(oldSize, c)
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(name, min, inc, max, value, units, vertical, oldSize)

  override def syncTheme(): Unit = {
    name.syncTheme()
    min.syncTheme()
    inc.syncTheme()
    max.syncTheme()
    value.syncTheme()
    units.syncTheme()
    vertical.syncTheme()
    oldSize.syncTheme()
  }
}
