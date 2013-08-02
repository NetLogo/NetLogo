// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ I18N, Syntax }
import org.nlogo.nvm.{ Context, Reporter, Pure, EngineException }

class _atan extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType, Syntax.NumberType),
      Syntax.NumberType)
  override def report(context: Context) =
    Double.box(
      report_1(context,
               argEvalDoubleValue(context, 0),
               argEvalDoubleValue(context, 1)))
  def report_1(context: Context, d1: Double, d2: Double) =
    if (d1 == 0 && d2 == 0)
      throw new EngineException(context, this,
        I18N.errors.get("org.nlogo.prim.etc.atan.bothInputsCannotBeZero"))
    else if (d1 == 0)
      if (d2 > 0) 0 else 180
    else if (d2 == 0)
      if (d1 > 0) 90 else 270
    else (StrictMath.toDegrees(StrictMath.atan2(d1, d2)) + 360) % 360
}
