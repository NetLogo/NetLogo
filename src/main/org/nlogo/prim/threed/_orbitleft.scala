// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _orbitleft extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType), "O---", true)
  override def perform(context: Context) {
    world.observer.orbitRight(-argEvalDoubleValue(context, 0))
    context.ip = next
  }
}
