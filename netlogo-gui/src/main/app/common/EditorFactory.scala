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
import org.nlogo.ide._
import org.nlogo.editor.{ AbstractEditorArea, AdvancedEditorArea, EditorArea, EditorConfiguration, EditorScrollPane, LineNumberScrollPane }
import org.nlogo.window.{ EditorColorizer, DefaultEditorFactory }

import org.fife.ui.rtextarea.RTextScrollPane

class EditorFactory(compiler: CompilerServices) extends DefaultEditorFactory(compiler) {
  override def defaultConfiguration(cols: Int, rows: Int): EditorConfiguration = {
    val codeCompletionPopup = new CodeCompletionPopup
    val showUsageBox = new ShowUsageBox(colorizer)
    val actions = Seq[Action](new ShowUsageBoxAction(showUsageBox), new JumpToDeclarationAction())
    super.defaultConfiguration(cols, rows)
      .withContextActions(actions)
      .addKeymap(
        KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK),
        new AutoSuggestAction("auto-suggest", codeCompletionPopup))
      .withLineNumbers(
        Preferences.userRoot.node("/org/nlogo/NetLogo").get("line_numbers", "false").toBoolean)
      .forThreeDLanguage(Version.is3D)
  }

  def newEditor(cols: Int, rows: Int, enableFocusTraversal: Boolean, enableHighlightCurrentLine: Boolean = false): AbstractEditorArea =
    newEditor(
      defaultConfiguration(cols, rows)
        .withFocusTraversalEnabled(enableFocusTraversal)
        .withCurrentLineHighlighted(enableHighlightCurrentLine),
        false)

  def newEditor(configuration: EditorConfiguration, isApp: Boolean): AbstractEditorArea = {
    val editor = newEditor(configuration)

    if (isApp && configuration.rows > 1)
      editor.addFocusListener(new FindDialog.FocusListener)

    editor
  }

  override def newEditor(configuration: EditorConfiguration): AbstractEditorArea = {
    if (configuration.rows == 100 && configuration.columns == 100) {
      val editor = new AdvancedEditorArea(configuration, 100, 100)
      configuration.configureAdvancedEditorArea(editor)
      editor
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
