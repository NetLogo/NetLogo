// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Rectangle

import javax.swing.JPanel

object ViewBoundsCalculator {
  def calculateViewBounds(
    viewWidgetPanel: JPanel,
    insideBorderHeight: Int,
    patchSize: Double,
    worldHeight: Int): Rectangle = {
    val insets = viewWidgetPanel.getInsets
    val availableWidth: Int =
      viewWidgetPanel.getWidth - insets.left - insets.right
    val graphicsHeight: Int =
      StrictMath.round(patchSize * worldHeight).toInt
    val stripHeight: Int =
      viewWidgetPanel.getHeight - graphicsHeight - insets.top - insets.bottom
    new Rectangle(
      insets.left,
      insets.top + insideBorderHeight + stripHeight,
      availableWidth,
      graphicsHeight)
  }
}
