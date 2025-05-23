// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

// replaces _hatch when initialization block is empty

class _hatchfast(breedName: String) extends Command {

  switches = true

  override def toString =
    super.toString + ":" + breedName

  override def perform(context: Context): Unit = {
    val count = argEvalIntValue(context, 0)
    if (count > 0) {
      val parent = context.agent.asInstanceOf[Turtle]
      val breed =
        if (breedName.isEmpty) parent.getBreed
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
