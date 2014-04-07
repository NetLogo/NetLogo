// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.core.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _unaryminus extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      right = List(Syntax.NumberType),
      ret = Syntax.NumberType)
  override def report(context: Context): java.lang.Double =
    Double.box(
      report_1(
        context, argEvalDoubleValue(context, 0)))
  def report_1(context: Context, d0: Double): Double =
    -d0
}
