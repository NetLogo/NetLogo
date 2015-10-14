// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.{ Component, Window }
import java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment

object Positioning {

  /** for centering frames and dialogs */
  def center(window: Window, parent: Window) {
    val (x, y) =
      if (parent == null) {
        val center = getLocalGraphicsEnvironment.getCenterPoint
        (center.x - window.getWidth / 2,
         center.y - window.getHeight / 2)
      }
      else
        (parent.getLocation.x + parent.getWidth / 2 - window.getPreferredSize.width / 2,
         parent.getLocation.y + parent.getHeight / 2 - window.getPreferredSize.height / 2)
    val availBounds =
      if (parent == null)
        getLocalGraphicsEnvironment.getMaximumWindowBounds
      else
        parent.getGraphicsConfiguration.getBounds
    val xMax = availBounds.x + availBounds.width - window.getWidth
    val yMax = availBounds.y + availBounds.height - window.getHeight
    window.setLocation(0 max x min xMax,
                       0 max y min yMax)
  }

  /** Moves c1 next to c2. Usually on the right, but if there isn't enough room, left or below. */
  def moveNextTo(c1: Component, c2: Component) {
    val Space = 4
    val right = c2.getBounds.x + c2.getBounds.width  + Space
    val below = c2.getBounds.y + c2.getBounds.height + Space
    val left =  c2.getBounds.x - c1.getBounds.width  - Space
    val screenBounds = c2.getGraphicsConfiguration.getBounds
    if (screenBounds.width - right - c1.getBounds.width > 0)
      c1.setLocation(right, c2.getLocation.y)
    else if (left > screenBounds.x)
      c1.setLocation(left, c2.getLocation.y)
    else if (screenBounds.height - below - c1.getBounds.height > 0)
      c1.setLocation(c2.getLocation.x, below)
    else
      c1.setLocation((screenBounds.x + screenBounds.width) - c1.getBounds.width,
                     c2.getLocation.y)
  }

}
