// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Agent, AgentSet, AgentSetBuilder }
import org.nlogo.core.Syntax
import org.nlogo.nvm.{ ArgumentTypeException, Context, Reporter }

class _whoarenot extends Reporter {
  override def report(context: Context): AgentSet = {
    val source = argEvalAgentSet(context, 0)

    args(1).report(context) match {
      case agent: Agent =>
        removeAgent(source, agent)

      case set: AgentSet =>
        removeAgentSet(source, set)

      case x =>
        throw new ArgumentTypeException(context, this, 1, Syntax.AgentsetType | Syntax.AgentType, x)

    }
  }

  def removeAgent(source: AgentSet, agent: Agent): AgentSet = {
    val result = new AgentSetBuilder(source.kind, source.count)
    val iterator = source.iterator
    while (iterator.hasNext) {
      val a = iterator.next()
      if (agent != a) {
        result.add(a)
      }
    }
    result.build()
  }

  def removeAgentSet(source: AgentSet, set: AgentSet): AgentSet = {
    val result = new AgentSetBuilder(source.kind, source.count)
    val iterator = source.iterator
    while (iterator.hasNext) {
      val a = iterator.next()
      if (!set.contains(a)) {
        result.add(a)
      }
    }
    result.build()
  }

}
