// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Point, Toolkit, Window }
import javax.swing.SwingUtilities

object Positioning {
  def center(window: Window, target: Component) {
    if (target == null) {
      val screenSize = Toolkit.getDefaultToolkit.getScreenSize

      window.setLocation(screenSize.width / 2 - window.getWidth / 2, screenSize.height / 2 - window.getHeight / 2)
    }

    else {
      val targetCorner = new Point

      SwingUtilities.convertPointToScreen(targetCorner, target)

      window.setLocation(targetCorner.x + target.getWidth / 2 - window.getWidth / 2,
                         targetCorner.y + target.getHeight / 2 - window.getHeight / 2)
    }
  }
}
