// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _clearpatches extends Command {
  override def syntax =
    Syntax.commandSyntax(
      agentClassString = "O---",
      switches = true)
  override def perform(context: Context) {
    world.clearPatches()
    context.ip = next
  }
}
