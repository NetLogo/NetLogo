// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.agent.Patch
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

// replaces _sprout when initialization block is empty

class _sproutfast(breedName: String) extends Command {

  override def syntax =
    Syntax.commandSyntax(
      Array(Syntax.NumberType),
      "--P-", true)

  override def toString =
    super.toString + ":" + breedName

  override def perform(context: Context) {
    val parent = context.agent.asInstanceOf[Patch]
    val count = argEvalIntValue(context, 0)
    if (count > 0) {
      val random = context.job.random
      val breed =
        if (breedName.isEmpty) world.turtles
        else world.getBreed(breedName)
      var i = 0
      while (i < count) {
        workspace.joinForeverButtons(
          parent.sprout(random.nextInt(14), random.nextInt(360), breed))
        i += 1
      }
    }
    context.ip = next
  }

}
