// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, Patch, AgentSet, ArrayAgentSet }
import org.nlogo.api.{ Syntax, AgentKind }
import org.nlogo.nvm.{ AssemblerAssistant, Command, CustomAssembled, Context }

class _sprout(val breedName: String) extends Command with CustomAssembled {

  def this() = this("")

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      "--P-", "-T--", true)

  override def toString =
    super.toString + ":" + breedName + ",+" + offset

  override def perform(context: Context) {
    val parent = context.agent.asInstanceOf[Patch]
    val count = argEvalIntValue(context, 0)
    val random = context.job.random
    if (count > 0) {
      val agentset = new ArrayAgentSet(AgentKind.Turtle, count, false, world)
      val breed =
        if (breedName.isEmpty) world.turtles
        else world.getBreed(breedName)
      var i = 0
      while (i < count) {
        val child = parent.sprout(random.nextInt(14), random.nextInt(360), breed)
        agentset.add(child)
        workspace.joinForeverButtons(child)
        i += 1
      }
      context.runExclusiveJob(agentset, next)
    }
    context.ip = offset
  }

  def assemble(a: AssemblerAssistant) {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
