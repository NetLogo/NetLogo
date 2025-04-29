// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.nvm.{ Command, Context }

class _crtfast(breedName: String) extends Command {

  switches = true

  override def toString =
    super.toString + ":" + breedName

  override def perform(context: Context): Unit = {
    val count = argEvalIntValue(context, 0)
    if (count > 0) {
      val breed =
        if (breedName.isEmpty) world.turtles
        else world.getBreed(breedName)
      val random = context.job.random
      var i = 0
      while(i < count) {
        workspace.joinForeverButtons(
          world.createTurtle(breed, random.nextInt(14),
                             random.nextInt(360)))
        i += 1
      }
    }
    context.ip = next
  }

}
