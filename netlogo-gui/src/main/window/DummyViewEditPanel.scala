// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.LocaleContext

class DummyViewEditPanel(target: DummyViewWidget) extends WidgetEditPanel(target) {
  private val widthEditor =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        loc"Width (pixels)",
        () => target.width(),
        _.foreach(target.setWidth),
        () => apply()))

  private val heightEditor =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        loc"Height (pixels)",
        () => target.height(),
        _.foreach(target.setHeight),
        () => apply()))

  add(widthEditor)
  add(heightEditor)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(widthEditor, heightEditor)

  override def requestFocus(): Unit = {
    widthEditor.requestFocus()
  }
}
