// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ AgentSet, AgentSetBuilder }
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Reporter, Context }

class _other extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.AgentsetType),
      ret = Syntax.AgentsetType)

  override def report(context: Context): AgentSet =
    report_1(context, argEvalAgentSet(context, 0))

  def report_1(context: Context, sourceSet: AgentSet): AgentSet = {
    val builder = new AgentSetBuilder(sourceSet.kind, sourceSet.count)
    val it = sourceSet.iterator
    while(it.hasNext) {
      val otherAgent = it.next()
      if (context.agent ne otherAgent)
        builder.add(otherAgent)
    }
    builder.build()
  }

}
