// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.api.Perspective
import org.nlogo.nvm.{ Command, Context }

class _followme extends Command {
  switches = true
  override def perform(context: Context): Unit = {
    val turtle = context.agent.asInstanceOf[Turtle]
    world.observer.setPerspective(Perspective.Follow(turtle, 5))
    context.ip = next
  }
}
