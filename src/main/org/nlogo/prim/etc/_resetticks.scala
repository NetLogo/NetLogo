package org.nlogo.prim.etc

import org.nlogo.nvm.{Command, Context, Syntax}

// true here because reset-ticks calls other code
class _resetticks extends Command(true) {
  override def syntax =
    Syntax.commandSyntax("O---", true)
  override def perform(context: Context) {
    workspace.resetTicks(context)
    context.ip = next
  }
}
