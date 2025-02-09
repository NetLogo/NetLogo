// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import javax.swing.Icon

import org.nlogo.theme.InterfaceColors

class CollapsibleArrow extends Icon {
  private var isOpen = false

  def getIconWidth = 9
  def getIconHeight = 9

  def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.DIALOG_TEXT)

    if (isOpen) {
      g2d.drawLine(x, y + 2, x + 4, y + 6)
      g2d.drawLine(x + 4, y + 6, x + 8, y + 2)
    } else {
      g2d.drawLine(x + 2, y + 8, x + 6, y + 4)
      g2d.drawLine(x + 6, y + 4, x + 2, y)
    }
  }

  def setOpen(open: Boolean): Unit = {
    isOpen = open
  }
}
