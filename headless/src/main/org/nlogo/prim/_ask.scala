// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.{ Syntax, I18N }
import org.nlogo.nvm.{ Command, Context, CustomAssembled, AssemblerAssistant,
                       EngineException, ArgumentTypeException }
import org.nlogo.agent.{ Agent, AgentSet, Observer }

class _ask extends Command with CustomAssembled {

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.AgentsetType | Syntax.AgentType,
            Syntax.CommandBlockType),
      "OTPL", "?", true)

  override def toString =
    super.toString + ":+" + offset

  override def perform(context: Context) {
    perform_1(context, args(0).report(context))
  }

  def perform_1(context: Context, target: AnyRef) {
    val agents = target match {
      case agents: AgentSet =>
        if (!context.agent.isInstanceOf[Observer]) {
          if (agents eq world.turtles)
            throw new EngineException(
              context, this, I18N.errors.get(
                "org.nlogo.prim.$common.onlyObserverCanAskAllTurtles"))
          if (agents eq world.patches)
            throw new EngineException(
              context, this, I18N.errors.get(
                "org.nlogo.prim.$common.onlyObserverCanAskAllPatches"))
        }
        agents
      case agent: Agent =>
        if (agent.id == -1)
          throw new EngineException(
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

  def perform_2(context: Context, agents: AgentSet) {
    if (!context.agent.isInstanceOf[Observer]) {
      if (agents eq world.turtles)
        throw new EngineException(
          context, this, I18N.errors.get(
            "org.nlogo.prim.$common.onlyObserverCanAskAllTurtles"))
      if (agents eq world.patches)
        throw new EngineException(
          context, this, I18N.errors.get(
            "org.nlogo.prim.$common.onlyObserverCanAskAllPatches"))
    }
    context.runExclusiveJob(agents, next)
    context.ip = offset
  }

  def perform_3(context: Context, agent: Agent) {
    if (agent.id == -1)
      throw new EngineException(
        context, this, I18N.errors.getN(
          "org.nlogo.$common.thatAgentIsDead", agent.classDisplayName))
    context.runExclusiveJob(AgentSet.fromAgent(agent), next)
    context.ip = offset
  }

  override def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
