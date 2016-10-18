// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.text.{ Document, EditorKit, JTextComponent }

trait AbstractEditorArea extends JTextComponent {
  def enableBracketMatcher(enable: Boolean): Unit
  def getEditorKit(): EditorKit
  def setEditorKit(kit: EditorKit): Unit
  def getEditorKitForContentType(contentType: String): EditorKit

  def getText(start: Int, end: Int): String

  def setIndenter(i: IndenterInterface): Unit

  def setSelection(s: Boolean): Unit

  def offsetToLine(doc: Document, line: Int): Int
  def lineToStartOffset(doc: Document, line: Int): Int
  def lineToEndOffset(doc: Document, line: Int): Int
}
