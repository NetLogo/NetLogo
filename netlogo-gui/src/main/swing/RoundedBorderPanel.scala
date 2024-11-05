// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Graphics }
import javax.swing.JComponent

import org.nlogo.theme.InterfaceColors

trait RoundedBorderPanel extends JComponent with HoverDecoration {
  private var backgroundColor: Color = Color.WHITE
  private var backgroundHoverColor: Color = Color.WHITE
  private var borderColor: Color = Color.BLACK
  private var diameter: Int = 0
  private var hoverEnabled = false

  setOpaque(false)
  setBackground(InterfaceColors.TRANSPARENT)

  def setBackgroundColor(color: Color) {
    backgroundColor = color
  }

  def setBackgroundHoverColor(color: Color) {
    backgroundHoverColor = color
  }

  def getBackgroundColor: Color =
    backgroundColor

  def setBorderColor(color: Color) {
    borderColor = color
  }

  def setDiameter(diameter: Double) {
    this.diameter = diameter.toInt
  }

  def enableHover() {
    hoverEnabled = true
  }

  override def paintComponent(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)

    if (isEnabled && hoverEnabled && isHover)
      g2d.setColor(backgroundHoverColor)
    else
      g2d.setColor(backgroundColor)

    g2d.fillRoundRect(0, 0, getWidth, getHeight, diameter, diameter)
    g2d.setColor(borderColor)
    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, diameter, diameter)

    super.paintComponent(g)
  }
}
