// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.LocaleContext
import org.nlogo.swing.{ BoxAlign, BoxRow }

class DummyInputEditPanel(target: DummyInputBoxWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        loc"Tag",
        () => target.name,
        _.foreach(target.setNameWrapper),
        () => apply()))

  private val typeOptions =
    new InputBoxEditor(
      new PropertyAccessor(
        target,
        loc"Type",
        () => target.typeOptions,
        _.foreach(target.setTypeOptions),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        loc"Use old sizing",
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  add(name)
  add(new BoxRow(Seq(typeOptions, oldSize), 6, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, typeOptions, oldSize)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
