package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context, Syntax }

class _deletelogfiles extends Command {
  override def syntax =
    Syntax.commandSyntax("O---", true)
  override def perform(context: Context) {
    workspace.deleteLogFiles()
    context.ip = next
  }
}
