package org.nlogo.prim.threed

import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Context, Syntax }

class _orbitup extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.TYPE_NUMBER), "O---", true)
  override def perform(context: Context) {
    world.observer.orbitUp(argEvalDoubleValue(context, 0))
    context.ip = next
  }
}
