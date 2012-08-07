// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _zoom extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType),
                         "O---", true)
  override def perform(context: Context) {
    val observer = world.observer
    // don't zoom past the point you are looking at.  maybe this should be an error?
    val delta = argEvalDoubleValue(context, 0) min observer.dist
    observer.oxyandzcor(
      observer.oxcor + delta * observer.dx,
      observer.oycor + delta * observer.dy,
      observer.ozcor - delta * observer.dz)
    context.ip = next
  }
}
