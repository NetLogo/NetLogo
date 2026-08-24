// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ Box, BoxLayout }

import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxRow, VerticalStrut, ZoomableBorder }

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

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(name)
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(actionKey, Box.createHorizontalGlue)))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, actionKey)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
