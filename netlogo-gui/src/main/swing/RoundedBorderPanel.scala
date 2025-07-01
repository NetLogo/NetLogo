// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Graphics }

import org.nlogo.theme.InterfaceColors

trait RoundedBorderPanel extends Transparent with MouseUtils {
  protected var backgroundColor = Color.WHITE
  protected var backgroundHoverColor = Color.WHITE
  protected var backgroundPressedColor = Color.WHITE
  protected var borderColor = Color.BLACK
  protected var diameter = 0
  protected var hoverEnabled = false
  protected var pressedEnabled = false

  def setBackgroundColor(color: Color): Unit = {
    backgroundColor = color
  }

  def getBackgroundColor: Color =
    backgroundColor

  def setBackgroundHoverColor(color: Color): Unit = {
    backgroundHoverColor = color
  }

  def setBackgroundPressedColor(color: Color): Unit = {
    backgroundPressedColor = color
  }

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

  def enablePressed(): Unit = {
    pressedEnabled = true
  }

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (!isEnabled) {
      g2d.setColor(InterfaceColors.Transparent)
    } else if (pressedEnabled && isPressed) {
      g2d.setColor(backgroundPressedColor)
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
