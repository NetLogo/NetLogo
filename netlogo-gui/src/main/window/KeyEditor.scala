// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ BorderLayout, EventQueue }
import javax.swing.JLabel
import javax.swing.event.{ DocumentEvent, DocumentListener }

import org.nlogo.swing.{ FixedLengthDocument, TextField }
import org.nlogo.theme.InterfaceColors

class KeyEditor(accessor: PropertyAccessor[Char]) extends PropertyEditor(accessor) {
  private val label = new JLabel(accessor.name)

  // 2 not 1 here otherwise "W" doesn't fit - ST 1/18/05
  private val editor = new TextField(2, "", new FixedLengthDocument(1)) {
    setMaximumSize(getPreferredSize)

    // use a listener to make it so that after I type a character that character is selected, so if
    // I type another one it replaces the old one - ST 8/6/04
    val listener = new DocumentListener {
      def changedUpdate(e: DocumentEvent): Unit = {
        accessor.changed()
      }

      def removeUpdate(e: DocumentEvent): Unit = {
        accessor.changed()
      }

      def insertUpdate(e: DocumentEvent): Unit = {
        accessor.changed()
        // not quite sure why this won't work without the invokeLater - ST 8/6/04
        EventQueue.invokeLater{ () => selectAll() }
      }
    }

    getDocument.addDocumentListener(listener)
  }

  setLayout(new BorderLayout(6, 0))

  add(label, BorderLayout.WEST)
  add(editor, BorderLayout.CENTER)

  override def get: Option[Char] = Some(
    if (editor.getText.isEmpty) '\u0000'
    else editor.getText.charAt(0))

  override def set(value: Char): Unit = {
    editor.setText(if (value == '\u0000') ""
                   else value.toString)
  }

  override def requestFocus(): Unit = { editor.requestFocus() }

  override def syncTheme(): Unit = {
    label.setForeground(InterfaceColors.dialogText())

    editor.syncTheme()
  }
}
