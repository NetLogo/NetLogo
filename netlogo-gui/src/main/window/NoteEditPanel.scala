// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import javax.swing.BoxLayout

import org.nlogo.awt.Hierarchy
import org.nlogo.core.I18N
import org.nlogo.swing.{ BoxColumn, BoxRow, HorizontalStrut, VerticalStrut, ZoomableBorder }

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

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(text)
  add(new VerticalStrut(6))
  add(fontSize)
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(
    new BoxColumn(Seq(
      textColorLight,
      new VerticalStrut(6),
      backgroundLight
    )),
    new HorizontalStrut(6),
    new BoxColumn(Seq(
      textColorDark,
      new VerticalStrut(6),
      backgroundDark
    ))
  )) {
    override def getMaximumSize: Dimension =
      new Dimension(super.getMaximumSize.width, getPreferredSize.height)
  })
  add(new VerticalStrut(6))
  add(markdown)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(text, fontSize, textColorLight, textColorDark, backgroundLight, backgroundDark, markdown)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    text.requestFocus()
  }
}
