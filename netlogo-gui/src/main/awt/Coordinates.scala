// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.{ Component, Point, Window }

object Coordinates {

  /**
   * Converts point from a component's coordinate system to screen coordinates.
   * Always returns a freshly constructed Point object.
   */
  def convertPointToScreen(p: Point, c: Component): Point =
    if (c == null)
      new Point(p)
    else if (c.isInstanceOf[Window])
      new Point(p.x + c.getLocationOnScreen.x,
                p.y + c.getLocationOnScreen.y)
    else
      convertPointToScreen(new Point(p.x + c.getLocation.x,
                                     p.y + c.getLocation.y),
                           c.getParent)

  /**
   * Returns the location of a component on the screen.
   */
  def getLocationOnScreen(c: Component): Point =
    convertPointToScreen(new Point(0, 0), c)

}
