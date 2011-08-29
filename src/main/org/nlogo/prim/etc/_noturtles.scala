package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _noturtles extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TYPE_TURTLESET)
  override def report (context: Context) =
    world.noTurtles
}
