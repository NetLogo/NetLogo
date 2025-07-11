// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import org.nlogo.editor.UndoManager
import org.nlogo.swing.UserAction.MenuAction

trait UndoRedoActions {
  lazy val undoAction: MenuAction = UndoManager.undoAction
  lazy val redoAction: MenuAction = UndoManager.redoAction
}
