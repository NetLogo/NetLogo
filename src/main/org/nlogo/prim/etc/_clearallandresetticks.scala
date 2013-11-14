// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

// true here because resetTicks calls other code
class _clearallandresetticks extends Command {
  override def syntax =
    Syntax.commandSyntax("O---", true)
  override def callsOtherCode = true
  override def perform(context: Context) {
    workspace.clearAll()
    workspace.resetTicks(context)
    context.ip = next
  }
}
