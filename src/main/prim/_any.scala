// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{ Context, Reporter }

class _any extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType),
      ret = Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    Boolean.box(report_1(context, argEvalAgentSet(context, 0)))
  def report_1(context: Context, arg0: AgentSet): Boolean =
    !arg0.isEmpty
}
