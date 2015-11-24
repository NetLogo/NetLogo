// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Color, Dimension }
import javax.swing.JTextPane
import javax.swing.event.{ DocumentEvent, DocumentListener }

class LineNumbersBar(editor: EditorArea[_]) extends JTextPane with DocumentListener {
  updateNumbers()
  setEnabled(false)
  setBackground(new Color(240, 240, 240))
  editor.getDocument.addDocumentListener(this)

  def updateNumbers() = {
    val lastLineNumber = editor.getText().filter(_=='\n').length + 1
    setText(1 to lastLineNumber mkString("\n"))
  }

  override def getPreferredSize = new Dimension(super.getPreferredSize.width, editor.getPreferredSize.height)
  override def getFont = editor.getFont

  def insertUpdate(e: DocumentEvent) = updateNumbers()
  def removeUpdate(e: DocumentEvent) = updateNumbers()
  def changedUpdate(e: DocumentEvent) = {}
}
