// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BasicStroke, Component, Graphics, Stroke }
import javax.swing.Icon

import org.nlogo.theme.InterfaceColors

class CollapsibleArrow(private var isOpen: Boolean) extends Icon {
  def getIconWidth: Int = Utils.zoom(9)
  def getIconHeight: Int = Utils.zoom(9)

  def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.dialogText())

    val stroke: Stroke = g2d.getStroke

    g2d.setStroke(new BasicStroke(Utils.zoom(1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))

    if (isOpen) {
      g2d.drawLine(x, y + Utils.zoom(2), x + Utils.zoom(4), y + Utils.zoom(6))
      g2d.drawLine(x + Utils.zoom(4), y + Utils.zoom(6), x + Utils.zoom(8), y + Utils.zoom(2))
    } else {
      g2d.drawLine(x + Utils.zoom(2), y + Utils.zoom(8), x + Utils.zoom(6), y + Utils.zoom(4))
      g2d.drawLine(x + Utils.zoom(6), y + Utils.zoom(4), x + Utils.zoom(2), y)
    }

    g2d.setStroke(stroke)
  }

  def setOpen(open: Boolean): Unit = {
    isOpen = open
  }
}
