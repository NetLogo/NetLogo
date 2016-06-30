// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import javax.swing._
import javax.swing.text.TextAction

import org.nlogo.editor.EditorArea

class CodeEditor(rows: Int, columns: Int,
                 font: java.awt.Font,
                 disableFocusTraversalKeys: Boolean,
                 listener: java.awt.event.TextListener,
                 colorizer: org.nlogo.editor.Colorizer,
                 i18n: String => String,
                 actionMap: Map[KeyStroke, TextAction] = EditorArea.emptyMap) extends
  EditorArea(rows, columns,
    font, disableFocusTraversalKeys, listener, colorizer, i18n, actionMap)
