// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.AgentSetBuilder
import org.nlogo.core.AgentKind
import org.nlogo.nvm.{ AssemblerAssistant, Command, Context, CustomAssembled, SelfScoping }

class _createorderedturtles(val breedName: String)
  extends Command
  with CustomAssembled
  with SelfScoping {

  def this() = this("")

  switches = true

  override def toString =
    super.toString + ":" + breedName + ",+" + offset

  override def perform(context: Context): Unit = {
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

  def assemble(a: AssemblerAssistant): Unit = {
    a.add(this)
    a.block()
    a.done()
    a.resume()
  }

}
