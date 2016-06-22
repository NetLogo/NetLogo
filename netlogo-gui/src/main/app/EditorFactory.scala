// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import javax.swing.KeyStroke
import javax.swing.text.TextAction

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.ide.{AutoSuggestAction, CodeCompletionPopup}
import collection.JavaConversions._

class EditorFactory(compiler: CompilerServices) extends org.nlogo.window.EditorFactory {
  def newEditor(cols: Int, rows: Int, disableFocusTraversal: Boolean) =
    newEditor(cols, rows, disableFocusTraversal, null, false)
  def newEditor(cols: Int,
                rows: Int,
                disableFocusTraversal: Boolean,
                listener: java.awt.event.TextListener,
                isApp: Boolean) =
  {
    val font = new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont,
                                 java.awt.Font.PLAIN, 12)
    val colorizer = new org.nlogo.window.EditorColorizer(compiler)
    val codeCompletionPopup = new CodeCompletionPopup()
    val actionMap = mapAsJavaMap(Map(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.CTRL_DOWN_MASK)
      -> new AutoSuggestAction("auto-suggest", codeCompletionPopup))).asInstanceOf[java.util.Map[KeyStroke, TextAction]]
    class MyCodeEditor
    extends org.nlogo.window.CodeEditor(rows, cols, font, disableFocusTraversal,
                                        listener, colorizer, I18N.gui.get _, actionMap)
    {
      override def focusGained(fe: java.awt.event.FocusEvent) {
        super.focusGained(fe)
        if(isApp && rows > 1)
          FindDialog.watch(this)
      }
      override def focusLost(fe: java.awt.event.FocusEvent) {
        super.focusLost(fe)
        if(isApp && !fe.isTemporary)
          FindDialog.dontWatch(this)
      }
    }
    new MyCodeEditor
  }
}
