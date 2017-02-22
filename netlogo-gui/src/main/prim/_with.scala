// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{Agent, AgentSet, LazyAgentSet}
import org.nlogo.api.Dump
import org.nlogo.core.I18N
import org.nlogo.nvm.{Context, Reporter}
import org.nlogo.nvm.RuntimePrimitiveException

class _with extends Reporter {

  def report(context: Context): AgentSet = {
    val sourceSet = argEvalAgentSet(context, 0)
    val reporterBlock = args(1)
    val freshContext = new Context(context, sourceSet)
    reporterBlock.checkAgentSetClass(sourceSet, context)

    val filter = (agent: Agent) => freshContext.evaluateReporter(agent, reporterBlock) match {
      case b: java.lang.Boolean => b.booleanValue
      case x => throw new RuntimePrimitiveException(context, this, I18N.errors.getN(
        "org.nlogo.prim.$common.expectedBooleanValue",
        displayName, Dump.logoObject(agent), Dump.logoObject(x)))
    }

    if (sourceSet.isInstanceOf[LazyAgentSet]) {
      sourceSet.asInstanceOf[LazyAgentSet].lazyWith(filter)
      sourceSet
    } else {
      new LazyAgentSet(sourceSet.kind, null, sourceSet, withs = List(filter))
    }
  }

}