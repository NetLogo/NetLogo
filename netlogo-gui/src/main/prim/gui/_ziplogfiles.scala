// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _ziplogfiles extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType), "O---")

  switches = true
  override def perform(context: Context) {
    workspace.zipLogFiles(argEvalString(context, 0))
    context.ip = next
  }
}
