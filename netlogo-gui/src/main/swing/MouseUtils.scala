// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Cursor }
import java.awt.event.{ MouseAdapter, MouseEvent }

object MouseUtils {
  private val HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
}

trait MouseUtils extends Component {
  private var useHandCursor = false
  private var hover = false
  private var pressed = false

  addMouseListener(new MouseAdapter {
    override def mouseEntered(e: MouseEvent): Unit = {
      if (useHandCursor)
        setCursor(MouseUtils.HAND_CURSOR)

      hover = true

      repaint()
    }

    override def mouseExited(e: MouseEvent): Unit = {
      if (!contains(e.getPoint)) {
        setCursor(null)

        hover = false

        repaint()
      }
    }

    override def mousePressed(e: MouseEvent): Unit = {
      pressed = true

      repaint()
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      pressed = false

      repaint()
    }
  })

  def setHandCursor(): Unit = {
    useHandCursor = true
  }

  def resetMouseState(): Unit = {
    hover = false
    pressed = false
  }

  def isHover: Boolean =
    hover

  def isPressed: Boolean =
    pressed
}
