package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

// true here because reset-ticks calls other code
class _resetticks extends Command(true) {
  override def syntax =
    Syntax.commandSyntax("O---", true)
  override def perform(context: Context) {
    workspace.resetTicks(context)
    context.ip = next
  }
}
