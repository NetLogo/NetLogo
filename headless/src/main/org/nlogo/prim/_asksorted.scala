// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context, CustomAssembled, AssemblerAssistant }
import org.nlogo.agent.{ Agent, ArrayAgentSet }

class _asksorted extends Command with CustomAssembled {

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.AgentsetType, Syntax.CommandBlockType),
      "OTPL", "?", true)

  override def toString =
    super.toString + ":+" + offset

  def perform(context: Context) {
    val agents = argEvalAgentSet(context, 0)
    val sortedAgents =
      agents
        .toLogoList
        .scalaIterator
        .collect{case agent: Agent => agent}
    for(agent <- sortedAgents) {
      val set = new ArrayAgentSet(agents.kind, 1, false, world)
      set.add(agent)
      context.runExclusiveJob(set, next)
    }
    context.ip = offset
  }

  override def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
