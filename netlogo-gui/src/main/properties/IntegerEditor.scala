// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import util.control.Exception.catching
import org.nlogo.swing.Implicits._

abstract class IntegerEditor(accessor: PropertyAccessor[Int])
  extends PropertyEditor(accessor)
{
  val editor = new org.nlogo.swing.TextField(8)
  setLayout(new java.awt.BorderLayout(BORDER_PADDING, 0))
  private val label = new javax.swing.JLabel(accessor.displayName)
  add(label, java.awt.BorderLayout.WEST)
  editor.getDocument().addDocumentListener(changed _)
  add(editor, java.awt.BorderLayout.CENTER)
  override def setEnabled(enabled: Boolean) {
    super.setEnabled(enabled)
    editor.setEnabled(enabled)
    label.setEnabled(enabled)
  }
  override def get =
    catching(classOf[NumberFormatException])
      .opt(editor.getText.toInt)
  override def set(value: Int) { editor.setText(value.toString) }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    c.weightx = 0.025
    c
  }
}
