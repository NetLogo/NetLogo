// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.{ Action, JMenu, JPopupMenu }
import javax.swing.text.EditorKit

import org.fife.ui.rsyntaxtextarea.{ folding, AbstractTokenMakerFactory, RSyntaxTextArea, Theme, TokenMakerFactory },
  folding.FoldParserManager

import org.nlogo.ide.NetLogoFoldParser
import KeyBinding._

class AdvancedEditorArea(val configuration: EditorConfiguration, rows: Int, columns: Int)
  extends RSyntaxTextArea(rows, columns) with AbstractEditorArea {

  val tmf = TokenMakerFactory.getDefaultInstance.asInstanceOf[AbstractTokenMakerFactory]
  tmf.putMapping("netlogo",   "org.nlogo.ide.NetLogoTwoDTokenMaker")
  tmf.putMapping("netlogo3d", "org.nlogo.ide.NetLogoThreeDTokenMaker")

  var indenter = Option.empty[Indenter]

  FoldParserManager.get.addFoldParserMapping("netlogo", new NetLogoFoldParser())
  FoldParserManager.get.addFoldParserMapping("netlogo3d", new NetLogoFoldParser())

  setSyntaxEditingStyle(if (configuration.is3Dlanguage) "netlogo3d" else "netlogo")
  setCodeFoldingEnabled(true)

  val theme =
    Theme.load(getClass.getResourceAsStream("/system/netlogo-editor-style.xml"))
  theme.apply(this)

  def enableBracketMatcher(enable: Boolean): Unit = {
    setBracketMatchingEnabled(enable)
  }

  override def getActions(): Array[Action] = {
    super.getActions.filter(_.getValue(Action.NAME) != "RSTA.GoToMatchingBracketAction").toArray[Action]
  }

  override def createPopupMenu(): JPopupMenu = {
    val popupMenu = super.createPopupMenu
    val toggleFolds = new ToggleFoldsAction(this)
    popupMenu.getComponents.last match {
      case foldMenu: JMenu => foldMenu.add(toggleFolds)
      case _               => popupMenu.add(toggleFolds)
    }
    popupMenu.addSeparator()
    configuration.contextActions.foreach(popupMenu.add)
    popupMenu.addPopupMenuListener(new SuspendCaretPopupListener(this))
    popupMenu
  }

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

  // this needs to be implemented if we ever allow tab-based focus traversal
  // with this editor area
  def setSelection(s: Boolean): Unit = { }

  def getEditorKit(): javax.swing.text.EditorKit = ???
  def getEditorKitForContentType(contentType: String): javax.swing.text.EditorKit = ???
  def setEditorKit(kit: javax.swing.text.EditorKit): Unit = ???
}
