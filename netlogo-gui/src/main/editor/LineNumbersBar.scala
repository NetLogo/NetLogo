// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Color, Dimension }
import javax.swing.JTextPane
import javax.swing.event.{ DocumentEvent, DocumentListener }
import javax.swing.text.{ DefaultCaret, JTextComponent }

class LineNumbersBar(editor: JTextComponent) extends JTextPane with DocumentListener {
  private var previousLinesCount = -1

  updateNumbers()
  setEnabled(false)
  setBackground(new Color(240, 240, 240))
  setBorder(editor.getBorder)
  getCaret.asInstanceOf[DefaultCaret].setUpdatePolicy(DefaultCaret.NEVER_UPDATE)
  editor.getDocument.addDocumentListener(this)

  def updateNumbers() = {
    val linesCount = editor.getText().count(_=='\n') + 1
    if(linesCount != previousLinesCount) {
      setText(1 to linesCount mkString("\n"))
      previousLinesCount = linesCount
    }
  }

  override def getPreferredSize = new Dimension(super.getPreferredSize.width, editor.getPreferredSize.height)
  override def getFont = editor.getFont

  def insertUpdate(e: DocumentEvent) = updateNumbers()
  def removeUpdate(e: DocumentEvent) = updateNumbers()
  def changedUpdate(e: DocumentEvent) = {}
}
