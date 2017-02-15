// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.Syntax
import org.nlogo.api, api.{ LogoListBuilder }
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.core.Pure

class _approximatehsb extends Reporter with Pure {

  override def report(context: Context): java.lang.Double =
    report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2));

  def report_1(context: Context, h: Double, s: Double, b: Double): java.lang.Double =
    validDouble(api.Color.getClosestColorNumberByHSB(h.toFloat, s.toFloat, b.toFloat), context)
}

class _approximatehsbold extends Reporter with Pure {

  override def report(context: Context): java.lang.Double =
    report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2));

  def report_1(context: Context, h: Double, s: Double, b: Double): java.lang.Double =
    validDouble(api.Color.getClosestColorNumberByHSB(h.toFloat * (360.0f / 255.0f), s.toFloat * (100.0f / 255.0f), b.toFloat * (100.0f / 255.0f)), context)
}
