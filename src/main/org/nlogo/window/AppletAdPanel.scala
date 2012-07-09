// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Font, Graphics, Graphics2D }
import javax.swing.{ BorderFactory, Box, BoxLayout, Icon }

class AppletAdPanel(iconListener: java.awt.event.MouseListener)
extends javax.swing.JPanel {

  locally {
    val icon =
      new RotatedIconHolder(
        new javax.swing.ImageIcon(
          classOf[AppletAdPanel].getResource(
            "/images/icon16.gif")))
    icon.addMouseListener(iconListener)
    val label = new JVertLabel("powered by NetLogo")
    label.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2))
    icon.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2))
    setBackground(Color.WHITE)
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
    add(Box.createGlue)
    add(label)
    add(icon)
  }

  private class JVertLabel(label: String) extends javax.swing.JLabel(label) {

    setFont(
      new Font(
        org.nlogo.awt.Fonts.platformFont, Font.PLAIN, 10))

    override def getPreferredSize = rotate(super.getPreferredSize)
    override def getMaximumSize = rotate(super.getMaximumSize)
    override def getMinimumSize = rotate(super.getMinimumSize)

    override def paintComponent(g: Graphics) {
      val g2d = g.asInstanceOf[Graphics2D]
      g2d.rotate(StrictMath.toRadians(90.0))
      g2d.drawString(getText, 2, -5)
    }
  }

  private class RotatedIconHolder(icon: Icon) extends javax.swing.JLabel {

    setIcon(icon)

    override def getPreferredSize = rotate(super.getPreferredSize)
    override def getMaximumSize = rotate(super.getMaximumSize)
    override def getMinimumSize = rotate(super.getMinimumSize)

    override def paintComponent(g: Graphics) {
      val g2d = g.asInstanceOf[Graphics2D]
      g2d.rotate(StrictMath.toRadians(90.0))
      getIcon.paintIcon(this, g, 2, (-getWidth) + 2)
    }
  }

  private def rotate(d: Dimension) =
    new Dimension(d.height, d.width)

}
