package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.api.Syntax

class _netlogoapplet extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.BooleanType)
  override def report(context: Context) =
    if (workspace.getIsApplet)
      java.lang.Boolean.TRUE
    else
      java.lang.Boolean.FALSE
}
