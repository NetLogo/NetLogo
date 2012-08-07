// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.{ Turtle, Patch3D }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _neighbors6 extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Syntax.PatchsetType, "-TP-")
  override def report(context: Context) =
    (context.agent match {
      case t: Turtle =>
        t.getPatchHere.asInstanceOf[Patch3D]
      case p: Patch3D =>
        p
    }).getNeighbors6
}
