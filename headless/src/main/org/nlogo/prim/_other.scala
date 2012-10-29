// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Agent, AgentSet, ArrayAgentSet }
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Context }

class _other extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.AgentsetType),
      Syntax.AgentsetType)

  override def report(context: Context) =
    report_1(context, argEvalAgentSet(context, 0))

  def report_1(context: Context, sourceSet: AgentSet): AgentSet = {
    val result = new ArrayAgentSet(
      sourceSet.kind, sourceSet.count, false, world)
    val it = sourceSet.iterator
    while(it.hasNext) {
      val otherAgent = it.next()
      if (context.agent ne otherAgent)
        result.add(otherAgent)
    }
    result
  }

}
