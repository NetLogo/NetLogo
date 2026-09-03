// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ JLabel, ScrollPaneConstants, SwingConstants }
import javax.swing.border.LineBorder

import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.swing.{ BoxAlign, BoxColumn, BoxRow, ScrollPane, TextArea, Zoomable }
import org.nlogo.theme.InterfaceColors

import scala.util.{ Success, Try }

class BigStringEditor(accessor: PropertyAccessor[String]) extends BoxColumn(3) with PropertyEditor(accessor) {
  private val label = new JLabel(accessor.name) with Zoomable {
    setVerticalAlignment(SwingConstants.TOP)
  }

  private val editor = new TextArea(6, 30) {
    setDragEnabled(false)
    setLineWrap(true)
    setWrapStyleWord(true)
    getDocument.addDocumentListener(() => accessor.changed())
  }

  private val scrollPane = new ScrollPane(editor, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

  add(new BoxRow(label, BoxAlign.Start))
  add(scrollPane)

  override def get: Try[String] = Success(Option(editor.getText()).getOrElse(""))
  override def set(value: String): Unit = {
    editor.setText(value)
    editor.select(0, 0)
  }

  override def requestFocus(): Unit = { editor.requestFocus() }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    editor.syncTheme()

    scrollPane.setBorder(new LineBorder(InterfaceColors.textAreaBorderEditable()))
    scrollPane.setBackground(InterfaceColors.textAreaBackground())
  }
}
