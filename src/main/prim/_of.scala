// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.api.{ I18N, LogoListBuilder }
import org.nlogo.agent.{ Agent, AgentSet }
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _of extends Reporter {

  override def report(context: Context): AnyRef =
    args(1).report(context) match {
      case agent: Agent =>
        if (agent.id == -1)
          throw new EngineException(
            context, this,
            I18N.errors.getN("org.nlogo.$common.thatAgentIsDead",
                             agent.classDisplayName))
        args(0).checkAgentClass(agent, context)
        new Context(context, agent)
          .evaluateReporter(agent, args(0))
      case sourceSet: AgentSet =>
        val builder = new LogoListBuilder
        val freshContext = new Context(context, sourceSet)
        args(0).checkAgentSetClass(sourceSet, context)
        val iter = sourceSet.shufflerator(context.job.random)
        while(iter.hasNext)
          builder.add(freshContext.evaluateReporter(iter.next(), args(0)))
        builder.toLogoList
      case x =>
        throw new org.nlogo.nvm.ArgumentTypeException(
          context, this, 1, Syntax.AgentsetType | Syntax.AgentType, x)
    }

}
