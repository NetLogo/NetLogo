// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Graphics }
import javax.swing.JComponent

import org.nlogo.swing.Utils

trait RoundedBorderPanel extends JComponent {
  private var backgroundColor: Color = Color.WHITE
  private var borderColor: Color = Color.BLACK
  private var diameter: Int = 0

  setOpaque(false)
  setBackground(InterfaceColors.TRANSPARENT)

  def setBackgroundColor(color: Color) {
    backgroundColor = color
  }

  def getBackgroundColor: Color =
    backgroundColor

  def setBorderColor(color: Color) {
    borderColor = color
  }

  def setDiameter(diameter: Double) {
    this.diameter = diameter.toInt
  }

  override def paintComponent(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(backgroundColor)
    g2d.fillRoundRect(0, 0, getWidth, getHeight, diameter, diameter)
    g2d.setColor(borderColor)
    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, diameter, diameter)

    super.paintComponent(g)
  }
}
