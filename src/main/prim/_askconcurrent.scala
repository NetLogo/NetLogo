// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.api.I18N
import org.nlogo.nvm.{ Command, CustomAssembled, Context,
                       EngineException, AssemblerAssistant }
import org.nlogo.agent.Observer

class _askconcurrent extends Command with CustomAssembled {

  override def syntax =
    Syntax.commandSyntax(
      right = List(Syntax.AgentsetType, Syntax.CommandBlockType),
      agentClassString = "OTPL",
      blockAgentClassString = "?",
      switches = true)

  override def toString =
    super.toString + ":+" + offset

  override def perform(context: Context) {
    val agentset = argEvalAgentSet(context, 0)
    if (!context.agent.isInstanceOf[Observer]) {
      if (agentset eq world.turtles)
        throw new EngineException(
          context, this, I18N.errors.get(
            "org.nlogo.prim.$common.onlyObserverCanAskAllTurtles"))
      if (agentset eq world.patches)
        throw new EngineException(
          context, this, I18N.errors.get(
            "org.nlogo.prim.$common.onlyObserverCanAskAllPatches"))
    }
    if (context.makeChildrenExclusive)
      context.runExclusiveJob(agentset, next)
    else {
      context.waiting = true
      workspace.addJobFromJobThread(context.makeConcurrentJob(agentset))
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
