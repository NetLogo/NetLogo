// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Color
import org.nlogo.core.Pure
import org.nlogo.nvm.{ Context, Reporter }

class _wrapcolor extends Reporter with Pure {

  override def report(context: Context): java.lang.Double =
    Double.box(report_1(context, argEvalDoubleValue(context, 0)))

  def report_1(context: Context, colorValue: Double): Double =
    if (colorValue < 0 || colorValue >= 140)
      Color.modulateDouble(colorValue)
    else
      colorValue

}
