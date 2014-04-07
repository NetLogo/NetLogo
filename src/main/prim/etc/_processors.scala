// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax }
import org.nlogo.nvm.{ Context, Reporter }

class _processors extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.NumberType)
  override def report(context: Context): java.lang.Double =
    Runtime.getRuntime.availableProcessors
}
