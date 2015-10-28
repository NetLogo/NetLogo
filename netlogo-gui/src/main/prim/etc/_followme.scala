// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }
import org.nlogo.api.{ Perspective, Syntax }

class _followme extends Command {
  override def syntax =
    Syntax.commandSyntax("-T--")

  switches = true
  override def perform(context: Context) {
    val turtle = context.agent.asInstanceOf[Turtle]
    // the following code is duplicated in _follow and _followme - ST 6/28/05
    val distance = (turtle.size * 5).toInt
    val followDistance = 1 max distance min 100
    world.observer.setPerspective(Perspective.Follow(turtle, followDistance))
    context.ip = next
  }
}
