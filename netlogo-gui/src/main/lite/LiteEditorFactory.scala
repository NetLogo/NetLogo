// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.window.{ CodeEditor, EditorColorizer, EditorFactory }
import org.nlogo.awt.Fonts.platformMonospacedFont

class LiteEditorFactory(compiler: CompilerServices) extends EditorFactory {
  override def newEditor(cols: Int, rows: Int, enableFocusTraversal: Boolean) = {
    val font = new java.awt.Font(
      platformMonospacedFont, java.awt.Font.PLAIN, 12)
    new CodeEditor(
      cols, rows, font, enableFocusTraversal, null,
      new EditorColorizer(compiler), I18N.gui.fn)
  }
}

