// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Color, Component, Dimension }
import java.awt.event.{ KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }
import javax.swing.{ Action, JMenu, JMenuItem, JPopupMenu }
import javax.swing.text.EditorKit

import org.fife.ui.rtextarea.{ Gutter, RTextArea }
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea

import org.nlogo.swing.{ Menu, MenuItem, PopupMenu, UserAction, WrappedAction },
  UserAction.{ EditCategory, EditFoldGroup, EditUndoGroup, KeyBindings, MenuAction }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class AdvancedEditorArea(val configuration: EditorConfiguration)
  extends RSyntaxTextArea(configuration.rows, configuration.columns) with AbstractEditorArea {

  private lazy val toggleFoldsAction = new ToggleFoldsAction(this)

  private var gutter: Option[Gutter] = None

  var indenter: Option[Indenter] = None

  // the language style is configured primarily in app.common.EditorFactory
  setSyntaxEditingStyle(if (configuration.is3Dlanguage) "netlogo3d" else "netlogo")
  setCodeFoldingEnabled(true)

  private var defaultSelectionColor = getSelectionColor

  configuration.configureAdvancedEditorArea(this)

  def enableBracketMatcher(enable: Boolean): Unit = {
    setBracketMatchingEnabled(enable)
  }

  def setGutter(gutter: Gutter): Unit = {
    this.gutter = Option(gutter)
  }

  override def getActions(): Array[Action] = {
    super.getActions.filter(_.getValue(Action.NAME) != "RSTA.GoToMatchingBracketAction").toArray[Action]
  }

  override def additionalMenuActions: Seq[MenuAction] = {
    // this is a little jank, but we're probably going to switch to a different text component
    // before this one gets an update anyway. (Isaac B 7/19/25)
    super.createPopupMenu.getComponents.toSeq.collect {
      case menu: JMenu =>
        menu.getMenuComponents.collect {
          case item: JMenuItem => new WrappedAction(item.getAction, EditCategory, EditFoldGroup, item.getAccelerator)
        }
    }.flatten :+ toggleFoldsAction
  }

  def resetUndoHistory(): Unit = {
    discardAllEdits()
  }

  override def createPopupMenu(): PopupMenu = {
    new PopupMenu {
      // RSyntaxTextArea creates menu items that don't sync with the color theme,
      // so we have to convert them to the synced versions (Isaac B 11/5/24)
      AdvancedEditorArea.super.createPopupMenu.getComponents.foreach(_ match {
        case menu: JMenu => add(new Menu(menu.getText) {
          menu.getMenuComponents.foreach(_ match {
            case item: JMenuItem => add(new MenuItem(item.getAction))
            case _ =>
          })
          add(new MenuItem(toggleFoldsAction))
        })
        case item: JMenuItem => add(new MenuItem(item.getAction))
        case separator: JPopupMenu.Separator => addSeparator()
        case _ =>
      })

      addSeparator()

      configuration.contextActions.foreach(action => add(new MenuItem(action)))

      addPopupMenuListener(new SuspendCaretPopupListener(AdvancedEditorArea.this))

      override def show(component: Component, x: Int, y: Int): Unit = {
        setBackground(InterfaceColors.menuBackground())

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

  // without this helper, ToggleFoldsAction causes an inconsistent state where the line
  // numbers don't align with the procedures (Isaac B 7/21/25)
  def repaintGutter(): Unit = {
    gutter.foreach(_.repaint())
  }

  // This method will receive null input if a partial accent character is entered in the editor, e.g., via Option+e on
  // MacOS. This also occurs when int'l keyboards enter "^" -- BCH 12/31/2016, RGG 1/3/17
  override def replaceSelection(s: String): Unit = if (s != null) {
    val selection = s.dropWhile(c => Character.getType(c) == Character.FORMAT).replace("\t", "  ")
    super.replaceSelection(s)
    indenter.foreach(_.handleInsertion(selection))
  } else {
    super.replaceSelection(s)
  }

  // this needs to be implemented if we ever allow tab-based focus traversal
  // with this editor area
  def setSelection(s: Boolean): Unit = { }

  def selectNormal(): Unit = {
    setSelectionColor(defaultSelectionColor)

    select(getSelectionStart, getSelectionEnd)
  }

  def selectError(start: Int, end: Int): Unit = {
    setSelectionColor(InterfaceColors.errorHighlight())

    select(start, end)
  }

  override def setSelectionStart(i: Int): Unit = {
    Option(getFoldManager.getFoldForLine(getFoldManager.getVisibleLineAbove(getLineOfOffset(i))))
      .foreach(_.setCollapsed(false))

    super.setSelectionStart(i)
  }

  override def setSelectionEnd(i: Int): Unit = {
    Option(getFoldManager.getFoldForLine(getFoldManager.getVisibleLineAbove(getLineOfOffset(i))))
      .foreach(_.setCollapsed(false))

    super.setSelectionEnd(i)
  }

  override def select(start: Int, end: Int): Unit = {
    for (i <- 0 until getFoldManager.getFoldCount) {
      val fold = getFoldManager.getFold(i)

      if (fold.getStartOffset < end && fold.getEndOffset > start)
        fold.setCollapsed(false)
    }

    super.select(start, end)
  }

  def setDefaultSelectionColor(color: Color): Unit = {
    defaultSelectionColor = color

    setSelectionColor(color)
  }

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      setSelectionColor(defaultSelectionColor)
    }
  })

  addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = {
      setSelectionColor(defaultSelectionColor)
    }
  })

  def undoAction = new WrappedAction(RTextArea.getAction(RTextArea.UNDO_ACTION), EditCategory, EditUndoGroup,
                                     KeyBindings.keystroke('Z', withMenu = true))

  def redoAction = new WrappedAction(RTextArea.getAction(RTextArea.REDO_ACTION), EditCategory, EditUndoGroup,
                                     KeyBindings.keystroke('Y', withMenu = true))

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

  override def getPreferredSize: Dimension =
    new Dimension(super.getPreferredSize.width, getLineHeight * (getLineCount + 1))
}
