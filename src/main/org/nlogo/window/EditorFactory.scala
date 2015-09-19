// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

trait EditorFactory {
  def newEditor(cols: Int, rows: Int, disableFocusTraversal: Boolean): org.nlogo.editor.AbstractEditorArea
}
