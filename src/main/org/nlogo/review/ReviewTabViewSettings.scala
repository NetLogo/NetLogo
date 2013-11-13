// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.api.Perspective
import org.nlogo.api.ViewSettings
import org.nlogo.window.View

object ReviewTabViewSettings {
  /**
   * Constructs new ReviewTabViewSettings from the original view 
   */
  def apply(originalView: View) =
    new ReviewTabViewSettings(
      fontSize = originalView.fontSize,
      patchSize = originalView.unzoomedPatchSize,
      viewWidth = originalView.viewWidth,
      viewHeight = originalView.viewHeight,
      viewOffsetX = originalView.viewOffsetX,
      viewOffsetY = originalView.viewOffsetY)
}

case class ReviewTabViewSettings private (
  override val fontSize: Int,
  override val patchSize: Double,
  override val viewWidth: Double,
  override val viewHeight: Double,
  override val viewOffsetX: Double,
  override val viewOffsetY: Double)
  extends ViewSettings {
  // the next few method disregard original view settings
  override def drawSpotlight = false
  override def renderPerspective = false
  override def isHeadless = false
  override def perspective = Perspective.Observe
}
