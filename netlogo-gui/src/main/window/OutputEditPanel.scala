// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.I18N

class OutputEditPanel(target: OutputWidget) extends WidgetEditPanel(target) {
  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.output.fontSize"),
        () => target.fontSize,
        _.foreach(target.setFontSize),
        () => apply()))

  add(fontSize)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(fontSize)

  override def requestFocus(): Unit = {
    fontSize.requestFocus()
  }
}
