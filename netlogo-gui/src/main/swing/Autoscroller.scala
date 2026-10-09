// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Cursor, MouseInfo, Point }
import java.awt.event.{ FocusAdapter, FocusEvent, HierarchyEvent, KeyAdapter, KeyEvent, MouseAdapter, MouseEvent }
import javax.swing.{ JComponent, SwingUtilities, Timer }

object Autoscroller {
  val DeadZone = 10 // distance (px) from the anchor within which no scrolling occurs
  val DragThreshold = 5 // distance (px) past which releasing the middle button ends autoscrolling
  val Speed = 12.0 // scroll speed (px/s) for each px of distance from the anchor beyond the dead zone
  val Interval = 16 // time (ms) between scroll updates
}

// Middle-click autoscrolling for a component. Pressing the middle mouse button (the scroll wheel) anchors a point,
// and moving the mouse away from the anchor scrolls at a speed that increases with the distance from it. Releasing
// the button after dragging away from the anchor, pressing any mouse button, pressing escape, or losing focus ends
// the scrolling. The component is not scrolled directly, since it may not be scrollable in the Swing sense (for
// example, a JFXPanel), so scrollBy is called with the number of pixels to scroll horizontally and vertically.
class Autoscroller(component: JComponent, scrollBy: (Int, Int) => Unit) {
  import Autoscroller._

  // positions are in screen coordinates, since the position of the component relative to the mouse changes as it
  // scrolls. the mouse is polled on each update instead of tracked with events, because the component stops receiving
  // mouse events if the mouse leaves it, and scrolling should continue (at the speed for the new position) regardless
  private var anchor: Option[Point] = None
  private var lastUpdate = 0L
  private var remainderX = 0.0 // scrolling is applied in whole pixels, so fractional amounts carry over
  private var remainderY = 0.0
  private var previousCursor: Cursor = null

  private val timer = new Timer(Interval, _ => update())

  component.addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      if (anchor.isDefined) {
        stop()
      } else if (SwingUtilities.isMiddleMouseButton(e)) {
        start(e.getLocationOnScreen)
      }
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      if (SwingUtilities.isMiddleMouseButton(e)) {
        anchor.foreach { point =>
          if (e.getLocationOnScreen.distance(point) >= DragThreshold)
            stop()
        }
      }
    }
  })

  component.addKeyListener(new KeyAdapter {
    override def keyPressed(e: KeyEvent): Unit = {
      if (e.getKeyCode == KeyEvent.VK_ESCAPE)
        stop()
    }
  })

  component.addFocusListener(new FocusAdapter {
    override def focusLost(e: FocusEvent): Unit = {
      stop()
    }
  })

  component.addHierarchyListener(e => {
    if ((e.getChangeFlags & HierarchyEvent.SHOWING_CHANGED) != 0 && !component.isShowing)
      stop()
  })

  private def start(point: Point): Unit = {
    anchor = Option(point)
    lastUpdate = System.nanoTime
    remainderX = 0.0
    remainderY = 0.0
    previousCursor = if (component.isCursorSet) component.getCursor else null

    component.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR))

    timer.start()
  }

  private def stop(): Unit = {
    if (anchor.isDefined) {
      timer.stop()

      anchor = None

      component.setCursor(previousCursor)
    }
  }

  private def velocity(distance: Int): Double =
    math.signum(distance.toDouble) * (math.abs(distance) - DeadZone).max(0) * Speed

  private def update(): Unit = {
    val now = System.nanoTime
    val seconds = (now - lastUpdate) / 1e9

    lastUpdate = now

    for {
      point <- anchor
      info <- Option(MouseInfo.getPointerInfo)
    } {
      val mouse = info.getLocation

      remainderX += velocity(mouse.x - point.x) * seconds
      remainderY += velocity(mouse.y - point.y) * seconds

      val x = remainderX.toInt
      val y = remainderY.toInt

      remainderX -= x
      remainderY -= y

      if (x != 0 || y != 0)
        scrollBy(x, y)
    }
  }
}
