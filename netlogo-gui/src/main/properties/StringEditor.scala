// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.swing.Implicits._

abstract class StringEditor(accessor: PropertyAccessor[String], useTooltip: Boolean)
  extends PropertyEditor(accessor, useTooltip)
{
  val editor = makeEditor()
  setLayout(new java.awt.BorderLayout(BORDER_PADDING, 0))
  val label = new javax.swing.JLabel(accessor.displayName)
  tooltipFont(label)
  add(label, java.awt.BorderLayout.WEST)
  editor.getDocument().addDocumentListener({ () => changed() })
  add(editor, java.awt.BorderLayout.CENTER)
  def makeEditor() = new org.nlogo.swing.TextField(12)
  override def get = Option(editor.getText)
  override def set(value: String) { editor.setText(value) }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    c.weightx = 0.25
    c
  }
}
