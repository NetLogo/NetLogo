// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.swing.Implicits._

abstract class StringEditor(accessor: PropertyAccessor[String])
  extends PropertyEditor(accessor)
{
  val editor = makeEditor()
  setLayout(new java.awt.BorderLayout(BORDER_PADDING, 0))
  add(new javax.swing.JLabel(accessor.displayName), java.awt.BorderLayout.WEST)
  editor.getDocument().addDocumentListener(changed _)
  add(editor, java.awt.BorderLayout.CENTER)
  def makeEditor() = new org.nlogo.swing.TextField(12)
  override def get = Option(editor.getText)
  override def set(value: String) { editor.setText(value) }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints() = {
    val c = super.getConstraints
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    c.weightx = 0.25
    c
  }
}
