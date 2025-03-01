// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Graphics }

import org.nlogo.theme.InterfaceColors

trait RoundedBorderPanel extends Transparent with HoverDecoration {
  private var backgroundColor = Color.WHITE
  private var backgroundHoverColor = Color.WHITE
  private var borderColor = Color.BLACK
  private var diameter = 0
  private var hoverEnabled = false

  def setBackgroundColor(color: Color): Unit = {
    backgroundColor = color
  }

  def setBackgroundHoverColor(color: Color): Unit = {
    backgroundHoverColor = color
  }

  def getBackgroundColor: Color =
    backgroundColor

  def setBorderColor(color: Color): Unit = {
    borderColor = color
  }

  def setDiameter(diameter: Double): Unit = {
    this.diameter = diameter.toInt
  }

  def getDiameter: Int =
    diameter

  def enableHover(): Unit = {
    hoverEnabled = true
  }

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (!isEnabled) {
      g2d.setColor(InterfaceColors.Transparent)
    } else if (hoverEnabled && isHover) {
      g2d.setColor(backgroundHoverColor)
    } else {
      g2d.setColor(backgroundColor)
    }

    g2d.fillRoundRect(0, 0, getWidth, getHeight, diameter, diameter)
    g2d.setColor(borderColor)
    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, diameter, diameter)

    super.paintComponent(g)
  }
}
