// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import org.nlogo.swing.Implicits._

abstract class KeyEditor(accessor: PropertyAccessor[Char])
  extends PropertyEditor(accessor)
{
  val editor = makeEditor()
  setLayout(new javax.swing.BoxLayout(this, javax.swing.BoxLayout.X_AXIS))
  add(new javax.swing.JLabel(accessor.displayName))
  add(javax.swing.Box.createHorizontalStrut(5))
  editor.getDocument.addDocumentListener(changed _)
  add(editor, java.awt.BorderLayout.CENTER)
  def makeEditor() = {
    val newEditor = new org.nlogo.swing.TextField(
      new org.nlogo.swing.FixedLengthDocument(1), "",
      // 2 not 1 here otherwise "W" doesn't fit - ST 1/18/05
      2)
    newEditor.setMaximumSize(newEditor.getPreferredSize)
    // use a listener to make it so that after I type a character that character is selected, so if
    // I type another one it replaces the old one - ST 8/6/04
    val listener = new javax.swing.event.DocumentListener() {
      def changedUpdate(e: javax.swing.event.DocumentEvent) { }
      def removeUpdate(e: javax.swing.event.DocumentEvent) { }
      def insertUpdate(e: javax.swing.event.DocumentEvent) {
        // not quite sure why this won't work without the invokeLater - ST 8/6/04
        java.awt.EventQueue.invokeLater{() => newEditor.selectAll()}
      }
    }
    newEditor.getDocument.addDocumentListener(listener)
    newEditor
  }
  override def get = Some(
    if(editor.getText().isEmpty) '\u0000'
    else editor.getText().charAt(0))
  override def set(value: Char) {
    editor.setText(if(value == '\u0000') ""
                   else value.toString)
  }
  override def requestFocus() { editor.requestFocus() }
  override def getConstraints = {
    val c = super.getConstraints
    c.fill = java.awt.GridBagConstraints.HORIZONTAL
    c.weightx = 0.25
    c
  }
}
