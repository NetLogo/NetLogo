// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.core.{ Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _noturtles extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.TurtlesetType)
  override def report (context: Context): AgentSet =
    world.noTurtles
}
