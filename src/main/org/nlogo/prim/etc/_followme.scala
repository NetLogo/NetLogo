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
    context.ip = next
  }
}
