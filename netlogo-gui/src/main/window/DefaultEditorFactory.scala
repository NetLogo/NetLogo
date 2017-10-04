// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.ScrollPaneConstants

import org.nlogo.api.ExtensionManager
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.editor.{ AbstractEditorArea, EditorArea, EditorConfiguration, EditorScrollPane, LineNumberScrollPane }

class DefaultEditorFactory(compiler: PresentationCompilerInterface, extensionManager: ExtensionManager) extends EditorFactory {
  val colorizer = new EditorColorizer(compiler, extensionManager)

  override def newEditor(configuration: EditorConfiguration): AbstractEditorArea =
    new EditorArea(configuration)

  def scrollPane(editor: AbstractEditorArea): EditorScrollPane = {
    val sp = new LineNumberScrollPane(
      editor,
      ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
      ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
    sp.setLineNumbersEnabled(editor.configuration.showLineNumbers)
    sp
  }
}
