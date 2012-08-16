// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

// replaces _hatch when initialization block is empty

class _fasthatch(breedName: String) extends Command {

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType), "-T--", true)

  override def toString =
    super.toString + ":" + breedName

  override def perform(context: Context) = {
    val count = argEvalIntValue(context, 0)
    if (count > 0) {
      val parent = context.agent.asInstanceOf[Turtle]
      val breed =
        if (breedName.isEmpty) world.turtles
        else world.getBreed(breedName)
      var i = 0
      while(i < count) {
        workspace.joinForeverButtons(
          parent.hatch(breed))
        i += 1
      }
    }
    context.ip = next
  }

}
