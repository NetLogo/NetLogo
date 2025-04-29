// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, Patch, Turtle }
import org.nlogo.nvm.{ Context, Reporter }

class _neighbors4 extends Reporter {
  override def report(context: Context): AgentSet =
    report_1(context)
  def report_1(context: Context): AgentSet =
    (context.agent match {
      case t: Turtle => t.getPatchHere
      case p: Patch => p
      case a => throw new Exception(s"Unexpected agent: $a")
    }).getNeighbors4
}
