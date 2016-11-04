// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.util.prefs.Preferences
import java.awt.Font
import java.awt.event.{FocusEvent, InputEvent, KeyEvent}
import javax.swing.{ Action, JScrollPane, KeyStroke, ScrollPaneConstants }
import javax.swing.text.TextAction

import scala.collection.JavaConversions._
import org.nlogo.api.{ CompilerServices, Version }
import org.nlogo.core.I18N
import org.nlogo.ide.{ AutoSuggestAction, CodeCompletionPopup, JumpToDeclarationAction,
  NetLogoFoldParser, ShiftActions, ShowUsageBox, ShowUsageBoxAction, ToggleComments }
import org.nlogo.editor.{ AbstractEditorArea, AdvancedEditorArea, EditorArea, EditorConfiguration, EditorScrollPane, LineNumberScrollPane }
import org.nlogo.window.{ EditorColorizer, DefaultEditorFactory }

import org.fife.ui.rsyntaxtextarea.{ folding, AbstractTokenMakerFactory, TokenMakerFactory },
  folding.FoldParserManager
import org.fife.ui.rtextarea.RTextScrollPane

class EditorFactory(compiler: CompilerServices) extends DefaultEditorFactory(compiler) {
  override def defaultConfiguration(rows: Int, cols: Int): EditorConfiguration = {
    val codeCompletionPopup = new CodeCompletionPopup
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
        new AutoSuggestAction("auto-suggest", codeCompletionPopup))
      .addKeymap(
        KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK), shiftTabAction)
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

  override def newEditor(configuration: EditorConfiguration): AbstractEditorArea = {
    // This is a proxy for advanced editor fixtures required only by the main code tab
    // - RG 10/28/16
    if (configuration.highlightCurrentLine) {
      val tmf = TokenMakerFactory.getDefaultInstance.asInstanceOf[AbstractTokenMakerFactory]
      tmf.putMapping("netlogo",   "org.nlogo.ide.NetLogoTwoDTokenMaker")
      tmf.putMapping("netlogo3d", "org.nlogo.ide.NetLogoThreeDTokenMaker")
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
