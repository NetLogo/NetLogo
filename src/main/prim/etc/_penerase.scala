// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.agent.Turtle
import org.nlogo.nvm.{ Command, Context }

class _penerase extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "-T--",
      switches = true)
  override def perform(context: Context) {
    context.agent.asInstanceOf[Turtle].penMode(Turtle.PEN_ERASE)
    context.ip = next
  }
}
