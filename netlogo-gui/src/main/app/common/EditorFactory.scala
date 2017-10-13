// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.util.prefs.Preferences
import java.awt.Font
import java.awt.event.{InputEvent, KeyEvent}
import javax.swing.{ Action, KeyStroke }

import org.nlogo.core.Dialect
import org.nlogo.ide.{ AutoSuggestAction, CodeCompletionPopup, JumpToDeclarationAction,
  NetLogoFoldParser, NetLogoTokenMakerFactory, ShiftActions, ShowUsageBox, ShowUsageBoxAction, ToggleComments }
import org.nlogo.editor.{ AbstractEditorArea, AdvancedEditorArea, EditorConfiguration, EditorScrollPane }
import org.nlogo.nvm.{ ExtensionManager, PresentationCompilerInterface }
import org.nlogo.window.DefaultEditorFactory

import org.fife.ui.rsyntaxtextarea.{ folding, TokenMakerFactory },
  folding.FoldParserManager
import org.fife.ui.rtextarea.RTextScrollPane

class EditorFactory(compiler: PresentationCompilerInterface, extensionManager: ExtensionManager, dialect: Dialect)
  extends DefaultEditorFactory(compiler, extensionManager) {
  System.setProperty(TokenMakerFactory.PROPERTY_DEFAULT_TOKEN_MAKER_FACTORY,
    "org.nlogo.ide.NetLogoTokenMakerFactory")
  useExtensionManager(extensionManager)

  def autoSuggestAction =
    new AutoSuggestAction("auto-suggest", CodeCompletionPopup(dialect, extensionManager))

  override def defaultConfiguration(rows: Int, cols: Int): EditorConfiguration = {
    val showUsageBox = new ShowUsageBox(colorizer)
    val shiftTabAction = new ShiftActions.LeftTab()
    val actions = Seq[Action](
      new ToggleComments(),
      new ShiftActions.Left(),
      new ShiftActions.Right(),
      new ShowUsageBoxAction(showUsageBox),
      new JumpToDeclarationAction())
    super.defaultConfiguration(rows, cols)
      .withContextActions(actions)
      .addKeymap(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK),
        autoSuggestAction)
      .addKeymap(
        KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK), shiftTabAction)
      .withLineNumbers(
        Preferences.userRoot.node("/org/nlogo/NetLogo").get("line_numbers", "false").toBoolean)
      .forThreeDLanguage(compiler.dialect.is3D)
  }

  def newEditor(configuration: EditorConfiguration, isApp: Boolean): AbstractEditorArea = {
    val editor = newEditor(configuration)

    if (isApp && configuration.rows > 1)
      editor.addFocusListener(new FindDialog.FocusListener)

    editor
  }

  def useExtensionManager(extensionManager: ExtensionManager): Unit = {
   val tmf = TokenMakerFactory.getDefaultInstance.asInstanceOf[NetLogoTokenMakerFactory]
   tmf.extensionManager = Some(extensionManager)
  }

  override def newEditor(configuration: EditorConfiguration): AbstractEditorArea = {
    // This is a proxy for advanced editor fixtures required only by the main code tab
    // - RG 10/28/16
    if (configuration.highlightCurrentLine) {
      FoldParserManager.get.addFoldParserMapping("netlogo", new NetLogoFoldParser())
      FoldParserManager.get.addFoldParserMapping("netlogo3d", new NetLogoFoldParser())
      new AdvancedEditorArea(configuration)
    } else
      super.newEditor(configuration)
  }

  override def scrollPane(editor: AbstractEditorArea): EditorScrollPane =
    editor match {
      case aea: AdvancedEditorArea =>
        val sp = new RTextScrollPane(aea) with EditorScrollPane {
          def lineNumbersEnabled = getLineNumbersEnabled
          override def setFont(f: Font) = {
            super.setFont(f)
            Option(getGutter).foreach(_.setLineNumberFont(f))
          }
        }
        sp.setLineNumbersEnabled(editor.configuration.showLineNumbers)
        sp
      case _ => super.scrollPane(editor)
    }
}
