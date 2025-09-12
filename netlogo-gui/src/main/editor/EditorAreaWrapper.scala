// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.JTextArea
import javax.swing.text.{ AbstractDocument, JTextComponent }

import org.nlogo.api.EditorAreaInterface

// wraps an EditorArea to satisfy EditorAreaInterface, for the benefit of SmartIndenter

class EditorAreaWrapper(text: JTextComponent) extends EditorAreaInterface {
  def getLineOfText(lineNum: Int) = {
    val lineStart = lineToStartOffset(lineNum)
    val lineEnd = lineToEndOffset(lineNum)
    getText(lineStart, lineEnd - lineStart)
  }
  def getCaretPosition: Int = text.getCaretPosition
  def setCaretPosition(pos: Int) = text.setCaretPosition(pos)
  def getText(start: Int, len: Int) = text.getDocument.getText(start, len)
  def getSelectionStart = text.getSelectionStart
  def getSelectionEnd = text.getSelectionEnd
  def setSelectionStart(pos: Int): Unit = { text.setSelectionStart(pos) }
  def setSelectionEnd(pos: Int): Unit = { text.setSelectionEnd(pos) }
  def offsetToLine(offset: Int) =
    text.getDocument.getDefaultRootElement.getElementIndex(offset)
  def lineToStartOffset(line: Int) =
    text.getDocument.getDefaultRootElement.getElement(line).getStartOffset
  def lineToEndOffset(line: Int) = {
    text.getDocument.getDefaultRootElement.getElement(line).getEndOffset
  }
  def insertString(pos: Int, spaces: String): Unit = {
    text.getDocument.insertString(pos, spaces, null)
  }
  def replaceSelection(s: String): Unit = { text.replaceSelection(s) }
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
        throw ex
      case ex: javax.swing.text.BadLocationException =>
        throw ex
    }
  }
  def remove(start: Int, len: Int): Unit = {
    try {
      text.getDocument.remove(start, len)
    } catch {
      case ex: javax.swing.text.BadLocationException =>
        throw ex
    }
  }
  override def beginCompoundEdit(): Unit = {
    text match {
      case a: AdvancedEditorArea => a.beginCompoundEdit()
      case _ => super.beginCompoundEdit()
    }
  }
  override def endCompoundEdit(): Unit = {
    text match {
      case a: AdvancedEditorArea => a.endCompoundEdit()
      case _ => super.endCompoundEdit()
    }
  }
}
