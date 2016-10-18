// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Font
import java.awt.event.{FocusEvent, InputEvent, KeyEvent}
import javax.swing.{ Action, JScrollPane, KeyStroke, ScrollPaneConstants }
import javax.swing.text.TextAction

import scala.collection.JavaConversions._
import org.nlogo.api.CompilerServices
import org.nlogo.awt.Fonts
import org.nlogo.core.I18N
import org.nlogo.ide._
import org.nlogo.editor.{ AbstractEditorArea, AdvancedEditorArea }
import org.nlogo.window.{CodeEditor, EditorColorizer, EditorFactory => WindowEditorFactory}

import org.fife.ui.rtextarea.RTextScrollPane

class EditorFactory(compiler: CompilerServices) extends WindowEditorFactory {
  def newEditor(cols: Int, rows: Int, enableFocusTraversal: Boolean, enableHighlightCurrentLine: Boolean = false): AbstractEditorArea =
    newEditor(cols, rows, enableFocusTraversal, enableHighlightCurrentLine, null, false)
  def newEditor(cols: Int,
                rows: Int,
                enableFocusTraversal: Boolean,
                enableHighlightCurrentLine: Boolean,
                listener: java.awt.event.TextListener,
                isApp: Boolean): AbstractEditorArea =
  {
    val font = new Font(Fonts.platformMonospacedFont, Font.PLAIN, 12)
    val colorizer = new EditorColorizer(compiler)
    val codeCompletionPopup = new CodeCompletionPopup
    val showUsageBox = new ShowUsageBox
    val actions = Seq[Action](new ShowUsageBoxAction(showUsageBox), new JumpToDeclarationAction())
    val actionMap = Map(
      KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, InputEvent.CTRL_DOWN_MASK) ->
        new AutoSuggestAction("auto-suggest", codeCompletionPopup))

    if (rows == 100 && cols == 100)
      new AdvancedEditorArea(100, 100)
    else {
      class MyCodeEditor
      extends CodeEditor(rows, cols, font, enableFocusTraversal,
        listener, colorizer, I18N.gui.get _, enableHighlightCurrentLine, actionMap, actions)
        {
          override def focusGained(fe: FocusEvent) {
            super.focusGained(fe)
            if(isApp && rows > 1)
              FindDialog.watch(this)
          }
          override def focusLost(fe: FocusEvent) {
            super.focusLost(fe)
            if(isApp && !fe.isTemporary)
              FindDialog.dontWatch(this)
          }
        }
        new MyCodeEditor
    }
  }

  def scrollPane(editor: AbstractEditorArea): JScrollPane =
    editor match {
      case aea: AdvancedEditorArea => new RTextScrollPane(aea)
      case _ =>
        new JScrollPane(
          editor,
          ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
          ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    }
}
