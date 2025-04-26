// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }
import org.nlogo.window.GUIWorkspace

class _exportinterface extends Command {
  override def perform(context: Context): Unit = {
    workspace match {
      case gw: GUIWorkspace =>
        gw.updateUI()
        val filePath = argEvalString(context, 0)
        try workspace.exportInterface(workspace.fileManager.attachPrefix(filePath))
        catch {
          case e: java.io.IOException =>
            throw new RuntimePrimitiveException(
              context, _exportinterface.this, token.text + ": " + e.getMessage)
        }
      case _ =>
        throw new RuntimePrimitiveException(
          context, this, token.text + " can only be used in the GUI")
    }
    context.ip = next
  }
}
