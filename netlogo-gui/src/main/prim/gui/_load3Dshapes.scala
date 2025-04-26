// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }
import org.nlogo.window.GUIWorkspace

class _load3Dshapes extends Command {

  switches = true
  override def perform(context: Context): Unit = {
    val filename = argEvalString(context, 0)
    workspace match {
      case gw: GUIWorkspace =>
        try gw.addCustomShapes(filename)
        catch {
          case e: java.io.IOException =>
            throw new RuntimePrimitiveException(context, this, e.getMessage)
        }
      case _ =>
        // ok to just ignore, I guess - ST 5/17/11
    }
    context.ip = next
  }
}
