// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api, api.{ Syntax, LogoListBuilder, LogoList, Color }
import org.nlogo.nvm.{ Context, Pure, Reporter }

class _extracthsb extends Reporter with Pure {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.NumberType | Syntax.ListType),
                          Syntax.ListType)
  override def report(context: Context): LogoList =
    report_1(context, args(0).report(context))

  def report_1(context: Context, obj: AnyRef): LogoList =
    obj match {
      case rgbList: LogoList =>
        try
          Color.getHSBListByRGBList(360.0f, 100.0f, 100.0f, rgbList)
        catch {
          case e: ClassCastException =>
            throw new org.nlogo.nvm.EngineException(context, this, displayName + " an rgb list must contain only numbers")
        }
      case color: java.lang.Double =>
        Color.getHSBListByColor(360.0f, 100.0f, 100.0f, color)
      case _ => throw new org.nlogo.nvm.ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.NumberType, obj)
    }

  def report_2(context: Context, rgbList: LogoList): LogoList =
    try
      Color.getHSBListByRGBList(360.0f, 100.0f, 100.0f, rgbList)
    catch {
      case e: ClassCastException =>
        throw new org.nlogo.nvm.EngineException(context, this, displayName + " an rgb list must contain only numbers")
    }

  def report_3(context: Context, color: java.lang.Double): LogoList =
    Color.getHSBListByColor(360.0f, 100.0f, 100.0f, color)
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
        try
          Color.getHSBListByRGBList(255.0f, 255.0f, 255.0f, rgbList)
        catch {
          case e: ClassCastException =>
            throw new org.nlogo.nvm.EngineException(context, this, displayName + " an rgb list must contain only numbers")
        }
      case color: java.lang.Double =>
        Color.getHSBListByColor(255.0f, 255.0f, 255.0f, color)
      case _ => throw new org.nlogo.nvm.ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.NumberType, obj)
    }

  def report_2(context: Context, rgbList: LogoList): LogoList =
    try
      Color.getHSBListByRGBList(255.0f, 255.0f, 255.0f, rgbList)
    catch {
      case e: ClassCastException =>
        throw new org.nlogo.nvm.EngineException(context, this, displayName + " an rgb list must contain only numbers")
    }

  def report_3(context: Context, color: java.lang.Double): LogoList =
    Color.getHSBListByColor(255.0f, 100.0f, 255.0f, color)
}
