// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, AgentSet, ArrayAgentSet }
import org.nlogo.api.{ Syntax, AgentKind }
import org.nlogo.nvm.{ Command, Context, CustomAssembled, AssemblerAssistant }

class _createturtles(val breedName: String) extends Command with CustomAssembled {

  def this() = this("")

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType, Syntax.CommandBlockType | Syntax.OptionalType),
      "O---", "-T--", true)

  override def toString =
    super.toString + ":" + breedName + ",+" + offset

  override def perform(context: Context) {
    val count = argEvalIntValue(context, 0)
    if (count > 0) {
      val agentset = new ArrayAgentSet(AgentKind.Turtle, count, false, world)
      val breed =
        if(breedName.isEmpty) world.turtles
        else world.getBreed(breedName)
      val random = context.job.random
      var i = 0
      while (i < count) {
        val turtle =world.createTurtle(breed, random.nextInt(14),
                                       random.nextInt(360))
        agentset.add(turtle)
        workspace.joinForeverButtons(turtle)
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
