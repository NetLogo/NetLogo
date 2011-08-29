package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _nolinks extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_LINKSET)
  override def report(context: Context) =
    world.noLinks
}
