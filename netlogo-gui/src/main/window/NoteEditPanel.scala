// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.awt.Hierarchy
import org.nlogo.core.I18N

class NoteEditPanel(target: NoteWidget) extends WidgetEditPanel(target) {
  private val frame = Hierarchy.getFrame(this)

  private val text =
    new BigStringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.text"),
        () => target.text,
        target.setText(_),
        () => apply()))

  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.fontSize"),
        () => target.fontSize,
        target.setFontSize(_),
        () => apply()))

  private val textColorLight =
    new ColorEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.textLight"),
        () => target.textColorLight,
        target.setTextColorLight(_),
        () => apply()),
      frame)

  private val textColorDark =
    new ColorEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.textDark"),
        () => target.textColorDark,
        target.setTextColorDark(_),
        () => apply()),
      frame)

  private val backgroundLight =
    new ColorEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.backgroundLight"),
        () => target.backgroundLight,
        target.setBackgroundLight(_),
        () => apply()),
      frame)

  private val backgroundDark =
    new ColorEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.backgroundDark"),
        () => target.backgroundDark,
        target.setBackgroundDark(_),
        () => apply()),
      frame)

  locally {
    val c = new GridBagConstraints

    c.gridy = 0
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(text, c)

    c.gridy = 1
    c.insets = new Insets(0, 6, 6, 6)

    add(fontSize, c)

    c.gridy = 2
    c.gridwidth = 1

    add(textColorLight, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(textColorDark, c)

    c.gridy = 3
    c.insets = new Insets(0, 6, 6, 6)

    add(backgroundLight, c)

    c.insets = new Insets(0, 0, 6, 6)

    add(backgroundDark, c)

    text.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(text, fontSize, textColorLight, textColorDark, backgroundLight, backgroundDark)

  override def isResizable: Boolean = true

  override def syncTheme(): Unit = {
    text.syncTheme()
    fontSize.syncTheme()
    textColorLight.syncTheme()
    textColorDark.syncTheme()
    backgroundLight.syncTheme()
    backgroundDark.syncTheme()
  }
}
