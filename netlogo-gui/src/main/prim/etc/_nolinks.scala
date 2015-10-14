// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _nolinks extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.LinksetType)
  override def report(context: Context) =
    world.noLinks
}
