// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

/** This is used to find places for agent monitors */

import java.awt.{ Frame, GraphicsEnvironment, Point, Rectangle, Window }
import java.util.{ List => JList }
import collection.JavaConverters._

object Tiler {

  def findEmptyLocation(otherWindows: JList[Window], window: Window): Point = {
    val parentFrame = window.getParent.asInstanceOf[Frame]
    val availBounds = GraphicsEnvironment.getLocalGraphicsEnvironment.getMaximumWindowBounds
    // first see if there's room to the right of the parent frame
    if (parentFrame.getLocation.x + parentFrame.getWidth + window.getWidth <= availBounds.x + availBounds.width) {
      val loc = slideDown(
        otherWindows, window, availBounds,
        parentFrame.getLocation.x + parentFrame.getWidth,
        parentFrame.getLocation.y + parentFrame.getInsets.top)
      if (loc != null)
        return loc
    }
    // next see if there's room below
    if (parentFrame.getLocation.y + parentFrame.getHeight + window.getHeight <= availBounds.y + availBounds.height) {
      val loc = slideRight(
        otherWindows, window, availBounds, parentFrame.getLocation.x,
        parentFrame.getLocation.y + parentFrame.getHeight)
      if (loc != null)
        return loc
    }
    // next try the left side
    if (parentFrame.getLocation.x - window.getWidth >= 0) {
      val loc = slideDown(
        otherWindows, window, availBounds,
        parentFrame.getLocation.x - window.getWidth,
        parentFrame.getLocation.y + parentFrame.getInsets.top)
      if (loc != null)
        return loc
    }
    // try against the right edge of the screen
    locally {
      val loc = slideDown(
        otherWindows, window, availBounds,
        availBounds.x + availBounds.width - window.getWidth,
        parentFrame.getLocation.y + parentFrame.getInsets.top)
      if (loc != null)
        return loc
    }
    // try against the bottom edge of the screen
    locally {
      val loc = slideRight(
        otherWindows, window, availBounds, 0,
        availBounds.y + availBounds.height - window.getHeight)
      if (loc != null)
        return loc
    }
    // try against the left edge of the screen
    locally {
      val loc = slideDown(
        otherWindows, window, availBounds, 0,
        parentFrame.getLocation.y + parentFrame.getInsets.top)
      if (loc != null)
        return loc
    }
    // put it in the lower right corner of the screen
    new Point(
      availBounds.x + availBounds.width - window.getWidth,
      availBounds.y + availBounds.height - window.getHeight)
  }

  private def slideDown(otherWindows: JList[Window], window: Window, availBounds: Rectangle, x: Int, _y: Int): Point = {
    var y = _y
    while(y + window.getHeight <= availBounds.y + availBounds.height) {
      if (emptyLocation(otherWindows, window, x, y))
        return new Point(x, y)
      y += 1
    }
    null
  }

  private def slideRight(otherWindows: JList[Window], window: Window, availBounds: Rectangle, _x: Int, y: Int) : Point ={
    var x = _x
    while(x + window.getWidth <= availBounds.x + availBounds.width) {
      if (emptyLocation(otherWindows, window, x, y))
        return new java.awt.Point(x, y)
      x += 1
    }
    null
  }

  private def emptyLocation(otherWindows: JList[Window], window: Window, x: Int, y: Int): Boolean = {
    for(otherWindow <- otherWindows.asScala)
      if (window != otherWindow && overlap(otherWindow, window, x, y))
        return false
    true
  }

  private def overlap(otherWindow: Window, window: Window, x: Int, y: Int): Boolean =
    otherWindow.getBounds.intersects(
      new Rectangle(new Point(x, y), window.getSize))

}
