// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxAlign, BoxRow }

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

  add(name)
  add(new BoxRow(actionKey, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, actionKey)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
