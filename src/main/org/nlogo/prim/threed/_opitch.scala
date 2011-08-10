package org.nlogo.prim.threed

import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _opitch extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_NUMBER)
  override def report(context: Context) =
    java.lang.Double.valueOf(world.observer.pitch)
}
