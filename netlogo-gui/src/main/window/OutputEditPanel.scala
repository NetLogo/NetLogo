// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.BoxLayout

import org.nlogo.core.I18N
import org.nlogo.swing.ZoomableBorder

class OutputEditPanel(target: OutputWidget) extends WidgetEditPanel(target) {
  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.output.fontSize"),
        () => target.fontSize,
        _.foreach(target.setFontSize),
        () => apply()))

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(fontSize)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(fontSize)

  override def requestFocus(): Unit = {
    fontSize.requestFocus()
  }
}
