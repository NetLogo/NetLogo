// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.BoxLayout

import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxRow, HorizontalStrut, VerticalStrut, ZoomableBorder }

class DummyInputEditPanel(target: DummyInputBoxWidget) extends WidgetEditPanel(target) {
  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.tag"),
        () => target.name,
        _.foreach(target.setNameWrapper),
        () => apply()))

  private val typeOptions =
    new InputBoxEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.input.type"),
        () => target.typeOptions,
        _.foreach(target.setTypeOptions),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(name)
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(typeOptions, new HorizontalStrut(6), oldSize)))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(name, typeOptions, oldSize)

  override def requestFocus(): Unit = {
    name.requestFocus()
  }
}
