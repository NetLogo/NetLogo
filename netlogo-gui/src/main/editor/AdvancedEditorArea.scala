// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.text.Document

import org.fife.ui.rtextarea.RTextScrollPane
import org.fife.ui.rsyntaxtextarea.{ folding, AbstractTokenMakerFactory, RSyntaxTextArea, SyntaxConstants, TokenMakerFactory },
  folding.FoldParserManager

import org.nlogo.ide.NetLogoFoldParser

class AdvancedEditorArea(rows: Int, columns: Int) extends RSyntaxTextArea(rows, columns) with AbstractEditorArea {
  TokenMakerFactory.getDefaultInstance
    .asInstanceOf[AbstractTokenMakerFactory]
    .putMapping("netlogo", "org.nlogo.ide.NetLogoTokenMaker")

  FoldParserManager.get.addFoldParserMapping("netlogo", new NetLogoFoldParser())
  FoldParserManager.get.addFoldParserMapping("netlogo3d", new NetLogoFoldParser())

  setSyntaxEditingStyle("netlogo")
  setCodeFoldingEnabled(true)

  def enableBracketMatcher(enable: Boolean): Unit = {
    setBracketMatchingEnabled(enable)
  }

  def getEditorKit(): javax.swing.text.EditorKit = ???
  def getEditorKitForContentType(contentType: String): javax.swing.text.EditorKit = ???
  def setEditorKit(kit: javax.swing.text.EditorKit): Unit = ???
  def lineToEndOffset(doc: Document,line: Int): Int = ???
  def lineToStartOffset(doc: Document,line: Int): Int = ???
  def offsetToLine(doc: Document,line: Int): Int = ???
  def setIndenter(i: org.nlogo.editor.IndenterInterface): Unit = {
    println("indenter set but not needed!")
  }
  def setSelection(s: Boolean): Unit = {
    println(s"selection set to $s")
  }
}
