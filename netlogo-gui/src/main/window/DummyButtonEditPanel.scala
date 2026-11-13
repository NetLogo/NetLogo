// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummyButtonEditPanel(target: DummyButtonWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Tag",
        () => target.name,
        _.foreach(target.setDisplayName),
        () => apply()))

  private val actionKey =
    new KeyEditor(
      new PropertyAccessor(
        target,
        loc"Action key",
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
