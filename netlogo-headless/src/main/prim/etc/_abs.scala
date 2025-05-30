// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _abs extends Reporter with Pure {

  override def report(context: Context): java.lang.Double =
    report_1(context, argEvalDoubleValue(context, 0))

  def report_1(context: Context, d: java.lang.Double): java.lang.Double = {
    val unwrapped = d.doubleValue
    if (unwrapped < 0)
      Double.box(-unwrapped)
    else
      d
  }

  def report_2(context: Context, d: Double): Double =
    if (d < 0) -d else d

}
