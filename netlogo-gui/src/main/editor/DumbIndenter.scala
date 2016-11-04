// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.text.JTextComponent
import RichDocument._

class DumbIndenter(code: JTextComponent) extends Indenter {
  def handleTab(): Unit = {
    code.replaceSelection("  ")
  }

  def handleCloseBracket(): Unit = { }

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
