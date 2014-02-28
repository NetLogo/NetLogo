// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import org.nlogo.api

object FixedViewSettings {
  /**
   * Constructs a new FixedViewSettings from some original view settings.
   */
  def apply(originalView: api.ViewSettings) = new FixedViewSettings(
    fontSize = originalView.fontSize,
    patchSize = originalView.patchSize,
    viewWidth = originalView.viewWidth,
    viewHeight = originalView.viewHeight,
    viewOffsetX = originalView.viewOffsetX,
    viewOffsetY = originalView.viewOffsetY)
}

case class FixedViewSettings private (
  override val fontSize: Int,
  override val patchSize: Double,
  override val viewWidth: Double,
  override val viewHeight: Double,
  override val viewOffsetX: Double,
  override val viewOffsetY: Double)
  extends api.ViewSettings
  with Serializable {
  // the next few method disregard original view settings
  // and these values are not saved when the run is serialized
  override def drawSpotlight = false
  override def renderPerspective = false
  override def perspective = api.Perspective.Observe
}
