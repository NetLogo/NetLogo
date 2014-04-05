// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _asin extends Reporter with Pure {
  override def syntax =
    SyntaxJ.reporterSyntax(
      Array(Syntax.NumberType),
      Syntax.NumberType)
  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context, argEvalDoubleValue(context, 0)))
  def report_1(context: Context, arg0: Double): Double =
    validDouble(StrictMath.toDegrees(StrictMath.asin(arg0)))
}
