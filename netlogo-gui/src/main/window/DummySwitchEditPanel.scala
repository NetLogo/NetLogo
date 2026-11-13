// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummySwitchEditPanel(target: DummySwitchWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      PropertyAccessor(
        target,
        loc"Tag",
        () => target.name,
        _.foreach(target.setVarName),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      PropertyAccessor(
        target,
        loc"Use old sizing",
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
