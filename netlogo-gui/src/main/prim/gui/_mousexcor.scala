// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.window.GUIWorkspace

class _mousexcor extends Reporter {

  override def report(context: Context): java.lang.Double =
    workspace match {
      case gw: GUIWorkspace =>
        gw.mouseXCor
      case _ =>
        0
    }
}
