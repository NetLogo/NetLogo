// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Dimension, Graphics }
import javax.swing.JPanel

import org.nlogo.theme.InterfaceColors

class CloseButton extends JPanel with Transparent with MouseUtils {
  override def getPreferredSize: Dimension =
    new Dimension(16, 16)

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (isHover) {
      g2d.setColor(InterfaceColors.tabCloseButtonBackgroundHover())
      g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)
    }

    g2d.setColor(getForeground)
    g2d.drawLine(4, 4, getWidth - 5, getHeight - 5)
    g2d.drawLine(getWidth - 5, 4, 4, getHeight - 5)
  }
}
