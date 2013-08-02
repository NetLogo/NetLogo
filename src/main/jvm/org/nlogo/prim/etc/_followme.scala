// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }
import org.nlogo.api.{ Perspective, Syntax }

class _followme extends Command {
  override def syntax =
    Syntax.commandSyntax("-T--", true)
  override def perform(context: Context) {
    val turtle = context.agent.asInstanceOf[Turtle]
    world.observer.setPerspective(Perspective.Follow, turtle)
    // the following code is duplicated in _follow and _followme - ST 6/28/05
    val distance = (turtle.size * 5).toInt
    context.ip = next
  }
}
