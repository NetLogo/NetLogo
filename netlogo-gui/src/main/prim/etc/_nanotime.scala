// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Reporter }

class _nanotime extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Syntax.NumberType)
  override def report(context: Context): java.lang.Double =
    System.nanoTime
}
