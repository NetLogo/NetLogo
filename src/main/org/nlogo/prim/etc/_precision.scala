// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _precision extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      Syntax.NumberType)
  override def report(context: Context) =
    newValidDouble(
      org.nlogo.api.Approximate.approximate(
        argEvalDoubleValue(context, 0),
        argEvalIntValue(context, 1)))
}
