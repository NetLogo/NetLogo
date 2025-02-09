// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.properties

import java.awt.EventQueue
import javax.swing.{ BoxLayout, JLabel }
import javax.swing.event.{ DocumentEvent, DocumentListener }

import org.nlogo.swing.Implicits._
import org.nlogo.swing.{ FixedLengthDocument, TextField }
import org.nlogo.theme.InterfaceColors

abstract class KeyEditor(accessor: PropertyAccessor[Char])
  extends PropertyEditor(accessor) {

  private val editor = makeEditor()
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  private val label = new JLabel(accessor.displayName)
  add(label)
  add(javax.swing.Box.createHorizontalStrut(5))
  editor.getDocument.addDocumentListener({ () => changed() })
  add(editor, java.awt.BorderLayout.CENTER)
  def makeEditor() = {
    // 2 not 1 here otherwise "W" doesn't fit - ST 1/18/05
    val newEditor = new TextField(2, "", new FixedLengthDocument(1))
    newEditor.setMaximumSize(newEditor.getPreferredSize)
    // use a listener to make it so that after I type a character that character is selected, so if
    // I type another one it replaces the old one - ST 8/6/04
    val listener = new DocumentListener {
      def changedUpdate(e: DocumentEvent) { }
      def removeUpdate(e: DocumentEvent) { }
      def insertUpdate(e: DocumentEvent) {
        // not quite sure why this won't work without the invokeLater - ST 8/6/04
        EventQueue.invokeLater{() => newEditor.selectAll()}
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

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.DIALOG_TEXT)

    editor.syncTheme()
  }
}
