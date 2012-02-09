// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import java.io.DataOutputStream
import org.nlogo.api.Color.{ getRGBAListByARGB, getARGBbyPremodulatedColorNumber }
import org.nlogo.api.LogoList

object AgentData {
  def toLogoList(color: AnyRef): LogoList =
    color match {
      case d: java.lang.Double =>
        getRGBAListByARGB(
          getARGBbyPremodulatedColorNumber(
            d.doubleValue))
      case l: LogoList =>
        l
      case null =>
        null
    }
}

abstract class AgentData extends Overridable {
  def xcor: Double
  def ycor: Double
  def spotlightSize: Double
  def wrapSpotlight: Boolean
  @throws(classOf[java.io.IOException])
  def serialize(out: DataOutputStream)
}
