// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.Component
import java.awt.event.{ KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }
import javax.swing.{ Action, JMenu, JMenuItem, JPopupMenu }
import javax.swing.text.EditorKit

import org.fife.ui.rtextarea.RTextArea
import org.fife.ui.rsyntaxtextarea.{ RSyntaxTextArea, Theme }

import org.nlogo.swing.{ Menu, MenuItem, PopupMenu }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

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

  override def createPopupMenu(): PopupMenu = {
    new PopupMenu {
      // RSyntaxTextArea creates menu items that don't sync with the color theme,
      // so we have to convert them to the synced versions (IB 11/5/24)
      AdvancedEditorArea.super.createPopupMenu.getComponents.foreach(_ match {
        case menu: JMenu => add(new Menu(menu.getText) {
          menu.getMenuComponents.foreach(_ match {
            case item: JMenuItem => add(new MenuItem(item.getAction))
          })
          add(new MenuItem(new ToggleFoldsAction(AdvancedEditorArea.this)))
        })
        case item: JMenuItem => add(new MenuItem(item.getAction))
        case separator: JPopupMenu.Separator => addSeparator()
      })

      addSeparator()

      configuration.contextActions.foreach(action => add(new MenuItem(action)))

      addPopupMenuListener(new SuspendCaretPopupListener(AdvancedEditorArea.this))

      override def show(component: Component, x: Int, y: Int) {
        setBackground(InterfaceColors.MENU_BACKGROUND)

        getComponents.foreach(_ match {
          case ts: ThemeSync => ts.syncTheme()
          case _ =>
        })

        super.show(component, x, y)
      }
    }
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

  def selectError(start: Int, end: Int) {
    setSelectionColor(InterfaceColors.ERROR_HIGHLIGHT)

    select(start, end)
  }

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent) {
      setSelectionColor(defaultSelectionColor)
    }
  })

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent) {
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
