// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }

import org.nlogo.core.I18N

class DummyButtonEditPanel(target: DummyButtonWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
        () => target.name,
        target.setDisplayName(_),
        () => apply()))

  private val actionKey =
    new KeyEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.actionKey"),
        () => target.actionKey,
        target.setActionKey(_),
        () => apply()))

  locally {
    setLayout(new GridBagLayout)

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

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(name, actionKey)

  override def syncTheme(): Unit = {
    name.syncTheme()
    actionKey.syncTheme()
  }
}
