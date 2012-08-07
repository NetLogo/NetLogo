// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _noturtles extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.TurtlesetType)
  override def report (context: Context) =
    world.noTurtles
}
