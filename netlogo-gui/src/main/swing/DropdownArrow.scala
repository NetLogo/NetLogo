// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BasicStroke, Dimension, Graphics, Stroke }
import javax.swing.JPanel

import org.nlogo.theme.InterfaceColors

class DropdownArrow extends JPanel with PreferredSize {
  setOpaque(false)

  override def getPreferredSize: Dimension =
    new Dimension(Utils.zoom(9), Utils.zoom(5))

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (isEnabled) {
      g2d.setColor(InterfaceColors.toolbarText())
    } else {
      g2d.setColor(InterfaceColors.menuTextDisabled())
    }

    val strokeWidth: Float = Utils.zoomClamped(1f)
    val pad: Int = (strokeWidth / 2).toInt

    val stroke: Stroke = g2d.getStroke

    g2d.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
    g2d.drawLine(pad, pad, getWidth / 2 - pad, getHeight - strokeWidth.toInt)
    g2d.drawLine(getWidth / 2, getHeight - strokeWidth.toInt, getWidth - strokeWidth.toInt, pad)
    g2d.setStroke(stroke)
  }
}
