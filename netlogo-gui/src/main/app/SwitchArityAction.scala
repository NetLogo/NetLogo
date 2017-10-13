// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app


import javax.swing.AbstractAction
import java.awt.event.ActionEvent

import org.nlogo.api.Exceptions
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.UserAction, UserAction.MenuAction

object SwitchArityAction {
  def switchName(to3D: Boolean) =
    if (to3D) "Switch to 3D"
    else "Switch to 2D"
}

class SwitchArityAction(val to3D: Boolean, fileManager: FileManager, app: App)
  extends AbstractAction(SwitchArityAction.switchName(to3D))
  with MenuAction {

  category = UserAction.FileCategory
  group = UserAction.FileSwitchArityGroup

  override def actionPerformed(e: ActionEvent): Unit = {
    try {
      fileManager.aboutToCloseFiles()
      app.switchArity(to3D)
    } catch {
      case ex: UserCancelException => Exceptions.ignore(ex)
    }
  }
}
