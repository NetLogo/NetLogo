// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.api.{ ParserServices, I18N }
import org.nlogo.window.{ CodeEditor, EditorColorizer, EditorFactory }
import org.nlogo.awt.Fonts.platformMonospacedFont

class LiteEditorFactory(parser: ParserServices) extends EditorFactory {
  override def newEditor(cols: Int, rows: Int, disableFocusTraversal: Boolean) = {
    val font = new java.awt.Font(
      platformMonospacedFont, java.awt.Font.PLAIN, 12)
    new CodeEditor(
      cols, rows, font, disableFocusTraversal, null,
      new EditorColorizer(parser), I18N.gui.fn)
  }
}
