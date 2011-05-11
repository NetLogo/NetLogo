package org.nlogo.prim.gui

import org.nlogo.nvm.{ Command, Context, Syntax }

class _beep extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    java.awt.Toolkit.getDefaultToolkit().beep()
    context.ip = next
  }
}
