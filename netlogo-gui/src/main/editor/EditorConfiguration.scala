// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Font, GraphicsEnvironment }
import java.awt.event.{ ActionEvent, KeyEvent, TextEvent, TextListener }
import java.awt.event.InputEvent.{ ALT_DOWN_MASK => AltKey, CTRL_DOWN_MASK => CtrlKey, SHIFT_DOWN_MASK => ShiftKey }
import javax.swing.{ Action, KeyStroke }
import javax.swing.text.{ DefaultEditorKit, TextAction }

import org.fife.ui.rtextarea.RTextAreaEditorKit

import org.nlogo.editor.KeyBinding._

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

  def defaultContextActions(colorizer: Colorizer): Seq[Action] =
    Seq(new MouseQuickHelpAction(colorizer))

  private val emptyMenu =
    new EditorMenu {
      def offerAction(action: Action): Unit = {}
    }

  def default(rows: Int, columns: Int, colorizer: Colorizer) =
    EditorConfiguration(rows, columns, defaultFont, emptyListener, colorizer, Map(), defaultContextActions(colorizer), Seq(), false, false, false, false, emptyMenu)
}

case class EditorConfiguration(
  rows:                 Int,
  columns:              Int,
  font:                 Font,
  listener:             TextListener,
  colorizer:            Colorizer,
  /* additionalActions are added to the input map and added to
   * top-level menus if appropriate */
  additionalActions:    Map[KeyStroke, TextAction],
  /* contextActions are presented in the right-click context menu */
  contextActions:       Seq[Action],
  /* menuActions are made available to top-level menus, but not otherwise available */
  menuActions:          Seq[Action],
  enableFocusTraversal: Boolean,
  highlightCurrentLine: Boolean,
  showLineNumbers:      Boolean,
  is3Dlanguage:         Boolean,
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
    def withMenuActions(actions: Seq[Action]) =
      copy(menuActions = menuActions ++ actions)
    def forThreeDLanguage(is3D: Boolean) =
      copy(is3Dlanguage = is3D)
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
      }

      additionalActions.foreach {
        case (k, v) => editor.getInputMap.put(k, v)
      }

      (contextActions ++ menuActions).foreach {
        case e: InstallableAction => e.install(editor)
        case _ =>
      }

      editor.getActionMap.put(DefaultEditorKit.previousWordAction, new CorrectPreviousWordAction(editor, false))
      editor.getActionMap.put(DefaultEditorKit.selectionPreviousWordAction, new CorrectPreviousWordAction(editor, true))
      editor.getActionMap.put(DefaultEditorKit.nextWordAction, new CorrectNextWordAction(editor, false))
      editor.getActionMap.put(DefaultEditorKit.selectionNextWordAction, new CorrectNextWordAction(editor, true))
      editor.getActionMap.put(DefaultEditorKit.selectWordAction, new CorrectSelectWordAction(editor))
      editor.getActionMap.put(DefaultEditorKit.deletePrevWordAction, new CorrectDeletePrevWordAction(editor))
      editor.getActionMap.put(DefaultEditorKit.deleteNextWordAction, new CorrectDeleteNextWordAction(editor))
    }

  def configureAdvancedEditorArea(editor: AbstractEditorArea) = {
    DocumentProperties.install(editor)

    val editorListener = new EditorListener(e => listener.textValueChanged(null))
    editorListener.install(editor)

    val indenter = new DumbIndenter(editor)
    editor.setIndenter(indenter)

    (contextActions ++ menuActions).foreach {
      case e: InstallableAction => e.install(editor)
      case _ =>
    }

    additionalActions.foreach {
      case (k, v) => editor.getInputMap.put(k, v)
    }

    if (EditorConfiguration.os("Mac")) {
      Seq(
        (KeyEvent.VK_BACK_SPACE, AltKey)  -> RTextAreaEditorKit.rtaDeletePrevWordAction,
        // Unfortunately, `rtaDeleteNextWordAction` is not implemented by rta.
        // Leaving this here for posterity wondering why alt-delete doesn't
        // work on Macs. - BCH 11/7/2016
        //(KeyEvent.VK_DELETE,     AltKey)  -> RTextAreaEditorKit.rtaDeleteNextWordAction,
        (KeyEvent.VK_A,          CtrlKey) -> DefaultEditorKit.beginLineAction,
        (KeyEvent.VK_E,          CtrlKey) -> DefaultEditorKit.endLineAction,
        (KeyEvent.VK_K,          CtrlKey) -> RTextAreaEditorKit.rtaDeleteRestOfLineAction
      ).foreach { case ((key, mod), action) => editor.getInputMap().put(KeyStroke.getKeyStroke(key, mod), action)}
    }
  }

  def permanentActions: Seq[Action] = additionalActions.values.toSeq ++ menuActions

  def editorOnlyActions: Seq[Action] = Seq()

  private class CorrectPreviousWordAction(editor: EditorArea, select: Boolean) extends TextAction("previous word") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (editor.getCaretPosition > 0) {
        if (editor.getText()(editor.getCaretPosition - 1).isLetterOrDigit) {
          while (editor.getCaretPosition > 0 && editor.getText()(editor.getCaretPosition - 1).isLetterOrDigit) {
            if (select) {
              editor.moveCaretPosition(editor.getCaretPosition - 1)
            } else {
              editor.setCaretPosition(editor.getCaretPosition - 1)
            }
          }
        } else if (select) {
          editor.moveCaretPosition(editor.getCaretPosition - 1)
        } else {
          editor.setCaretPosition(editor.getCaretPosition - 1)
        }
      }
    }
  }

  private class CorrectNextWordAction(editor: EditorArea, select: Boolean) extends TextAction("next word") {
    def actionPerformed(e: ActionEvent): Unit = {
      if (editor.getCaretPosition < editor.getText().size) {
        if (editor.getText()(editor.getCaretPosition).isLetterOrDigit) {
          while (editor.getCaretPosition < editor.getText().size &&
                 editor.getText()(editor.getCaretPosition).isLetterOrDigit) {
            if (select) {
              editor.moveCaretPosition(editor.getCaretPosition + 1)
            } else {
              editor.setCaretPosition(editor.getCaretPosition + 1)
            }
          }
        } else if (select) {
          editor.moveCaretPosition(editor.getCaretPosition + 1)
        } else {
          editor.setCaretPosition(editor.getCaretPosition + 1)
        }
      }
    }
  }

  private class CorrectSelectWordAction(editor: EditorArea) extends TextAction("select word") {
    def actionPerformed(e: ActionEvent): Unit = {
      new CorrectPreviousWordAction(editor, false).actionPerformed(e)
      new CorrectNextWordAction(editor, true).actionPerformed(e)
    }
  }

  private class CorrectDeletePrevWordAction(editor: EditorArea) extends TextAction("delete previous word") {
    def actionPerformed(e: ActionEvent): Unit = {
      new CorrectPreviousWordAction(editor, true).actionPerformed(e)

      editor.replaceSelection(null)
    }
  }

  private class CorrectDeleteNextWordAction(editor: EditorArea) extends TextAction("delete next word") {
    def actionPerformed(e: ActionEvent): Unit = {
      new CorrectNextWordAction(editor, true).actionPerformed(e)

      editor.replaceSelection(null)
    }
  }
}
