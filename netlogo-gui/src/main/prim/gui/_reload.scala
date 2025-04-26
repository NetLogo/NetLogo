// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }
import org.nlogo.window.GUIWorkspace

class _reload extends Command {


  switches = true
  override def perform(context: Context): Unit = {
    workspace match {
      case gw: GUIWorkspace =>
        gw.reload()
        context.ip = next
      case _ =>
        throw new RuntimePrimitiveException(
          context, this, token.text + " can only be used in the GUI")
    }
  }
}
