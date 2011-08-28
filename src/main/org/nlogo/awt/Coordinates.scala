package org.nlogo.awt

import java.awt.{ Component, Point, Window }
import java.applet.Applet

object Coordinates {

  /**
   * Converts point from a component's coordinate system to screen coordinates.
   */
  def convertPointToScreen(p: Point, c: Component) {
    if(c != null) {
      val done = c.isInstanceOf[Applet] || c.isInstanceOf[Window]
      val (x, y) =
        if (done) {
          val pp = c.getLocationOnScreen
          (pp.x, pp.y)
        }
        else
          (c.getLocation.x, c.getLocation.y)
      p.x += x
      p.y += y
      if (!done)
        convertPointToScreen(p, c.getParent)
    }
  }

  /**
   * Converts point to a component's coordinate system from screen coordinates.
   */
  def convertPointFromScreen(p: Point, c: Component) {
    if(c != null) {
      val done = c.isInstanceOf[Applet] || c.isInstanceOf[Window]
      val (x, y) =
        if (done) {
          val pp = c.getLocationOnScreen
          (pp.x, pp.y)
        }
        else
          (c.getLocation.x, c.getLocation.y)
      p.x -= x
      p.y -= y
      if (!done)
        convertPointToScreen(p, c.getParent)
    }
  }

  /**
   * Returns the location of a component on the screen.
   */
  def getLocationOnScreen(c: Component): Point = {
    val result = new Point(0, 0)
    convertPointToScreen(result, c)
    result
  }

  /**
   * Returns the difference between two points.
   */
  def subtractPoints(p1: Point, p2: Point): Point =
    new Point(p1.x - p2.x, p1.y - p2.y)

}
