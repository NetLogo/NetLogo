// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Reporter, Pure, Context }

@annotation.strictfp
class _pow extends Reporter with Pure {

  override def syntax =
    Syntax.reporterSyntax(
      left = Syntax.NumberType,
      right = Array(Syntax.NumberType),
      ret = Syntax.NumberType,
      precedence = Syntax.NormalPrecedence - 1)

  override def report(context: Context) =
    Double.box(
      report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1)))

  def report_1(context: Context, d0: Double, d1: Double): Double =
    validDouble(StrictMath.pow(d0, d1))

}
