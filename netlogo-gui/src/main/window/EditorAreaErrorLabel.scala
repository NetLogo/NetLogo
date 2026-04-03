// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ CompilerException, NetLogoPreferences }
import org.nlogo.editor.AbstractEditorArea

class EditorAreaErrorLabel(val editorArea: AbstractEditorArea) extends ErrorLabel {
  override def setError(compilerError: Option[Exception], offset: Int, respectFocus: Boolean): Unit = {
    super.setError(compilerError, offset, respectFocus)

    compilerError match {
      case Some(ex: CompilerException) if respectFocus && NetLogoPreferences.getBoolean("focusOnError", true) =>
        editorArea.selectError(ex.start - offset, ex.end - offset)
        editorArea.setSelection(false)
        editorArea.requestFocus()
      case _ =>
        editorArea.selectNormal()
        editorArea.setSelection(true)
    }
  }
}
