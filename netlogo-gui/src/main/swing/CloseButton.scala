// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BasicStroke, Dimension, Graphics, Stroke }
import javax.swing.JPanel

import org.nlogo.theme.InterfaceColors

class CloseButton extends JPanel with Transparent with MouseUtils with Zoomable {
  override def getPreferredSize: Dimension =
    new Dimension(zoom(16), zoom(16))

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (isHover) {
      g2d.setColor(InterfaceColors.tabCloseButtonBackgroundHover())
      g2d.fillRoundRect(0, 0, getWidth, getHeight, zoom(6), zoom(6))
    }

    val pad = zoom(4)

    val stroke: Stroke = g2d.getStroke

    g2d.setStroke(new BasicStroke(zoomClamped(1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g2d.setColor(getForeground)
    g2d.drawLine(pad, pad, getWidth - pad - 1, getHeight - pad - 1)
    g2d.drawLine(getWidth - pad - 1, pad, pad, getHeight - pad - 1)
    g2d.setStroke(stroke)
  }
}
