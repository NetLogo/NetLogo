// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ Syntax, Color }
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _wrapcolor extends Reporter with Pure {

  override def syntax =
    Syntax.reporterSyntax(
      Array(Syntax.NumberType), Syntax.NumberType)

  override def report(context: Context) =
    Double.box(report_1(context, argEvalDoubleValue(context, 0)))

  def report_1(context: Context, colorValue: Double): Double =
    if (colorValue < 0 || colorValue >= 140)
      Color.modulateDouble(colorValue)
    else
      colorValue

}
