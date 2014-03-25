// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.{ AgentSet, Link }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _bothends extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.AgentsetType, "---L")
  override def report(context: Context): AgentSet =
    report_1(context)
  def report_1(context: Context): AgentSet =
    context.agent.asInstanceOf[Link].bothEnds
}
