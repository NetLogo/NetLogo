// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Font
import java.awt.event.TextListener
import org.nlogo.api.TokenType
import org.nlogo.editor.{ Colorizer, EditorArea }

class CodeEditor(rows: Int, columns: Int, font: Font, disableFocusTraversalKeys: Boolean,
  listener: TextListener, colorizer: Colorizer[TokenType], i18n: String => String)
extends EditorArea[TokenType](rows, columns, font, disableFocusTraversalKeys,
  listener, colorizer, i18n)
