// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api, api.{ LogoListBuilder, Color },
  Color.convertGoodHSBListToDumbOldHSBFormat
import org.nlogo.core.{ LogoList, Syntax }
import org.nlogo.nvm.{ Context, Reporter }
import org.nlogo.core.Pure

class _extracthsb extends Reporter with Pure {

  override def report(context: Context): LogoList =
    report_1(context, args(0).report(context))

  def report_1(context: Context, obj: AnyRef): LogoList =
    obj match {
      case rgbList: LogoList =>
        try
          Color.getHSBListByRGBList(rgbList)
        catch {
          case e: ClassCastException =>
            throw new org.nlogo.nvm.RuntimePrimitiveException(context, this, displayName + " an rgb list must contain only numbers")
        }
      case color: java.lang.Double =>
        Color.getHSBListByColor(color)
      case _ => throw new org.nlogo.nvm.ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.NumberType, obj)
    }

  def report_2(context: Context, rgbList: LogoList): LogoList =
    try
      Color.getHSBListByRGBList(rgbList)
    catch {
      case e: ClassCastException =>
        throw new org.nlogo.nvm.RuntimePrimitiveException(context, this, displayName + " an rgb list must contain only numbers")
    }

  def report_3(context: Context, color: java.lang.Double): LogoList =
    Color.getHSBListByColor(color)
}

class _extracthsbold extends Reporter with Pure {

  override def report(context: Context): LogoList =
    report_1(context, args(0).report(context))

  def report_1(context: Context, obj: Object): LogoList =
    obj match {
      case rgbList: LogoList =>
        try
          convertGoodHSBListToDumbOldHSBFormat(Color.getHSBListByRGBList(rgbList))
        catch {
          case e: ClassCastException =>
            throw new org.nlogo.nvm.RuntimePrimitiveException(context, this, displayName + " an rgb list must contain only numbers")
        }
      case color: java.lang.Double =>
        convertGoodHSBListToDumbOldHSBFormat(Color.getHSBListByColor(color))
      case _ => throw new org.nlogo.nvm.ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.NumberType, obj)
    }

  def report_2(context: Context, rgbList: LogoList): LogoList =
    try
      convertGoodHSBListToDumbOldHSBFormat(Color.getHSBListByRGBList(rgbList))
    catch {
      case e: ClassCastException =>
        throw new org.nlogo.nvm.RuntimePrimitiveException(context, this, displayName + " an rgb list must contain only numbers")
    }

  def report_3(context: Context, color: java.lang.Double): LogoList =
    convertGoodHSBListToDumbOldHSBFormat(Color.getHSBListByColor(color))
}
