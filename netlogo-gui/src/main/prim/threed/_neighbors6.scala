// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.agent.{ Patch3D, Turtle }
import org.nlogo.nvm.{ Context, Reporter }

class _neighbors6 extends Reporter {

  override def report(context: Context) =
    (context.agent match {
      case t: Turtle =>
        t.getPatchHere.asInstanceOf[Patch3D]
      case p: Patch3D =>
        p
      case a => throw new Exception(s"Unexpected agent: $a")
    }).getNeighbors6
}
