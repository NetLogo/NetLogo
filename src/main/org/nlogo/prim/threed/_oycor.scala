package org.nlogo.prim.threed

import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _oycor extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_NUMBER, "O---")
  override def report(context: Context) =
    java.lang.Double.valueOf(world.observer.oycor)
}
