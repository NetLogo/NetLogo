// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.text.JTextComponent
import RichDocument._

class DumbIndenter(code: JTextComponent) extends Indenter {
  private val TabSize = 2

  def handleTab(): Unit = {
    code.replaceSelection(" " * TabSize)
  }

  def handleUntab(): Unit = {
    val start = code.getSelectionStart
    val lineStart = code.getText.lastIndexOf('\n', start - 1) + 1
    val textStart = code.getText.indexWhere(!_.isWhitespace, lineStart)
    val remove = {
      if (textStart == -1) {
        TabSize.min(code.getText.size - lineStart)
      } else {
        TabSize.min(textStart - lineStart)
      }
    }

    if (remove > 0) {
      code.setCaretPosition(lineStart)
      code.moveCaretPosition(lineStart + remove)
      code.replaceSelection("")
      code.setCaretPosition((start - remove).max(0))
    }
  }

  def handleCloseBracket(): Unit = {
    code.replaceSelection("]")
  }

  def handleInsertion(text: String): Unit = { }

  def handleEnter(): Unit = {
    val doc = code.getDocument
    val currentLine = doc.offsetToLine(code.getSelectionStart)
    val lineStart = doc.lineToStartOffset(currentLine)
    doc.getLineText(currentLine).foreach { text =>
      val spaces = new StringBuilder("\n")
      var i = 0
      var break = false

      while (! break && i < text.length && lineStart + i < code.getSelectionStart) {
        val c = text.charAt(i)
        if (!Character.isWhitespace(c)) {
          break = true
        } else {
          spaces.append(c)
        }
        i += 1
      }
      code.replaceSelection(spaces.toString)
    }
  }

}
