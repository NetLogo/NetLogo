// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.agent.{ AgentSet, AgentSetBuilder }
import org.nlogo.nvm.{ Context, Reporter }

object _force {
  case class coreprim() extends org.nlogo.core.Reporter {
    override def syntax =
      Syntax.reporterSyntax(
        right = List(Syntax.AgentsetType), ret = Syntax.AgentsetType)
  }
}

class _force extends Reporter {
  override def report(context: Context): AgentSet =
    report_1(context, argEvalAgentSet(context, 0))

  def report_1(context: Context, sourceSet: AgentSet): AgentSet = {
    sourceSet
  }
}
