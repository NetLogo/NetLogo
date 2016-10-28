// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.event.KeyEvent
import javax.swing.JScrollPane

import org.nlogo.editor.{ AbstractEditorArea, Colorizer, EditorConfiguration, EditorScrollPane }
import org.nlogo.swing.UserAction

trait EditorFactory {
  def colorizer: Colorizer

  def defaultConfiguration(rows: Int, cols: Int): EditorConfiguration =
    EditorConfiguration.default(rows, cols, colorizer)
      .withMenuActions(Seq(
        TextMenuActions.CutAction,
        TextMenuActions.CopyAction,
        TextMenuActions.PasteAction,
        TextMenuActions.DeleteAction,
        TextMenuActions.SelectAllAction))
    .addKeymap(UserAction.KeyBindings.keystroke(KeyEvent.VK_F1), TextMenuActions.keyboardQuickHelp(colorizer))

  def newEditor(configuration: EditorConfiguration): AbstractEditorArea

  def scrollPane(editor: AbstractEditorArea): EditorScrollPane
}
