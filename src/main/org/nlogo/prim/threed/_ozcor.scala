// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _ozcor extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType, "O---")
  override def report(context: Context) =
    Double.box(world.observer.ozcor)
}
