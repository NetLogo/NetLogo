package org.nlogo.prim.gui

import org.nlogo.api.LogoException
import org.nlogo.nvm.{ Command, Context, Syntax }

class _ziplogfiles extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.TYPE_STRING), "O---", true)
  override def perform(context: Context) {
    workspace.zipLogFiles(argEvalString(context, 0))
    context.ip = next
  }
}
