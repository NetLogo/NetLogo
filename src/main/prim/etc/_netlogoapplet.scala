// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.core.{ Syntax }

class _netlogoapplet extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(
      ret = Syntax.BooleanType)
  override def report(context: Context): java.lang.Boolean =
    java.lang.Boolean.FALSE
}
