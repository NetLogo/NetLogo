// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N

class DummyButtonEditPanel(target: DummyButtonWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
        () => target.name,
        _.foreach(target.setDisplayName),
        () => apply()))

  private val actionKey =
    new KeyEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.actionKey"),
        () => target.actionKey,
        _.foreach(target.setActionKey),
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

    add(actionKey, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, actionKey)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
