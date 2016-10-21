// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing.JScrollPane
import org.nlogo.editor.{ AbstractEditorArea, Colorizer, EditorConfiguration, EditorScrollPane }

trait EditorFactory {
  def colorizer: Colorizer

  def defaultConfiguration(cols: Int, rows: Int): EditorConfiguration =
    EditorConfiguration.default(cols, rows, colorizer)

  def newEditor(configuration: EditorConfiguration): AbstractEditorArea

  def scrollPane(editor: AbstractEditorArea): EditorScrollPane
}
