// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent


@annotation.strictfp
case class DrawingLine3D(x0: Double, y0: Double, z0: Double,
                         x1: Double, y1: Double, z1: Double,
                         heading: Double, pitch: Double,
                         width: Double, color: AnyRef)
extends org.nlogo.api.DrawingLine3D {
  def length = {
    val xdiff = x1 - x0
    val ydiff = y1 - y0
    val zdiff = z1 - z0
    StrictMath.sqrt((xdiff * xdiff) + (ydiff * ydiff) + (zdiff * zdiff))
  }
}
