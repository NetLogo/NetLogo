// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.internalapi.GUIWorkspace

class _mouseycor extends Reporter {

  override def report(context: Context): java.lang.Double =
    workspace match {
      case gw: GUIWorkspace =>
        gw.mouseYCor
      case _ =>
        0
    }
}
