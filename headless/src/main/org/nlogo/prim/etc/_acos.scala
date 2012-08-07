// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Syntax
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _acos extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType),
      Syntax.NumberType)
  override def report(context: Context) =
    Double.box(report_1(context, argEvalDoubleValue(context, 0)))
  def report_1(context: Context, arg0: Double) =
    validDouble(StrictMath.toDegrees(StrictMath.acos(arg0)))
}
