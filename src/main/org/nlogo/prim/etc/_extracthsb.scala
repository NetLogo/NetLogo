// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api, api.{ Syntax, LogoListBuilder, LogoList }
import org.nlogo.nvm.{ Context, Pure, Reporter }

object extracthsb {
  def fromRGB(hMax: Float, sMax: Float, bMax: Float, rgbList: LogoList): LogoList = {
    val r = rgbList.get(0).asInstanceOf[Double].intValue
    val g = rgbList.get(1).asInstanceOf[Double].intValue
    val b = rgbList.get(2).asInstanceOf[Double].intValue
    val hsbvals = java.awt.Color.RGBtoHSB(
          (StrictMath.max
            (0, StrictMath.min(255, r)) / 255),
          (StrictMath.max
            (0, StrictMath.min(255, g)) / 255),
          (StrictMath.max
            (0, StrictMath.min(255, b)) / 255),
          null)

    val hsbList = new LogoListBuilder
    hsbList.add(Double.box(hMax * hsbvals(0)))
    hsbList.add(Double.box(sMax * hsbvals(1)))
    hsbList.add(Double.box(bMax * hsbvals(2)))
    hsbList.toLogoList
  }

  def fromColor(hMax: Float, sMax: Float, bMax: Float, color: Double): LogoList = {
    val ccolor = if (color < 0 || color >= 140) api.Color.modulateDouble(color) else color
    api.Color.getHSBListByARGB(api.Color.getARGBbyPremodulatedColorNumber(ccolor), hMax, sMax, bMax)
  }
}

class _extracthsb extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType | Syntax.ListType),
                          Syntax.ListType)
  override def report(context: Context): LogoList =
    report_1(context, args(0).report(context))

  def report_1(context: Context, obj: Object): LogoList =
    obj match {
      case rgbList: LogoList =>
        try {
          extracthsb.fromRGB(360.0f, 100.0f, 100.0f, rgbList)
        } catch {
          case e: ClassCastException =>
            throw new org.nlogo.nvm.EngineException(context, this, displayName + " an rgb list must contain only numbers")
        }
      case color: java.lang.Double =>
        extracthsb.fromColor(360.0f, 100.0f, 100.0f, color)
      case _ => throw new org.nlogo.nvm.ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.NumberType, obj)
    }

  def report_2(context: Context, rgbList: LogoList): LogoList =
    try {
      extracthsb.fromRGB(360.0f, 100.0f, 100.0f, rgbList)
    } catch {
      case e: ClassCastException =>
        throw new org.nlogo.nvm.EngineException(context, this, displayName + " an rgb list must contain only numbers")
    }

  def report_3(context: Context, color: java.lang.Double): LogoList =
    extracthsb.fromColor(360.0f, 100.0f, 100.0f, color)
}

class _extracthsbold extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType | Syntax.ListType),
                          Syntax.ListType)
  override def report(context: Context): LogoList =
    report_1(context, args(0).report(context))

  def report_1(context: Context, obj: Object): LogoList =
    obj match {
      case rgbList: LogoList =>
        try {
          extracthsb.fromRGB(255.0f, 255.0f, 255.0f, rgbList)
        } catch {
          case e: ClassCastException =>
            throw new org.nlogo.nvm.EngineException(context, this, displayName + " an rgb list must contain only numbers")
        }
      case color: java.lang.Double =>
        extracthsb.fromColor(255.0f, 255.0f, 255.0f, color)
      case _ => throw new org.nlogo.nvm.ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.NumberType, obj)
    }

  def report_2(context: Context, rgbList: LogoList): LogoList =
    try {
      extracthsb.fromRGB(255.0f, 255.0f, 255.0f, rgbList)
    } catch {
      case e: ClassCastException =>
        throw new org.nlogo.nvm.EngineException(context, this, displayName + " an rgb list must contain only numbers")
    }

  def report_3(context: Context, color: java.lang.Double): LogoList =
    extracthsb.fromColor(255.0f, 100.0f, 255.0f, color)
}
