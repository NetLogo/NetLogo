// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context }
import org.nlogo.agent.AgentSet

class _anyother extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentsetType),
      Syntax.BooleanType, "-TPL")

  override def report(context: Context): java.lang.Boolean =
    Boolean.box(report_1(context, argEvalAgentSet(context, 0)))

  def report_1(context: Context, sourceSet: AgentSet): Boolean = {
    val it = sourceSet.iterator
    while(it.hasNext) {
      if (it.next() ne context.agent)
        return true
    }
    false
  }

}
