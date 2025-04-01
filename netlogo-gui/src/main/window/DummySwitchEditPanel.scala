// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N

class DummySwitchEditPanel(target: DummySwitchWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
        () => target.name,
        target.setVarName(_),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        target.oldSize(_),
        () => apply()))

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(name, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(oldSize, c)

    name.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(name, oldSize)
}
