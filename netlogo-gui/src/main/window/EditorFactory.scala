// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JScrollPane
import org.nlogo.editor.AbstractEditorArea

trait EditorFactory {
  def newEditor(cols: Int, rows: Int, enableFocusTraversal: Boolean, enableHighlightCurrentLine: Boolean): AbstractEditorArea
  def scrollPane(editor: AbstractEditorArea): JScrollPane
}
