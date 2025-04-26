// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Dimension, Graphics }
import javax.swing.JPanel

class CloseButton extends JPanel with Transparent {
  override def getPreferredSize: Dimension =
    new Dimension(8, 8)

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(getForeground)
    g2d.drawLine(0, 0, getWidth - 1, getHeight - 1)
    g2d.drawLine(getWidth - 1, 0, 0, getHeight - 1)
  }
}
