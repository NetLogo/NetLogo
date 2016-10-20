// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Font

import javax.swing.text.Document

import org.fife.ui.rtextarea.RTextScrollPane
import org.fife.ui.rsyntaxtextarea.{ folding, AbstractTokenMakerFactory, RSyntaxTextArea, SyntaxConstants, Theme, TokenMakerFactory },
  folding.FoldParserManager

import org.nlogo.ide.NetLogoFoldParser

class AdvancedEditorArea(configuration: EditorConfiguration, rows: Int, columns: Int)
  extends RSyntaxTextArea(rows, columns) with AbstractEditorArea {

  TokenMakerFactory.getDefaultInstance
    .asInstanceOf[AbstractTokenMakerFactory]
    .putMapping("netlogo", "org.nlogo.ide.NetLogoTokenMaker")

  var indenter = Option.empty[Indenter]

  FoldParserManager.get.addFoldParserMapping("netlogo", new NetLogoFoldParser())
  FoldParserManager.get.addFoldParserMapping("netlogo3d", new NetLogoFoldParser())

  setSyntaxEditingStyle("netlogo")
  setCodeFoldingEnabled(true)

  val theme =
    Theme.load(getClass.getResourceAsStream("/system/netlogo-editor-style.xml"))
  theme.apply(this)

  def enableBracketMatcher(enable: Boolean): Unit = {
    setBracketMatchingEnabled(enable)
  }

  def getEditorKit(): javax.swing.text.EditorKit = ???
  def getEditorKitForContentType(contentType: String): javax.swing.text.EditorKit = ???
  def setEditorKit(kit: javax.swing.text.EditorKit): Unit = ???
  def lineToEndOffset(doc: Document,line: Int): Int = ???
  def lineToStartOffset(doc: Document,line: Int): Int = ???
  def offsetToLine(doc: Document,line: Int): Int = ???
  def setIndenter(indenter: Indenter): Unit = {
    indenter.addActions(configuration, getInputMap)
    this.indenter = Some(indenter)
  }

  override def replaceSelection(s: String): Unit = {
    var selection =
      s.dropWhile(c => Character.getType(c) == Character.FORMAT)
        .replaceAllLiterally("\t", "  ")
    super.replaceSelection(s)
    indenter.foreach(_.handleInsertion(selection))
  }
  def setSelection(s: Boolean): Unit = {
    println(s"selection set to $s")
  }
}
