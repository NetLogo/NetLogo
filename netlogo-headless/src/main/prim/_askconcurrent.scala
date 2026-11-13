// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Observer
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, SelfScoping }
import org.nlogo.nvm.RuntimePrimitiveException

class _askconcurrent extends Command with CustomAssembled with SelfScoping {

  switches = true

  override def toString =
    super.toString + ":+" + offset

  override def perform(context: Context): Unit = {
    val agentset = argEvalAgentSet(context, 0)
    if (!context.agent.isInstanceOf[Observer]) {
      if (agentset eq world.turtles)
        throw new RuntimePrimitiveException(context, this, loc"Only the observer can ASK the set of all turtles.")
      if (agentset eq world.patches)
        throw new RuntimePrimitiveException(context, this, loc"Only the observer can ASK the set of all patches.")
    }
    if (context.makeChildrenExclusive)
      context.runExclusiveJob(agentset, next)
    else {
      context.waiting = true
      workspace.addJobFromJobThread(context.makeConcurrentJob(agentset))
    }
    context.ip = offset
  }

  override def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
