package org.nlogo.prim.etc

import org.nlogo.nvm.{Command, Context, Syntax}

// true here because tick calls other code
class _tick extends Command(true) {
  override def syntax = Syntax.commandSyntax("O---", true)
  override def perform(context: Context) {
    workspace.tick(context, this)
    context.ip = next
  }
}
