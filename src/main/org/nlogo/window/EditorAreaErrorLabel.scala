// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import scala.annotation.strictfp

import org.nlogo.api.CompilerException
import org.nlogo.editor.EditorArea

@strictfp class EditorAreaErrorLabel(private val editorArea: EditorArea[_]) extends ErrorLabel {
  override def setError(compilerError: Exception, offset: Int): Unit = {
    super.setError(compilerError, offset)
    if(compilerError.isInstanceOf[CompilerException]) {
      val compilerEx = compilerError.asInstanceOf[CompilerException]
      editorArea.select(compilerEx.startPos - offset, compilerEx.endPos - offset)
      editorArea.setSelection(false)
      editorArea.requestFocus()
    } else editorArea.setSelection(true)
  }
}
