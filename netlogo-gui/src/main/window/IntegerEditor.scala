// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JLabel

import org.nlogo.swing.TextField
import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.theme.InterfaceColors

import util.control.Exception.catching

class IntegerEditor(accessor: PropertyAccessor[Int]) extends PropertyEditor(accessor) with WorldIntegerEditor {
  private val label = new JLabel(accessor.name)
  private val editor = new TextField(8) {
    getDocument.addDocumentListener(() => accessor.changed())
  }

  add(label)
  add(editor)

  override def get: Option[Int] =
    catching(classOf[NumberFormatException]).opt(editor.getText.toInt)
  override def set(value: Int): Unit = { editor.setText(value.toString) }

  override def requestFocus(): Unit = { editor.requestFocus() }
  override def setEnabled(enabled: Boolean): Unit = {
    super.setEnabled(enabled)
    editor.setEnabled(enabled)
    label.setEnabled(enabled)
  }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    editor.syncTheme()
  }
}
