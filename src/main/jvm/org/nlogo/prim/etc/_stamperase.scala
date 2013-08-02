// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _stamperase extends Command {
  override def syntax =
    Syntax.commandSyntax("-T-L", true)
  override def perform(context: Context) {
    world.stamp(context.agent, true)
    context.ip = next
  }
}
