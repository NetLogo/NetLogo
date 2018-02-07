// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.ReporterRunnable
import org.nlogo.awt.UserCancelException
import org.nlogo.nvm.{ Context, Reporter, RuntimePrimitiveException }
import org.nlogo.swing.FileDialog
import org.nlogo.window.GUIWorkspace

class _usernewfile extends Reporter {

  override def report(context: Context) = {
    var result: AnyRef = null
    workspace match {
      case gw: GUIWorkspace =>
        gw.updateUI()
        result = gw.waitForResult(
          new ReporterRunnable[AnyRef] {
            override def run() =
              try {
                gw.view.mouseDown(false)
                FileDialog.setDirectory(workspace.fileManager.prefix)
                FileDialog.showFiles(gw.getFrame, "Choose File", java.awt.FileDialog.SAVE)
              }
              catch {
                case _: UserCancelException =>
                  java.lang.Boolean.FALSE
              }})
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
        s
    }
  }

}
