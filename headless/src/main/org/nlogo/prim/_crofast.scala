// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Turtle
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _crofast(breedName: String) extends Command {

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType),
      "O---", true)

  override def toString =
    super.toString + ":" + breedName

  override def perform(context: Context) {
    val count = argEvalIntValue(context, 0)
    if (count > 0) {
      val breed =
        if (breedName.isEmpty) world.turtles
        else world.getBreed(breedName)
      var i = 0
      while (i < count) {
        val turtle = world.createTurtle(breed)
        turtle.colorDouble(Double.box(10.0 * i + 5.0))
        turtle.heading((360.0 * i) / count)
        workspace.joinForeverButtons(turtle)
        i += 1
      }
    }
    context.ip = next
  }

}
