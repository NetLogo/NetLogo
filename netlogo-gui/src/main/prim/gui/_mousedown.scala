// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.internalapi.GUIWorkspace

class _mousedown extends Reporter {

  override def report(context: Context): java.lang.Boolean =
    workspace match {
      case gw: GUIWorkspace =>
        gw.mouseDown
      case _ =>
        false
    }
}
