// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.{ Turtle, AgentSet, AgentSetBuilder }
import org.nlogo.api.{ Syntax, AgentKind }
import org.nlogo.nvm.{ Command, Context, CustomAssembled, AssemblerAssistant }

class _createorderedturtles(val breedName: String) extends Command with CustomAssembled {

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
      val builder = new AgentSetBuilder(AgentKind.Turtle, count)
      val breed =
        if(breedName.isEmpty) world.turtles
        else world.getBreed(breedName)
      var i = 0
      while (i < count) {
        val turtle = world.createTurtle(breed)
        turtle.colorDouble(Double.box(10.0 * i + 5.0))
        turtle.heading((360.0 * i) / count)
        builder.add(turtle)
        workspace.joinForeverButtons(turtle)
        i += 1
      }
      context.runExclusiveJob(builder.build(), next)
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
