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
        _.foreach(target.setText),
        () => apply()))

  private val fontSize =
    new IntegerEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.fontSize"),
        () => target.fontSize,
        _.foreach(target.setFontSize),
        () => apply()))

  private val textColorLight =
    new ColorEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.textLight"),
        () => target.textColorLight,
        _.foreach(target.setTextColorLight),
        () => apply()),
      frame)

  private val textColorDark =
    new ColorEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.textDark"),
        () => target.textColorDark,
        _.foreach(target.setTextColorDark),
        () => apply()),
      frame)

  private val backgroundLight =
    new ColorEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.backgroundLight"),
        () => target.backgroundLight,
        _.foreach(target.setBackgroundLight),
        () => apply()),
      frame)

  private val backgroundDark =
    new ColorEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.backgroundDark"),
        () => target.backgroundDark,
        _.foreach(target.setBackgroundDark),
        () => apply()),
      frame)

  private val markdown =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.text.markdown"),
        () => target.markdown,
        _.foreach(target.setMarkdown),
        () => apply()))

  locally {
    val c = new GridBagConstraints

    c.gridy = 0
    c.gridwidth = 2
    c.fill = GridBagConstraints.BOTH
    c.weightx = 1
    c.weighty = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(text, c)

    c.gridy = 1
    c.fill = GridBagConstraints.HORIZONTAL
    c.weighty = 0
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

    c.gridy = 4
    c.insets = new Insets(0, 6, 6, 6)

    add(markdown, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(text, fontSize, textColorLight, textColorDark, backgroundLight, backgroundDark, markdown)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    text.requestFocus()
  }
}
