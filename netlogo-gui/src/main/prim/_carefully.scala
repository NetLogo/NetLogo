// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.prim

import org.nlogo.agent.AgentSet
import org.nlogo.api.LogoException
import org.nlogo.core.Let
import org.nlogo.nvm.{AssemblerAssistant, Command, Context, CustomAssembled, SelfScoping}

final class _carefully(private val let: Let)
  extends Command
  with CustomAssembled
  with SelfScoping {

  def this() = this(null)

  override def toString: String = super.toString + ":+" + offset

  override def perform(context: Context): Unit = perform_1(context)

  def perform_1(context: Context): Unit = {
    val agentset = AgentSet.fromAgent(context.agent)
    val oldBinding = context.activation.binding
    try { // start new job that skips over the _goto command
      context.activation.binding = context.activation.binding.enterScope
      context.runExclusiveJob(agentset, next + 1)
      context.activation.binding = context.activation.binding.exitScope
      // move on to the _goto command, which will skip to the end.
      context.ip = next
    } catch {
      case ex: LogoException =>
        context.activation.binding = oldBinding
        context.activation.binding.let(let, ex)
        context.ip = offset // jump to error handler
    }
  }

  override def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.goTo()
    a.block(0)
    a.done()
    a.resume()
    a.block(1)
    a.comeFrom()
  }
}
