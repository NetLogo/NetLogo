// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.agent.Link
import org.nlogo.nvm.{ Command, Context }

class _hidelink extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "---L",
      switches = true)
  override def perform(context: Context) {
    context.agent.asInstanceOf[Link].hidden(true)
    context.ip = next
  }
}
