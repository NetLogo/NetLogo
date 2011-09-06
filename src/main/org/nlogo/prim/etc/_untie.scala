package org.nlogo.prim.etc

import org.nlogo.agent.Link
import org.nlogo.nvm.{ Command, Context, Syntax }

class _untie extends Command {
  override def syntax =
    Syntax.commandSyntax("---L", true)
  override def perform(context: Context) {
    context.agent.asInstanceOf[Link].mode(Link.MODE_NONE)
    context.ip = next
  }
}
