// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummySwitchEditPanel(target: DummySwitchWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
        () => target.name,
        _.foreach(target.setVarName),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  add(name)
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, oldSize)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
