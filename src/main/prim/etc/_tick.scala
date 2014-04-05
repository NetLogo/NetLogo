// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _tick extends Command {
  override def syntax =
    SyntaxJ.commandSyntax("O---", true)
  override def callsOtherCode = true
  override def perform(context: Context) {
    workspace.tick(context, this)
    context.ip = next
  }
}
