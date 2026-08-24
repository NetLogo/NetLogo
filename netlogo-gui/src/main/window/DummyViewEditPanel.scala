// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.BoxLayout

import org.nlogo.core.I18N
import org.nlogo.swing.{ VerticalStrut, ZoomableBorder }

class DummyViewEditPanel(target: DummyViewWidget) extends WidgetEditPanel(target) {
  private val widthEditor =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.view.width"),
        () => target.width(),
        _.foreach(target.setWidth),
        () => apply()))

  private val heightEditor =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.hubnet.view.height"),
        () => target.height(),
        _.foreach(target.setHeight),
        () => apply()))

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(widthEditor)
  add(new VerticalStrut(6))
  add(heightEditor)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(widthEditor, heightEditor)

  override def requestFocus(): Unit = {
    widthEditor.requestFocus()
  }
}
