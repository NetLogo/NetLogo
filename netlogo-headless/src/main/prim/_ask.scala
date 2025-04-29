// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Agent, AgentSet, Observer }
import org.nlogo.core.{ I18N, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, AssemblerAssistant, Command, Context,
                       CustomAssembled, RuntimePrimitiveException, SelfScoping }

class _ask
  extends Command
  with CustomAssembled
  with SelfScoping {

  switches = true

  override def toString =
    super.toString + ":+" + offset

  override def perform(context: Context): Unit = {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, target: AnyRef): Unit = {
    val agents = target match {
      case agents: AgentSet =>
        if (!context.agent.isInstanceOf[Observer]) {
          if (agents eq world.turtles)
            throw new RuntimePrimitiveException(
              context, this, I18N.errors.get(
                "org.nlogo.prim.$common.onlyObserverCanAskAllTurtles"))
          if (agents eq world.patches)
            throw new RuntimePrimitiveException(
              context, this, I18N.errors.get(
                "org.nlogo.prim.$common.onlyObserverCanAskAllPatches"))
        }
        agents
      case agent: Agent =>
        if (agent.id == -1)
          throw new RuntimePrimitiveException(
            context, this, I18N.errors.getN(
              "org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
        AgentSet.fromAgent(agent)
      case _ =>
        throw new ArgumentTypeException(
          context, this, 0, Syntax.AgentsetType | Syntax.AgentType, target)
    }
    context.runExclusiveJob(agents, next)
    context.ip = offset
  }

  def perform_2(context: Context, agents: AgentSet): Unit = {
    if (!context.agent.isInstanceOf[Observer]) {
      if (agents eq world.turtles)
        throw new RuntimePrimitiveException(
          context, this, I18N.errors.get(
            "org.nlogo.prim.$common.onlyObserverCanAskAllTurtles"))
      if (agents eq world.patches)
        throw new RuntimePrimitiveException(
          context, this, I18N.errors.get(
            "org.nlogo.prim.$common.onlyObserverCanAskAllPatches"))
    }
    context.runExclusiveJob(agents, next)
    context.ip = offset
  }

  def perform_3(context: Context, agent: Agent): Unit = {
    if (agent.id == -1)
      throw new RuntimePrimitiveException(
        context, this, I18N.errors.getN(
          "org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
    context.runExclusiveJob(AgentSet.fromAgent(agent), next)
    context.ip = offset
  }

  override def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
