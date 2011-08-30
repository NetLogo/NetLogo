package org.nlogo.prim.threed

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _oxcor extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType, "O---")
  override def report(context: Context) =
    java.lang.Double.valueOf(world.observer.oxcor)
}
