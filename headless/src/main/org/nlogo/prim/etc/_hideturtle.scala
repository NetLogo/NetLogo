// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.Turtle;
import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _hideturtle extends Command {
  override def syntax =
    Syntax.commandSyntax("-T--", true)
  override def perform(context: Context) {
    context.agent.asInstanceOf[Turtle].hidden(true)
    context.ip = next
  }
}
