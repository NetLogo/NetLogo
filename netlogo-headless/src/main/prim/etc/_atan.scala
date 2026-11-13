// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ LocaleContext, Pure }
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _atan extends Reporter with Pure {
  override def report(context: Context): java.lang.Double =
    Double.box(
      report_1(context,
               argEvalDoubleValue(context, 0),
               argEvalDoubleValue(context, 1)))
  def report_1(context: Context, d1: Double, d2: Double): Double =
    if (d1 == 0 && d2 == 0)
      throw new RuntimePrimitiveException(context, this, loc"atan is undefined when both inputs are zero.")
    else if (d1 == 0)
      if (d2 > 0) 0 else 180
    else if (d2 == 0)
      if (d1 > 0) 90 else 270
    else validDouble(StrictMath.toDegrees(StrictMath.atan2(d1, d2)) + 360, context) % 360
}
