// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ LogoListBuilder }
import org.nlogo.core.Syntax
import org.nlogo.core.LogoList
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.core.Pure

object hsb {
   def toList(hMax: Float, sMax: Float, bMax: Float,
              h: Float, s: Float, b: Float) = {
     val argb = java.awt.Color.HSBtoRGB(
           (StrictMath.max
             (0, StrictMath.min(hMax, h)) / hMax),
           (StrictMath.max
             (0, StrictMath.min(sMax, s)) / sMax),
           (StrictMath.max
             (0, StrictMath.min(bMax, b)) / bMax))

     val rgbList = new LogoListBuilder
     rgbList.add(Double.box((argb >> 16) & 0xff))
     rgbList.add(Double.box((argb >> 8) & 0xff))
     rgbList.add(Double.box(argb & 0xff))
     rgbList.toLogoList
   }
}

class _hsb extends Reporter with Pure {

  override def report(context: Context) =
    report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2));

  def report_1(context: Context, h: Double, s: Double, b: Double) =
    hsb.toList(360.0f, 100.0f, 100.0f, h.toFloat, s.toFloat, b.toFloat)
}

class _hsbold extends Reporter with Pure {

  override def report(context: Context) =
    report_1(context,
        argEvalDoubleValue(context, 0),
        argEvalDoubleValue(context, 1),
        argEvalDoubleValue(context, 2));

  def report_1(context: Context, h: Double, s: Double, b: Double) =
    hsb.toList(255.0f, 255.0f, 255.0f, h.toFloat, s.toFloat, b.toFloat)
}
