// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.BorderLayout
import javax.swing.JLabel

import org.nlogo.swing.TextField
import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.theme.InterfaceColors

import scala.util.{ Success, Try }

class IntegerEditor(accessor: PropertyAccessor[Int]) extends PropertyEditor(accessor) with WorldIntegerEditor {
  private val label = new JLabel(accessor.name)
  private val editor = new TextField(8) {
    getDocument.addDocumentListener(() => accessor.changed())
  }

  setLayout(new BorderLayout(6, 0))

  add(label, BorderLayout.WEST)
  add(editor, BorderLayout.CENTER)

  override def get: Try[Int] = editor.getText.toIntOption.fold(defaultError)(Success(_))
  override def set(value: Int): Unit = { editor.setText(value.toString) }

  override def setToolTipText(text: String): Unit = {
    label.setToolTipText(text)
  }

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

class NegativeIntegerEditor(accessor: PropertyAccessor[Int]) extends IntegerEditor(accessor) {
  override def get: Try[Int] =
    super.get.filter(_ <= 0).orElse(defaultError)
}

class PositiveIntegerEditor(accessor: PropertyAccessor[Int]) extends IntegerEditor(accessor) {
  override def get: Try[Int] =
    super.get.filter(_ >= 0).orElse(defaultError)
}
