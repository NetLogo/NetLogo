// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.Color
import org.nlogo.core.{ LogoList, Pure, Syntax }
import org.nlogo.nvm.{ ArgumentTypeException, Context, Reporter }
import org.nlogo.nvm.RuntimePrimitiveException

class _extracthsb extends Reporter with Pure {

  def report(context: Context): AnyRef =
    report_1(context, args(0).report(context))

  def report_1(context: Context, obj: AnyRef): LogoList =
    obj match {
      case rgbList: LogoList =>
        try
          Color.getHSBListByRGBList(rgbList)
        catch {
          case e: ClassCastException =>
            throw new RuntimePrimitiveException(context, this, displayName + " an rgb list must contain only numbers")
        }
      case color: java.lang.Double =>
        Color.getHSBListByColor(color)
      case _ => throw new ArgumentTypeException(context, this, 1, Syntax.ListType | Syntax.NumberType, obj)
    }


  def report_2(context: Context, rgbList: LogoList): LogoList =
    try
      Color.getHSBListByRGBList(rgbList)
    catch {
      case e: ClassCastException =>
        throw new RuntimePrimitiveException(context, this, displayName + " an rgb list must contain only numbers")
    }

  def report_3(context: Context, color: java.lang.Double): LogoList =
    Color.getHSBListByColor(color)
}
