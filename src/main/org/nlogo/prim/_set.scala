package org.nlogo.prim

import org.nlogo.nvm.{Command, Context, Syntax}

class _set extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.TYPE_WILDCARD,
                               Syntax.TYPE_WILDCARD))
  override def perform(context: Context) {
    // we get compiled out of existence
    throw new UnsupportedOperationException
  }
}
