// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.{ Let, LogoException, Syntax }
import org.nlogo.nvm.{ Command, Context, CustomAssembled, AssemblerAssistant }

class _carefully extends Command with CustomAssembled {

  // MethodRipper won't let us call a public method from perform_1() - ST 7/20/12
  private[this] val _let = Let()
  def let = _let

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.CommandBlockType,
            Syntax.CommandBlockType))

  override def toString =
    super.toString + ":+" + offset

  override def perform(context: Context) {
    perform_1(context)
  }

  def perform_1(context: Context) {
    val agentset = new org.nlogo.agent.ArrayAgentSet(
      context.agent.kind, 1, false, world)
    agentset.add(context.agent)
    try {
      // start new job that skips over the _goto command
      context.runExclusiveJob(agentset, next + 1)
      // move on to the _goto command, which will skip to the end.
      context.ip = next
    }
    catch { case ex: LogoException =>
      context.let(_let, ex)
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
