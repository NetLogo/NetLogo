// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Pure, Context }

class _plus extends Reporter with Pure {

  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = Array(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 3)

  override def report(context: Context) =
    Double.box(
      report_1(context,
               argEvalDoubleValue(context, 0),
               argEvalDoubleValue(context, 1)))

  def report_1(context: Context, d1: Double, d2: Double) =
    d1 + d2

}
