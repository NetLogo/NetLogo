// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import javax.swing.Action

trait InstallableAction extends Action {
  def install(editorArea: AbstractEditorArea): Unit
}
