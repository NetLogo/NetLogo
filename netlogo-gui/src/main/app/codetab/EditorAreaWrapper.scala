// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import org.nlogo.api.EditorAreaInterface
import org.nlogo.editor.EditorArea
import javax.swing.JTextArea
import javax.swing.text.{ AbstractDocument, JTextComponent }

// wraps an EditorArea to satisfy EditorAreaInterface, for the benefit of SmartIndenter

class EditorAreaWrapper(text: JTextComponent) extends EditorAreaInterface {
  def getLineOfText(lineNum: Int) = {
    val lineStart = lineToStartOffset(lineNum)
    val lineEnd = lineToEndOffset(lineNum)
    getText(lineStart, lineEnd - lineStart)
  }
  def getText(start: Int, len: Int) = text.getDocument.getText(start, len)
  def getSelectionStart = text.getSelectionStart
  def getSelectionEnd = text.getSelectionEnd
  def setSelectionStart(pos: Int) { text.setSelectionStart(pos) }
  def setSelectionEnd(pos: Int) { text.setSelectionEnd(pos) }
  def offsetToLine(offset: Int) =
    text.getDocument.getDefaultRootElement.getElementIndex(offset)
  def lineToStartOffset(line: Int) =
    text.getDocument.getDefaultRootElement.getElement(line).getStartOffset
  def lineToEndOffset(line: Int) = {
    text.getDocument.getDefaultRootElement.getElement(line).getEndOffset
  }
  def insertString(pos: Int, spaces: String) {
    text.getDocument.insertString(pos, spaces, null)
  }
  def replaceSelection(s: String) { text.replaceSelection(s) }
  def replace(start: Int, len: Int, str: String): Unit = {
    try {
      text match {
        case textArea: JTextArea =>
          textArea.replaceRange(str, start, start + len)
        case _ =>
          text.getDocument match {
            case abstractDoc: AbstractDocument =>
              abstractDoc.replace(start, len, str, null)
            case doc =>
              doc.remove(start, len)
              doc.insertString(start, str, null)
          }
      }
    } catch {
      case ex: IllegalArgumentException =>
        println("errored replacing: " + start + ", " + len + " with: " + str)
        throw ex
      case ex: javax.swing.text.BadLocationException =>
        println("errored replacing: " + start + ", " + len + " with: " + str)
        throw ex
    }
  }
  def remove(start: Int, len: Int) {
    try {
      text.getDocument.remove(start, len)
    } catch {
      case ex: javax.swing.text.BadLocationException =>
        println("errored removing: " + start + ", " + len)
        throw ex
    }
  }
}
