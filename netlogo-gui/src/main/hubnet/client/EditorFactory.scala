// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import javax.swing.{ JScrollPane, ScrollPaneConstants }

import org.nlogo.editor.{ AbstractEditorArea, EditorConfiguration }
import org.nlogo.api.CompilerServices

class EditorFactory(compiler: CompilerServices) extends org.nlogo.window.EditorFactory {
  override def newEditor(cols: Int, rows: Int, enableFocusTraversal: Boolean, enableHighlightCurrentLine: Boolean): org.nlogo.editor.AbstractEditorArea =
    newEditor(cols, rows, enableFocusTraversal, null, false, enableHighlightCurrentLine)

  def newEditor(cols: Int, rows: Int , enableFocusTraversal: Boolean, listener: java.awt.event.TextListener, isApp: Boolean,
                enableHighlightCurrentLine: Boolean = false) = {
    val editorConfiguration =
      EditorConfiguration.default(rows, cols, new org.nlogo.window.EditorColorizer(compiler))
        .withFocusTraversalEnabled(enableFocusTraversal)
        .withCurrentLineHighlighted(enableHighlightCurrentLine)
        .withListener(listener)

    new org.nlogo.window.CodeEditor(editorConfiguration)
  }

  def scrollPane(editor: AbstractEditorArea): JScrollPane =
    new JScrollPane(
      editor,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
}
