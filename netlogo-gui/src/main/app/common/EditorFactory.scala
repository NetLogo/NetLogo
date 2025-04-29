// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.util.prefs.Preferences
import java.awt.{ Adjustable, Font, Graphics }
import java.awt.event.{InputEvent, KeyEvent}
import javax.swing.{ Action, KeyStroke }

import org.nlogo.api.{ CompilerServices, Version }
import org.nlogo.ide.{ AutoSuggestAction, CodeCompletionPopup, JumpToDeclarationAction,
  NetLogoFoldParser, NetLogoTokenMakerFactory, ShiftActions, ShowUsageBox, ShowUsageBoxAction, ToggleComments }
import org.nlogo.editor.{ AbstractEditorArea, AdvancedEditorArea, EditorConfiguration, EditorScrollPane }
import org.nlogo.nvm.ExtensionManager
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.DefaultEditorFactory

import org.fife.ui.rsyntaxtextarea.{ folding, TokenMakerFactory },
  folding.FoldParserManager
import org.fife.ui.rtextarea.RTextScrollPane

class EditorFactory(compiler: CompilerServices, extensionManager: ExtensionManager)
  extends DefaultEditorFactory(compiler) with ThemeSync {

  System.setProperty(TokenMakerFactory.PROPERTY_DEFAULT_TOKEN_MAKER_FACTORY,
    "org.nlogo.ide.NetLogoTokenMakerFactory")
  useExtensionManager(extensionManager)

  private val codeCompletionPopup = CodeCompletionPopup(compiler.dialect, extensionManager)

  def autoSuggestAction =
    new AutoSuggestAction("auto-suggest", codeCompletionPopup)

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
        KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), shiftTabAction)
      .withLineNumbers(
        Preferences.userRoot.node("/org/nlogo/NetLogo").get("line_numbers", "false").toBoolean)
      .forThreeDLanguage(Version.is3D)
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
          // this is needed because JScrollPane defines its own ScrollBar class (Isaac B 2/25/25)
          import org.nlogo.swing.{ ScrollBar => NLScrollBar }

          setHorizontalScrollBar(new NLScrollBar(Adjustable.HORIZONTAL))
          setVerticalScrollBar(new NLScrollBar(Adjustable.VERTICAL))

          def lineNumbersEnabled = getLineNumbersEnabled
          override def setFont(f: Font) = {
            super.setFont(f)
            Option(getGutter).foreach(_.setLineNumberFont(f))
          }

          override def paintComponent(g: Graphics): Unit = {
            getGutter.setBackground(InterfaceColors.codeBackground())
            getGutter.setBorderColor(InterfaceColors.codeSeparator())
          }
        }
        sp.setLineNumbersEnabled(editor.configuration.showLineNumbers)
        sp
      case _ => super.scrollPane(editor)
    }

  override def syncTheme(): Unit = {
    codeCompletionPopup.syncTheme()
  }
}
