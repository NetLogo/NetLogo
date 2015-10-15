// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

class _hideturtle extends Command {
  switches = true
  override def perform(context: Context) {
    context.agent.asInstanceOf[Turtle].hidden(true)
    context.ip = next
  }
}
