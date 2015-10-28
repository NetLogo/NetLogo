// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core.AgentKind
import org.nlogo.api

case class LinkStamp3D(
  shape: String, x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double,
  color: AnyRef, lineThickness: Double, isDirectedLink: Boolean, linkDestinationSize: Double,
  heading: Double, pitch: Double)
extends api.LinkStamp3D {
  def this(l: Link3D) =
    this(l.shape, l.x1, l.y1, l.z1, l.x2, l.y2, l.z2, l.color, l.lineThickness,
         l.isDirectedLink, l.linkDestinationSize, l.heading, l.pitch)
  override val kind = AgentKind.Link
  override def midpointX = (x1 + x2) / 2
  override def midpointY = (y1 + y2) / 2
  override def getBreedIndex = 0
  override def labelColor = 0: java.lang.Double
  override def labelString = ""
  override def hasLabel = false
  override def hidden = false
  override def getBreed = null
  override def end1 = null
  override def end2 = null
  override def size = 0
  override def id = 0
  override def world = null
  override def classDisplayName = ""
  override def alpha = api.Color.getColor(color).getAlpha
  override def getVariable(vn: Int) = unsupported
  override def setVariable(vn: Int, value: AnyRef) = unsupported
  override def variables = unsupported
  private def unsupported = throw new UnsupportedOperationException
}
