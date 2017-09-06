// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait EditorAreaInterface {
  def getSelectionStart: Int
  def getSelectionEnd: Int
  def getCaretPosition: Int
  def setCaretPosition(pos: Int): Unit
  def setSelectionStart(pos: Int)
  def setSelectionEnd(pos: Int)
  def offsetToLine(pos: Int): Int
  def lineToStartOffset(pos: Int): Int
  def lineToEndOffset(pos: Int): Int
  def getText(start: Int, len: Int): String
  def getLineOfText(lineNum: Int): String
  def insertString(pos: Int, str: String)
  def replaceSelection(text: String)
  def replace(start: Int, len: Int, str: String): Unit
  def remove(start: Int, len: Int)
  def beginCompoundEdit(): Unit
  def endCompoundEdit(): Unit
}
