// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BasicStroke, Component, Graphics, Stroke }
import javax.swing.Icon

import org.nlogo.theme.InterfaceColors

class CollapsibleArrow(zoom: ZoomHelpers, private var isOpen: Boolean) extends Icon {
  def getIconWidth: Int = zoom.zoom(9)
  def getIconHeight: Int = zoom.zoom(9)

  def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.dialogText())

    val stroke: Stroke = g2d.getStroke

    g2d.setStroke(new BasicStroke(zoom.zoomClamped(1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))

    if (isOpen) {
      g2d.drawLine(x, y + zoom.zoom(2), x + zoom.zoom(4), y + zoom.zoom(6))
      g2d.drawLine(x + zoom.zoom(4), y + zoom.zoom(6), x + zoom.zoom(8), y + zoom.zoom(2))
    } else {
      g2d.drawLine(x + zoom.zoom(2), y + zoom.zoom(8), x + zoom.zoom(6), y + zoom.zoom(4))
      g2d.drawLine(x + zoom.zoom(6), y + zoom.zoom(4), x + zoom.zoom(2), y)
    }

    g2d.setStroke(stroke)
  }

  def setOpen(open: Boolean): Unit = {
    isOpen = open
  }
}
