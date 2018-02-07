// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import java.lang.{ Boolean => JBoolean }
import java.io.File
import org.nlogo.awt.UserCancelException
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }
import org.nlogo.swing.FileDialog
import org.nlogo.window.GUIWorkspace

class _userdirectory extends Reporter {



  override def report(context: Context) = {
    var result: AnyRef = null
    workspace match {
      case gw: GUIWorkspace =>
        gw.updateUI()
        result = gw.waitForResult(() =>
          try {
            gw.view.mouseDown(false)
            FileDialog.setDirectory(workspace.fileManager.prefix)
            FileDialog.showDirectories(gw.getFrame, "Choose Directory") + File.separatorChar
          }
          catch {
            case _: UserCancelException =>
              JBoolean.FALSE
          })
      case _ =>
        throw new RuntimePrimitiveException(
          context, this, "You can't get user input headless.")
    }
    result match {
      case null =>
        throw new org.nlogo.nvm.HaltException(false)
      case b: java.lang.Boolean =>
        b
      case s: String =>
        if(!new java.io.File(s).exists)
          throw new RuntimePrimitiveException(
            context, this, "This directory doesn't exist")
        result
    }
  }

}

