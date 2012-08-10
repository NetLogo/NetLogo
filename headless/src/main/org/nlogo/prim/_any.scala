// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _any extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentsetType),
      Syntax.BooleanType)
  override def report(context: Context) =
    Boolean.box(report_1(context, argEvalAgentSet(context, 0)))
  def report_1(context: Context, arg0: AgentSet) =
    !arg0.isEmpty
}
