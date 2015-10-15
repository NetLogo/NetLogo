// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.LogoException
import org.nlogo.core.Let
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled }

class _carefully(let: Let) extends Command with CustomAssembled {
  override def toString =
    super.toString + ":+" + offset

  override def perform(context: Context) {
    perform_1(context)
  }

  def perform_1(context: Context) {
    val agentset = AgentSet.fromAgent(context.agent)
    try {
      // start new job that skips over the _goto command
      context.runExclusiveJob(agentset, next + 1)
      // move on to the _goto command, which will skip to the end.
      context.ip = next
    }
    catch { case ex: LogoException =>
      context.let(let, ex)
      context.ip = offset  // jump to error handler
    }
  }

  override def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.goTo()
    a.block(0)
    a.done()
    a.resume()
    a.block(1)
    a.comeFrom()
  }
}
