// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax }
import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{ Context, Reporter }

class _turtles extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType)
  override def report(context: Context): AnyRef =
    report_1(context)
  def report_1(context: Context): AgentSet =
    world.turtles()
}
