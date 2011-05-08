package org.nlogo.prim.etc

import org.nlogo.nvm.{Context, Syntax}

class _clearticks extends org.nlogo.nvm.Command {
  override def syntax =
    Syntax.commandSyntax("O---", true)
  override def perform(context: Context) {
    workspace.clearTicks()
    context.ip = next
  }
}
