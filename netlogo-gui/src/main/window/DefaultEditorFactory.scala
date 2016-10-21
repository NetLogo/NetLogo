// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.{ JScrollPane, ScrollPaneConstants }

import org.nlogo.editor.{ AbstractEditorArea, EditorArea, EditorConfiguration, EditorScrollPane, LineNumberScrollPane }
import org.nlogo.api.CompilerServices

class DefaultEditorFactory(compiler: CompilerServices) extends EditorFactory {
  val colorizer = new EditorColorizer(compiler)

  override def newEditor(configuration: EditorConfiguration): AbstractEditorArea =
    new EditorArea(configuration)

  def scrollPane(editor: AbstractEditorArea): EditorScrollPane =
    new LineNumberScrollPane(
      editor,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
}
