// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.{ BorderLayout, GridBagConstraints }
import javax.swing.JLabel

import org.nlogo.api.Dump
import org.nlogo.swing.Implicits._
import org.nlogo.swing.TextField
import org.nlogo.theme.InterfaceColors

import util.control.Exception.catching

abstract class DoubleEditor(accessor: PropertyAccessor[Double])
  extends PropertyEditor(accessor) {

  private val editor = new TextField(8)
  setLayout(new BorderLayout(BORDER_PADDING, 0))
  private val label = new JLabel(accessor.displayName)
  add(label, BorderLayout.WEST)
  editor.getDocument().addDocumentListener({ () => changed() })
  add(editor, BorderLayout.CENTER)
  override def setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    editor.setEnabled(enabled)
    label.setEnabled(enabled)
  }
  override def get =
    catching(classOf[NumberFormatException]) opt editor.getText().toDouble
  override def set(value: Double) { editor.setText(Dump.number(value)) }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 0.1
    c
  }

  def syncTheme() {
    label.setForeground(InterfaceColors.DIALOG_TEXT)

    editor.syncTheme()
  }
}
