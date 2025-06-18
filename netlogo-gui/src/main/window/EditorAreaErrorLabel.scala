// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.core.{ CompilerException, NetLogoPreferences }
import org.nlogo.editor.AbstractEditorArea

class EditorAreaErrorLabel(val editorArea: AbstractEditorArea) extends ErrorLabel {
  override def setError(compilerError: Exception, offset: Int): Unit = {
    super.setError(compilerError, offset)

    compilerError match {
      case ex: CompilerException if NetLogoPreferences.getBoolean("focusOnError", true) =>
        editorArea.selectError(ex.start - offset, ex.end - offset)
        editorArea.setSelection(false)
        editorArea.requestFocus()
      case _ =>
        editorArea.setSelection(true)
    }
  }
}
