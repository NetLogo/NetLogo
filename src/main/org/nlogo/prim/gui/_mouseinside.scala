package org.nlogo.prim.gui

import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Context, Reporter, Syntax }
import org.nlogo.window.GUIWorkspace

class _mouseinside extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_BOOLEAN)
  override def report(context: Context): java.lang.Boolean =
    workspace match {
      case gw: GUIWorkspace =>
        gw.mouseInside()
      case _ =>
        false
    }
}
