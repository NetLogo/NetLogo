// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _tan extends Reporter with Pure {
  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context, argEvalDoubleValue(context, 0)))
  def report_1(context: Context, angle: Double): Double = {
    val mod = Math.abs(angle % 180)
    if (mod == 90) {
      validDouble(java.lang.Double.POSITIVE_INFINITY, context)
    }
    if (mod == 0) {
      0
    } else {
      StrictMath.tan(StrictMath.toRadians(angle))
    }
  }
}
