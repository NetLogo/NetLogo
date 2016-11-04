// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import org.nlogo.api.EditorAreaInterface
import org.nlogo.editor.EditorArea
import javax.swing.text.JTextComponent

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
  def lineToEndOffset(line: Int) =
    text.getDocument.getDefaultRootElement.getElement(line).getEndOffset
  def insertString(pos: Int, spaces: String) {
    text.getDocument.insertString(pos, spaces, null)
  }
  def replaceSelection(s: String) { text.replaceSelection(s) }
  def remove(start: Int, len: Int) {
    text.getDocument.remove(start, len)
  }
}
