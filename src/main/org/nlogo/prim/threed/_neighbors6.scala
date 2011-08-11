package org.nlogo.prim.threed

import org.nlogo.agent.{ Turtle, Patch3D }
import org.nlogo.nvm.{ Context, Reporter, Syntax }

class _neighbors6 extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Syntax.TYPE_PATCHSET, "-TP-")
  override def report(context: Context) =
    (context.agent match {
      case t: Turtle =>
        t.getPatchHere.asInstanceOf[Patch3D]
      case p: Patch3D =>
        p
    }).getNeighbors6
}
