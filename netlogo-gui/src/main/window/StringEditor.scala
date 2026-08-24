// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import javax.swing.{ BoxLayout, JLabel }

import org.nlogo.swing.{ HorizontalStrut, TextField }
import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.theme.InterfaceColors

import scala.util.{ Success, Try }

class StringEditor(accessor: PropertyAccessor[String]) extends PropertyEditor(accessor) {
  private val label = new JLabel(accessor.name)
  private val editor = new TextField(12) {
    getDocument.addDocumentListener(() => accessor.changed())
  }

  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

  add(label)
  add(new HorizontalStrut(6))
  add(editor)

  override def get: Try[String] = Success(Option(editor.getText).getOrElse(""))
  override def set(value: String): Unit = { editor.setText(value) }

  override def setToolTipText(text: String): Unit = {
    label.setToolTipText(text)
  }

  override def requestFocus(): Unit = { editor.requestFocus() }

  override def hasFocus(): Boolean =
    editor.hasFocus

  override def getMaximumSize: Dimension =
    new Dimension(super.getMaximumSize.width, getPreferredSize.height)

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    editor.syncTheme()
  }
}
