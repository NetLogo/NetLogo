// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Command, Context }

class _zoom extends Command {
  switches = true

  override def syntax =
    Syntax.commandSyntax(Array(Syntax.NumberType), "O---")
  override def perform(context: Context) {
    val observer = world.observer
    // don't zoom past the point you are looking at.  maybe this should be an error?
    val orientation = observer.orientation.get
    val delta = argEvalDoubleValue(context, 0) min orientation.dist
    observer.oxyandzcor(
      observer.oxcor + delta * orientation.dx,
      observer.oycor + delta * orientation.dy,
      observer.ozcor - delta * orientation.dz)
    context.ip = next
  }
}
