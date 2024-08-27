// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ Action, JMenu, JPopupMenu }
import javax.swing.text.EditorKit

import org.fife.ui.rtextarea.RTextArea
import org.fife.ui.rsyntaxtextarea.{ RSyntaxTextArea, Theme }

class AdvancedEditorArea(val configuration: EditorConfiguration)
  extends RSyntaxTextArea(configuration.rows, configuration.columns) with AbstractEditorArea {

  var indenter = Option.empty[Indenter]

  // the language style is configured primarily in app.common.EditorFactory
  setSyntaxEditingStyle(if (configuration.is3Dlanguage) "netlogo3d" else "netlogo")
  setCodeFoldingEnabled(true)

  val theme =
    Theme.load(getClass.getResourceAsStream("/system/netlogo-editor-style.xml"))
  theme.apply(this)

  private val defaultSelectionColor = getSelectionColor

  configuration.configureAdvancedEditorArea(this)

  def enableBracketMatcher(enable: Boolean): Unit = {
    setBracketMatchingEnabled(enable)
  }

  override def getActions(): Array[Action] = {
    super.getActions.filter(_.getValue(Action.NAME) != "RSTA.GoToMatchingBracketAction").toArray[Action]
  }

  def resetUndoHistory(): Unit = {
    discardAllEdits()
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

  // This method will receive null input if a partial accent character is entered in the editor, e.g., via Option+e on
  // MacOS. This also occurs when int'l keyboards enter "^" -- BCH 12/31/2016, RGG 1/3/17
  override def replaceSelection(s: String): Unit = if (s != null) {
    val selection =
      s.dropWhile(c => Character.getType(c) == Character.FORMAT)
        .replaceAllLiterally("\t", "  ")
    super.replaceSelection(s)
    indenter.foreach(_.handleInsertion(selection))
  } else {
    super.replaceSelection(s)
  }

  // this needs to be implemented if we ever allow tab-based focus traversal
  // with this editor area
  def setSelection(s: Boolean): Unit = { }

  override def select(start: Int, end: Int) {
    setSelectionColor(AbstractEditorArea.ERROR_HIGHLIGHT)

    super.select(start, end)
  }

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      setSelectionColor(defaultSelectionColor)
    }
  })

  def undoAction = RTextArea.getAction(RTextArea.UNDO_ACTION)
  def redoAction = RTextArea.getAction(RTextArea.REDO_ACTION)

  // These methods are used only by the input widget, which uses editor.EditorArea
  // exclusively at present. - RG 10/28/16
  def getEditorKitForContentType(contentType: String): EditorKit = null
  def getEditorKit(): EditorKit =
    getUI.getEditorKit(this)
  def setEditorKit(kit: EditorKit): Unit = { }

  def beginCompoundEdit(): Unit = {
    beginAtomicEdit()
  }

  def endCompoundEdit(): Unit = {
    endAtomicEdit()
  }
}
