// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _clearticks extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---",
      switches = true)
  override def perform(context: Context) {
    workspace.clearTicks()
    context.ip = next
  }
}
