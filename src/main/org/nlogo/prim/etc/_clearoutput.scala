package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context, Syntax }

class _clearoutput extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    workspace.clearOutput()
    context.ip = next
  }
}
