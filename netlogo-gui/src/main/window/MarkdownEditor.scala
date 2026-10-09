// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.border.LineBorder

import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, CheckBox, PreferredSize, ScrollPane, TextArea }
import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.theme.InterfaceColors

import scala.util.{ Success, Try }

class MarkdownEditor(accessor: PropertyAccessor[String])
  extends BoxColumn(6) with PropertyEditor(accessor) with PreferredSize {

  private val toggle = new CheckBox(accessor.name, toggleEditor)

  private val editor = new TextArea(6, 30) {
    setDragEnabled(false)
    setLineWrap(true)
    setWrapStyleWord(true)

    getDocument.addDocumentListener(() => accessor.changed())
  }

  private val scrollPane = new ScrollPane(editor)

  setBorder(null)

  add(new BoxRow(toggle, BoxAlign.Start))
  add(scrollPane)

  override def get: Try[String] =
    Success(editor.getText)

  override def set(value: String): Unit = {
    editor.setText(value)

    toggleEditor(value.trim.nonEmpty)
  }

  private def toggleEditor(enabled: Boolean): Unit = {
    toggle.setSelected(enabled)
    scrollPane.setVisible(enabled)

    revalidate()
    repaint()
  }

  override def syncTheme(): Unit = {
    toggle.setForeground(InterfaceColors.dialogText())

    scrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderEditable()))
    scrollPane.setBackground(InterfaceColors.textAreaBackground())

    editor.syncTheme()
  }
}
