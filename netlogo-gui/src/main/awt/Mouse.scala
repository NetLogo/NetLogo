// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.awt

import java.awt.event.{ InputEvent, MouseEvent }
import java.awt.{ Component, Point }

object Mouse {
  def translateMouseEvent(e: MouseEvent, target: Component, offsets: Point): MouseEvent =
    new MouseEvent(target, e.getID, e.getWhen, e.getModifiersEx,
        e.getX + offsets.x, e.getY + offsets.y,
        e.getClickCount, e.isPopupTrigger)
  def hasButton1(e: MouseEvent) =
    e.getID match {
      case MouseEvent.MOUSE_RELEASED => (e.getButton & MouseEvent.BUTTON1) != 0
      case _ => (e.getModifiersEx & InputEvent.BUTTON1_DOWN_MASK) != 0
    }
}
