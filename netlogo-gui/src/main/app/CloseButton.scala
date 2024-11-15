package org.nlogo.app

import org.nlogo.swing.Utils
import org.nlogo.theme.InterfaceColors

import java.awt.{Dimension, Graphics}
import javax.swing.JPanel

private class CloseButton extends JPanel {
  setOpaque(false)
  setBackground(InterfaceColors.TRANSPARENT)

  override def getPreferredSize: Dimension =
    new Dimension(8, 8)

  override def paintComponent(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(getForeground)
    g2d.drawLine(0, 0, getWidth - 1, getHeight - 1)
    g2d.drawLine(getWidth - 1, 0, 0, getHeight - 1)
  }
}
