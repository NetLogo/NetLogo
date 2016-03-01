// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.api.CompilerServices

class EditorFactory(compiler: CompilerServices) extends org.nlogo.window.EditorFactory {
  override def newEditor(cols: Int, rows: Int, disableFocusTraversal: Boolean): org.nlogo.editor.AbstractEditorArea =
    newEditor(cols, rows, disableFocusTraversal, null, false)
  def newEditor(cols: Int, rows: Int, disableFocusTraversal: Boolean, listener: java.awt.event.TextListener, isApp: Boolean) =
    new org.nlogo.window.CodeEditor(
      rows, cols,
      new java.awt.Font(org.nlogo.awt.Fonts.platformMonospacedFont, java.awt.Font.PLAIN, 12),
      disableFocusTraversal, listener,
      new org.nlogo.window.EditorColorizer(compiler),
      org.nlogo.core.I18N.gui.get _)
}
