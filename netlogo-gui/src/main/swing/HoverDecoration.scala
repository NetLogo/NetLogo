// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Cursor }
import java.awt.event.{ MouseAdapter, MouseEvent }

object HoverDecoration {
  private val HAND_CURSOR = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
}

trait HoverDecoration extends Component {
  private var useHandCursor = false
  private var hover = false

  addMouseListener(new MouseAdapter {
    override def mouseEntered(e: MouseEvent) {
      if (useHandCursor)
        setCursor(HoverDecoration.HAND_CURSOR)
      
      hover = true

      repaint()
    }

    override def mouseExited(e: MouseEvent) {
      setCursor(null)

      hover = false

      repaint()
    }
  })

  def setHandCursor() {
    useHandCursor = true
  }

  def isHover: Boolean =
    hover
}
