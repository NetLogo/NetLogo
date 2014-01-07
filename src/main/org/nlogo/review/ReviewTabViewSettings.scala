// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.api.Observer
import org.nlogo.api.Perspective
import org.nlogo.api.ViewSettings

object ReviewTabViewSettings {
  /**
   * Constructs new ReviewTabViewSettings from the original view
   */
  def apply(
    originalViewSettings: ViewSettings,
    unzoomedPatchSize: Double,
    observer: Observer) =
    new ReviewTabViewSettings(
      fontSize = originalViewSettings.fontSize,
      patchSize = unzoomedPatchSize,
      viewWidth = originalViewSettings.viewWidth,
      viewHeight = originalViewSettings.viewHeight,
      viewOffsetX = observer.followOffsetX,
      viewOffsetY = observer.followOffsetY,
      drawSpotlight = originalViewSettings.drawSpotlight,
      renderPerspective = originalViewSettings.renderPerspective,
      perspective = observer.perspective
    )
}

case class ReviewTabViewSettings private (
  override val fontSize: Int,
  override val patchSize: Double,
  override val viewWidth: Double,
  override val viewHeight: Double,
  override val viewOffsetX: Double,
  override val viewOffsetY: Double,
  override val drawSpotlight: Boolean,
  override val renderPerspective: Boolean,
  override val perspective: Perspective)
  extends ViewSettings {
  override def isHeadless = false
}
