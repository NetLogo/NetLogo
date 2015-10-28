// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.AgentKind
import org.nlogo.api.Color

@annotation.strictfp
case class TurtleStamp3D(shape: String, xcor: Double, ycor: Double, zcor: Double, size: Double,
                         heading: Double, pitch: Double, roll: Double, color: AnyRef, lineThickness: Double)
extends org.nlogo.api.TurtleStamp3D {
  def this(t: Turtle3D) =
    this(t.shape, t.xcor, t.ycor, t.zcor, t.size, t.heading, t.pitch, t.roll, t.color, t.lineThickness)

  override val kind = AgentKind.Turtle
  // stuff we're ignoring for now
  override def hasLabel = false
  override def labelString = ""
  override def labelColor = null
  override def hidden = false
  override def id = 0L
  override def getBreed = unsupported
  override def getBreedIndex = 0
  override def world = null
  override def getPatchHere = null
  override def jump(d: Double) {}
  override def heading(d: Double) {}
  override def classDisplayName = ""
  override def dx = 0
  override def dy = 0
  override def dz = 0
  override def alpha = Color.getColor(color).getAlpha
  override def getVariable(vn: Int) = unsupported
  override def setVariable(vn: Int, value: AnyRef) = unsupported
  override def variables = unsupported

  private def unsupported = throw new UnsupportedOperationException
}
