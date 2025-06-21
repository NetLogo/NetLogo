// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.event.KeyEvent
import javax.swing.Action

import org.nlogo.editor.UndoManager
import org.nlogo.swing.{ UserAction, WrappedAction }

trait UndoRedoActions {
  lazy val undoAction: Action = {
    new WrappedAction(UndoManager.undoAction,
      UserAction.EditCategory,
      UserAction.EditUndoGroup,
      UserAction.KeyBindings.keystroke('Z', withMenu = true), KeyEvent.VK_U)
  }

  lazy val redoAction: Action = {
    new WrappedAction(UndoManager.redoAction,
      UserAction.EditCategory,
      UserAction.EditUndoGroup,
      UserAction.KeyBindings.keystroke('Y', withMenu = true), KeyEvent.VK_R)
  }
}
