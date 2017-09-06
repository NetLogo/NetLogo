// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.JTextArea
import javax.swing.text.{ AbstractDocument, JTextComponent }

// wraps an EditorArea to satisfy EditorAreaInterface, for the benefit of SmartIndenter

trait EditorAreaWrapper {
  def textComponent: JTextComponent
  def getLineOfText(lineNum: Int) = {
    val lineStart = lineToStartOffset(lineNum)
    val lineEnd = lineToEndOffset(lineNum)
    getText(lineStart, lineEnd - lineStart)
  }
  def getCaretPosition: Int = textComponent.getCaretPosition
  def setCaretPosition(pos: Int) = textComponent.setCaretPosition(pos)
  def getText(start: Int, len: Int) = textComponent.getDocument.getText(start, len)
  def getSelectionStart = textComponent.getSelectionStart
  def getSelectionEnd = textComponent.getSelectionEnd
  def setSelectionStart(pos: Int) { textComponent.setSelectionStart(pos) }
  def setSelectionEnd(pos: Int) { textComponent.setSelectionEnd(pos) }
  def offsetToLine(offset: Int) =
    textComponent.getDocument.getDefaultRootElement.getElementIndex(offset)
  def lineToStartOffset(line: Int) =
    textComponent.getDocument.getDefaultRootElement.getElement(line).getStartOffset
  def lineToEndOffset(line: Int) = {
    textComponent.getDocument.getDefaultRootElement.getElement(line).getEndOffset
  }
  def insertString(pos: Int, spaces: String) {
    textComponent.getDocument.insertString(pos, spaces, null)
  }
  def replaceSelection(s: String) { textComponent.replaceSelection(s) }
  def replace(start: Int, len: Int, str: String): Unit = {
    try {
      textComponent match {
        case textArea: JTextArea =>
          textArea.replaceRange(str, start, start + len)
        case _ =>
          textComponent.getDocument match {
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
      textComponent.getDocument.remove(start, len)
    } catch {
      case ex: javax.swing.text.BadLocationException =>
        println("errored removing: " + start + ", " + len)
        throw ex
    }
  }
  def beginCompoundEdit(): Unit = {
    textComponent match {
      case a: AdvancedEditorArea => a.beginCompoundEdit()
      case _ =>
    }
  }
  def endCompoundEdit(): Unit = {
    textComponent match {
      case a: AdvancedEditorArea => a.endCompoundEdit()
      case _ =>
    }
  }
}
