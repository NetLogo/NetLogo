// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.window.GUIWorkspace

class _mouseinside extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    workspace match {
      case gw: GUIWorkspace =>
        gw.mouseInside()
      case _ =>
        false
    }
}
