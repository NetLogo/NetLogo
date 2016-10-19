// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import javax.swing.{ JScrollPane, ScrollPaneConstants }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.{ AbstractEditorArea, EditorConfiguration }
import org.nlogo.window.{ CodeEditor, EditorColorizer, EditorFactory }

class LiteEditorFactory(compiler: CompilerServices) extends EditorFactory {
  override def newEditor(cols: Int, rows: Int, enableFocusTraversal: Boolean, enableHighlightCurrentLine: Boolean) = {
    val colorizer = new EditorColorizer(compiler)
    val configuration =
      EditorConfiguration.default(rows, cols, colorizer)
        .withFocusTraversalEnabled(enableFocusTraversal)
        .withCurrentLineHighlighted(enableHighlightCurrentLine)
    new CodeEditor(configuration)
  }

  def scrollPane(editor: AbstractEditorArea): JScrollPane =
    new JScrollPane(
      editor,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
}

