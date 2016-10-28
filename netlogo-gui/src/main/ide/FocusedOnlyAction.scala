// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.event.{ FocusEvent, FocusListener }
import org.nlogo.editor.{ AbstractEditorArea, InstallableAction }

trait FocusedOnlyAction extends InstallableAction with FocusListener {

  def install(editorArea: AbstractEditorArea): Unit = {
    editorArea.addFocusListener(this)
  }

  def focusGained(fe: FocusEvent): Unit = {
    setEnabled(true)
  }

  def focusLost(fe: FocusEvent): Unit = {
    if (! fe.isTemporary) {
      setEnabled(false)
    }
  }
}
