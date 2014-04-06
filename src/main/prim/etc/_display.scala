// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Command, Context }

class _display extends Command {
  override def syntax =
    Syntax.commandSyntax(
      switches = true)
  override def perform(context: Context) {
    world.displayOn(true)
    workspace.requestDisplayUpdate(context, true)
    context.ip = next
  }
}
