// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

class DumbIndenter(code: AbstractEditorArea) extends IndenterInterface {
  def handleTab(): Unit = {
    code.replaceSelection("  ")
  }

  def handleCloseBracket(): Unit = { }

  def handleInsertion(text: String): Unit = { }

  def handleEnter(): Unit = {
    val doc = code.getDocument
    val currentLine = code.offsetToLine(doc, code.getSelectionStart)
    val lineStart = code.lineToStartOffset(doc, currentLine)
    val lineEnd = code.lineToEndOffset(doc, currentLine)
    val text = code.getText(lineStart, lineEnd - lineStart)

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
