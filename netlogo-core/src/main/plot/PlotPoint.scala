// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.plot

import org.nlogo.api.PlotPointInterface

@SerialVersionUID(0)
case class PlotPoint(x: Double, y: Double, isDown: Boolean, color: Int) extends PlotPointInterface // color is ARGB
