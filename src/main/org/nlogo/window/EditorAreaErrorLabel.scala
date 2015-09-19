// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.CompilerException
import org.nlogo.editor.EditorArea

class EditorAreaErrorLabel(editorArea: EditorArea[_]) extends ErrorLabel {
  override def setError(compilerError: Exception, offset: Int): Unit = {
    super.setError(compilerError, offset)
    compilerError match {
      case compilerEx: CompilerException =>
        editorArea.select(compilerEx.startPos - offset, compilerEx.endPos - offset)
        editorArea.setSelection(false)
        editorArea.requestFocus()
      case _ => editorArea.setSelection(true)
    }
  }
}
