// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.{ Font, GraphicsEnvironment }
import java.awt.event.{ InputEvent, KeyEvent, TextEvent, TextListener }

import javax.swing.{ Action, KeyStroke }
import javax.swing.text.TextAction

import org.nlogo.core.I18N

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

  protected val emptyListener =
    new TextListener() { override def textValueChanged(e: TextEvent) { } }

  def defaultMenuItems(colorizer: Colorizer): Seq[Action] =
    Seq(Actions.mouseQuickHelpAction(colorizer, I18N.gui.get _))

  def defaultActions(colorizer: Colorizer): Map[KeyStroke, TextAction] =
    Map(keystroke(KeyEvent.VK_F1) -> new QuickHelpAction(colorizer))

  def default(rows: Int, columns: Int, colorizer: Colorizer) =
    EditorConfiguration(rows, columns, defaultFont, emptyListener, colorizer, defaultActions(colorizer), defaultMenuItems(colorizer), false, false)
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
  highlightCurrentLine: Boolean) {

    def withFont(font: Font) =
      copy(font = font)
    def withListener(listener: TextListener) =
      copy(listener = listener)
    def withFocusTraversalEnabled(isEnabled: Boolean) =
      copy(enableFocusTraversal = isEnabled)
    def withCurrentLineHighlighted(isHighlighted: Boolean) =
      copy(highlightCurrentLine = isHighlighted)
    def withContextActions(actions: Seq[Action]) =
      copy(contextActions = contextActions ++ actions)
    def withKeymap(keymap: Map[KeyStroke, TextAction]) =
      copy(additionalActions = keymap)

    def configureEditorArea(editor: EditorArea) = {
      import InputEvent.{ SHIFT_MASK => ShiftKey }
      def keystroke(key: Int, mask: Int = 0): KeyStroke =
        KeyStroke.getKeyStroke(key, mask)

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

      additionalActions.foreach {
        case (k, v) => editor.getInputMap.put(k, v)
      }

      editor.setFocusTraversalKeysEnabled(enableFocusTraversal)
      if (enableFocusTraversal) {
        val focusTraversalListener = new FocusTraversalListener(editor)
        editor.addFocusListener(focusTraversalListener)
        editor.addMouseListener(focusTraversalListener)
        editor.getInputMap.put(keystroke(KeyEvent.VK_TAB),           new TransferFocusAction())
        editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, ShiftKey), new TransferFocusBackwardAction())
      } else {
        editor.getInputMap.put(keystroke(KeyEvent.VK_TAB),           Actions.tabKeyAction)
        editor.getInputMap.put(keystroke(KeyEvent.VK_TAB, ShiftKey), Actions.shiftTabKeyAction)
      }

      // add key binding, for getting quick "contexthelp", based on where
      // the cursor is...
      editor.getInputMap.put(keystroke(KeyEvent.VK_F1, 0), Actions.quickHelpAction(colorizer, I18N.gui.get _))

      val editorListener = new EditorListener(e => listener.textValueChanged(null))
      editorListener.install(editor)
    }
}
