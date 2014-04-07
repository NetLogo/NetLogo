// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.agent.{ AgentSet, Link }
import org.nlogo.nvm.{ Context, Reporter }

class _bothends extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.AgentsetType,
      agentClassString = "---L")
  override def report(context: Context): AgentSet =
    report_1(context)
  def report_1(context: Context): AgentSet =
    context.agent.asInstanceOf[Link].bothEnds
}
