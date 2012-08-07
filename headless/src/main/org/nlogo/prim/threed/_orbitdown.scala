// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _orbitdown extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType), "O---", true)
  override def perform(context: Context) {
    world.observer.orbitUp(-argEvalDoubleValue(context, 0))
    context.ip = next
  }
}
