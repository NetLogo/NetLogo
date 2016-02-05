// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.swing.Implicits._

abstract class BigStringEditor(accessor: PropertyAccessor[String])
  extends PropertyEditor(accessor)
{
  setLayout(new java.awt.BorderLayout(BORDER_PADDING, 0))
  val label = new javax.swing.JLabel(accessor.displayName)
  label.setVerticalAlignment(javax.swing.SwingConstants.TOP)
  add(label, java.awt.BorderLayout.NORTH)
  private val editor = new javax.swing.JTextArea(6, 30)
  editor.setDragEnabled(false)
  editor.setLineWrap(true)
  editor.setWrapStyleWord(true)
  editor.getDocument().addDocumentListener(changed _)
  add(new javax.swing.JScrollPane(editor,
                                  javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                                  javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
      java.awt.BorderLayout.CENTER)
  override def get = Option(editor.getText())
  override def set(value: String) {
    editor.setText(value)
    editor.select(0, 0)
  }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = java.awt.GridBagConstraints.BOTH
    c.weightx = 1.0
    c.weighty = 1.0
    c
  }
}
