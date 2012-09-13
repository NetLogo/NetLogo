// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, ArrayAgentSet }
import org.nlogo.api.{ Syntax, AgentKind }
import org.nlogo.nvm.{ Command, Context, CustomAssembled, AssemblerAssistant }

class _hatch(val breedName: String) extends Command with CustomAssembled {

  def this() = this("")

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      "-T--", "-T--", true)

  override def toString =
    super.toString + ":" + breedName + ",+" + offset

  override def perform(context: Context) {
    val count = argEvalIntValue(context, 0)
    if (count > 0) {
      val parent = context.agent.asInstanceOf[Turtle]
      val agentset = new ArrayAgentSet(AgentKind.Turtle, count, false, world)
      val breed =
        if (breedName.isEmpty) parent.getBreed
        else world.getBreed(breedName)
      var i = 0
      while(i < count) {
        val child = parent.hatch(breed)
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
