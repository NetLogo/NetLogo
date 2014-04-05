// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Command, Context }

class _stamperase extends Command {
  override def syntax =
    SyntaxJ.commandSyntax("-T-L", true)
  override def perform(context: Context) {
    world.stamp(context.agent, true)
    context.ip = next
  }
}
