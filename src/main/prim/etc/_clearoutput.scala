// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _clearoutput extends Command {
  override def syntax =
    Syntax.commandSyntax
  override def perform(context: Context) {
    workspace.clearOutput()
    context.ip = next
  }
}
