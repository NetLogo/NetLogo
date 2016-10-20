// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Font, GraphicsEnvironment }
import java.awt.event.{ InputEvent, KeyEvent, TextEvent, TextListener },
  InputEvent.{ SHIFT_MASK => ShiftKey }

import javax.swing.{ Action, KeyStroke }
import javax.swing.text.{ JTextComponent, TextAction }

import org.nlogo.core.I18N
import KeyBinding._

object EditorConfiguration {
  private def os(s: String) =
    System.getProperty("os.name").startsWith(s)

  lazy val platformMonospacedFont =
    if (os("Mac"))
      "Menlo"
    else if (os("Windows"))
      GraphicsEnvironment.getLocalGraphicsEnvironment.getAvailableFontFamilyNames
        .find(_.equalsIgnoreCase("Lucida Console")).getOrElse("Monospaced")
    else "Monospaced"

  val defaultFont = new Font(platformMonospacedFont, Font.PLAIN, 12)

  private val emptyListener =
    new TextListener() { override def textValueChanged(e: TextEvent) { } }

  def defaultMenuItems(colorizer: Colorizer): Seq[Action] =
    Seq(new MouseQuickHelpAction(colorizer))

  def defaultActions(colorizer: Colorizer): Map[KeyStroke, TextAction] =
    Map(keystroke(KeyEvent.VK_F1) -> new KeyboardQuickHelpAction(colorizer))

  private val emptyMenu =
    new EditorMenu {
      def offerAction(action: Action): Unit = {}
    }

  def default(rows: Int, columns: Int, colorizer: Colorizer) =
    EditorConfiguration(rows, columns, defaultFont, emptyListener, colorizer, defaultActions(colorizer), defaultMenuItems(colorizer), false, false, false, emptyMenu)
}

case class EditorConfiguration(
  rows:                 Int,
  columns:              Int,
  font:                 Font,
  listener:             TextListener,
  colorizer:            Colorizer,
  additionalActions:    Map[KeyStroke, TextAction],
  contextActions:       Seq[Action],
  enableFocusTraversal: Boolean,
  highlightCurrentLine: Boolean,
  showLineNumbers:      Boolean,
  menu:                 EditorMenu) {

    def withFont(font: Font) =
      copy(font = font)
    def withListener(listener: TextListener) =
      copy(listener = listener)
    def withFocusTraversalEnabled(isEnabled: Boolean) =
      copy(enableFocusTraversal = isEnabled)
    def withCurrentLineHighlighted(isHighlighted: Boolean) =
      copy(highlightCurrentLine = isHighlighted)
    def withLineNumbers(show: Boolean) =
      copy(showLineNumbers = show)
    def withContextActions(actions: Seq[Action]) =
      copy(contextActions = contextActions ++ actions)
    def addKeymap(key: KeyStroke, action: TextAction) =
      copy(additionalActions = additionalActions + (key -> action))
    def withKeymap(keymap: Map[KeyStroke, TextAction]) =
      copy(additionalActions = keymap)
    def withMenu(newMenu: EditorMenu) =
      copy(menu = newMenu)

    def configureEditorArea(editor: EditorArea) = {

      editor.setEditorKit(new HighlightEditorKit(colorizer))

      val editorListener = new EditorListener(e => listener.textValueChanged(null))
      editorListener.install(editor)
      DocumentProperties.install(editor)

      val indenter = new DumbIndenter(editor)
      editor.setIndenter(indenter)

      if (highlightCurrentLine) {
        new LinePainter(editor)
      }

      editor.setFont(font)
      editor.setFocusTraversalKeysEnabled(enableFocusTraversal)

      if (enableFocusTraversal) {
        val focusTraversalListener = new FocusTraversalListener(editor)
        editor.addFocusListener(focusTraversalListener)
        editor.addMouseListener(focusTraversalListener)
        editor.getInputMap.put(keystroke(KeyEvent.VK_TAB),           new TransferFocusAction())
        editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, ShiftKey), new TransferFocusBackwardAction())
      } else {
        editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, ShiftKey), Actions.shiftTabKeyAction)
      }

      val indenter = new DumbIndenter(editor)
      editor.setIndenter(indenter)

      additionalActions.foreach {
        case (k, v) => editor.getInputMap.put(k, v)
      }

      val editorListener = new EditorListener(e => listener.textValueChanged(null))
      editorListener.install(editor)
    }

  def configureAdvancedEditorArea(editor: AbstractEditorArea) = {
    DocumentProperties.install(editor)

    val editorListener = new EditorListener(e => listener.textValueChanged(null))
    editorListener.install(editor)

    editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, ShiftKey), Actions.shiftTabKeyAction)

    val indenter = new DumbIndenter(editor)
    editor.setIndenter(indenter)

    contextActions.foreach {
      case e: EditorAwareAction => e.install(editor)
      case _ =>
    }

    additionalActions.foreach {
      case (k, v) => editor.getInputMap.put(k, v)
    }
  }

  def menuActions: Seq[Action] = additionalActions.values.toSeq ++
    Seq(Actions.PasteAction,
      Actions.CutAction,
      Actions.CopyAction,
      Actions.DeleteAction,
      Actions.SelectAllAction,
      Actions.commentToggleAction,
      Actions.shiftLeftAction,
      Actions.shiftRightAction,
      Actions.tabKeyAction,
      UndoManager.undoAction,
      UndoManager.redoAction)
}
