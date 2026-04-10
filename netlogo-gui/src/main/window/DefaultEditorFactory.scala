// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.editor.{ EditorArea, EditorConfiguration }
import org.nlogo.api.CompilerServices

class DefaultEditorFactory(val compiler: CompilerServices) extends EditorFactory {
  val colorizer = new EditorColorizer(compiler)

  override def newEditor(configuration: EditorConfiguration): EditorArea =
    new EditorArea(configuration) with AutoIndentHandler
}
