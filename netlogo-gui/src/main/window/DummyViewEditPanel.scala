// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N

class DummyViewEditPanel(target: DummyViewWidget) extends WidgetEditPanel(target) {
  private val widthEditor =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.view.width"),
        () => target.width,
        target.setWidth(_),
        () => apply()))

  private val heightEditor =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.view.height"),
        () => target.width,
        target.setWidth(_),
        () => apply()))

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(widthEditor, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(heightEditor, c)

    widthEditor.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(widthEditor, heightEditor)
}
