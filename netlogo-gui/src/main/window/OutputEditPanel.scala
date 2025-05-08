// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N

class OutputEditPanel(target: OutputWidget) extends WidgetEditPanel(target) {
  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.output.fontSize"),
        () => target.fontSize,
        target.setFontSize(_),
        () => apply()))

  locally {
    val c = new GridBagConstraints

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(fontSize, c)

    fontSize.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(fontSize)
}
