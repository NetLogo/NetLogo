// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

class _penup extends Command {
  switches = true
  override def perform(context: Context) {
    context.agent.asInstanceOf[Turtle].penMode(Turtle.PEN_UP)
    context.ip = next
  }
}
