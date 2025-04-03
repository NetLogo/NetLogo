// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.BorderLayout
import javax.swing.JLabel

import org.nlogo.api.Dump
import org.nlogo.swing.Implicits.thunk2documentListener
import org.nlogo.swing.TextField
import org.nlogo.theme.InterfaceColors

import util.control.Exception.catching

class DoubleEditor(accessor: PropertyAccessor[Double]) extends PropertyEditor(accessor) {
  private val label = new JLabel(accessor.name)
  private val editor = new TextField(8) {
    getDocument.addDocumentListener(() => accessor.changed())
  }

  setLayout(new BorderLayout(6, 0))

  add(label, BorderLayout.WEST)
  add(editor, BorderLayout.CENTER)

  override def setEnabled(enabled: Boolean): Unit = {
    super.setEnabled(enabled)
    editor.setEnabled(enabled)
    label.setEnabled(enabled)
  }

  override def get: Option[Double] =
    catching(classOf[NumberFormatException]) opt editor.getText.toDouble
  override def set(value: Double): Unit = { editor.setText(Dump.number(value)) }

  override def requestFocus(): Unit = { editor.requestFocus() }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    editor.syncTheme()
  }
}

class StrictlyPositiveDoubleEditor(accessor: PropertyAccessor[Double]) extends DoubleEditor(accessor) {
  override def get: Option[Double] =
    super.get.filter(_ > 0)
}
