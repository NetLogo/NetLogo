// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.event.{ InputEvent, MouseEvent }
import java.awt.{ Component, Point }

object Mouse {
  def translateMouseEvent(e: MouseEvent, target: Component, offsets: Point): MouseEvent =
    new MouseEvent(target, e.getID, e.getWhen, e.getModifiers,
        e.getX + offsets.x, e.getY + offsets.y,
        e.getClickCount, e.isPopupTrigger)
  def hasButton1(e: MouseEvent) =
    (e.getModifiers & InputEvent.BUTTON1_MASK) != 0
}
