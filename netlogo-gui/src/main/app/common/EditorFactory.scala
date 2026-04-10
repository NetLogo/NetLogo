// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.event.{InputEvent, KeyEvent}
import javax.swing.KeyStroke

import org.nlogo.api.{ CompilerServices, Version }
import org.nlogo.core.NetLogoPreferences
import org.nlogo.ide.{ AutoSuggestAction, CodeCompletionPopup, ShiftActions }
import org.nlogo.editor.{ EditorArea, EditorConfiguration, ToggleComments }
import org.nlogo.nvm.ExtensionManager
import org.nlogo.swing.UserAction.MenuAction
import org.nlogo.theme.ThemeSync
import org.nlogo.window.DefaultEditorFactory

class EditorFactory(compiler: CompilerServices, extensionManager: ExtensionManager)
  extends DefaultEditorFactory(compiler) with ThemeSync {

  private val codeCompletionPopup = CodeCompletionPopup(compiler.dialect, extensionManager)

  def autoSuggestAction =
    new AutoSuggestAction("auto-suggest", codeCompletionPopup)

  override def defaultConfiguration(rows: Int, cols: Int): EditorConfiguration = {
    val shiftTabAction = new ShiftActions.LeftTab()
    val actions = Seq[MenuAction](
      new ToggleComments(),
      new ShiftActions.Left(),
      new ShiftActions.Right()
    )
    super.defaultConfiguration(rows, cols)
      .withContextActions(actions)
      .addKeymap(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK),
        autoSuggestAction)
      .addKeymap(
        KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), shiftTabAction)
      .withLineNumbers(
        NetLogoPreferences.get("line_numbers", "false").toBoolean)
      .forThreeDLanguage(Version.is3D)
  }

  def newEditor(configuration: EditorConfiguration, isApp: Boolean): EditorArea = {
    val editor = newEditor(configuration)

    if (isApp && configuration.rows > 1)
      editor.addFocusListener(new FindDialog.FocusListener)

    editor
  }

  override def syncTheme(): Unit = {
    codeCompletionPopup.syncTheme()
  }
}
