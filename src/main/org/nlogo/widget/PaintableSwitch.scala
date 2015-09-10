// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.widget

import java.awt.Color.BLACK
import java.awt.Dimension
import java.awt.Font
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import org.nlogo.swing.Utils.createWidgetBorder
import org.nlogo.window.InterfaceColors.SWITCH_BACKGROUND
import org.nlogo.window.InterfaceColors.SWITCH_HANDLE

import Switch.BORDERX
import Switch.BORDERY
import Switch.CHANNEL_HEIGHT
import Switch.CHANNEL_WIDTH
import Switch.MINHEIGHT
import Switch.MINWIDTH
import javax.swing.JComponent
import javax.swing.JPanel

trait PaintableSwitch extends JComponent {

  def isOn: Boolean
  def displayName: String
  protected def channel: PaintableSwitchChannel
  protected def dragger: PaintableSwitchDragger

  setBackground(SWITCH_BACKGROUND)
  setBorder(createWidgetBorder)
  setOpaque(true)
  org.nlogo.awt.Fonts.adjustDefaultFont(this)

  override def getPreferredSize: Dimension = {
    val fontMetrics: FontMetrics = getFontMetrics(getFont)
    val height: Int = (fontMetrics.getMaxDescent + fontMetrics.getMaxAscent) + 2 * BORDERY
    val width: Int = 6 * BORDERX + channel.getWidth + fontMetrics.stringWidth(displayName) + fontMetrics.stringWidth("Off")
    new Dimension(StrictMath.max(MINWIDTH, width), StrictMath.max(MINHEIGHT, height))
  }

  override def getMinimumSize = new Dimension(MINWIDTH, MINHEIGHT)
  override def getMaximumSize = new Dimension(10000, MINHEIGHT)

  override def doLayout() {
    super.doLayout()
    val scaleFactor: Float = getHeight.toFloat / MINHEIGHT.toFloat
    channel.setSize((CHANNEL_WIDTH * scaleFactor).toInt, (CHANNEL_HEIGHT * scaleFactor).toInt)
    channel.setLocation(BORDERX, (getHeight - channel.getHeight) / 2)
    dragger.setSize((channel.getWidth * 0.9).toInt, (channel.getHeight * 0.35).toInt)
    dragger.setLocation(BORDERX + (channel.getWidth - dragger.getWidth) / 2, channel.getY + (if (isOn) (0.1 * channel.getHeight).toInt else (channel.getHeight - dragger.getHeight - (0.1 * channel.getHeight).toInt)))
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    g.asInstanceOf[Graphics2D].setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)

    val fontMetrics: FontMetrics = g.getFontMetrics
    val stringAscent: Int = fontMetrics.getMaxAscent
    val controlRect: Rectangle = channel.getBounds
    g.setColor(getForeground)
    g.drawString("On", controlRect.width + BORDERX,
      (getHeight - (2 * stringAscent) - (2 * BORDERY)) / 2 + stringAscent + 1)
    g.drawString("Off", controlRect.width + BORDERX,
      (getHeight - (2 * stringAscent) - (2 * BORDERY)) / 2 + 2 * stringAscent + 1)
    val controlLabelWidth: Int =
      StrictMath.max(fontMetrics.stringWidth("On"), fontMetrics.stringWidth("Off")) + controlRect.width + 2 * BORDERX
    g.setColor(getForeground)
    g.drawString(
      org.nlogo.awt.Fonts.shortenStringToFit(displayName, getWidth - 3 * BORDERX - controlLabelWidth, fontMetrics),
      controlLabelWidth + 2 * BORDERX,
      (getHeight - fontMetrics.getHeight - (2 * BORDERY)) / 2 + stringAscent + 1)
  }
}

class PaintableSwitchChannel extends JComponent {
  setOpaque(false)
  setBackground(org.nlogo.awt.Colors.mixColors(SWITCH_BACKGROUND, BLACK, 0.5))
  override def paintComponent(g: Graphics) {
    val x: Int = (getWidth * 0.2).toInt
    val y: Int = (getHeight * 0.1).toInt
    val width: Int = (getWidth * 0.6).toInt
    val height: Int = (getHeight * 0.8).toInt
    g.setColor(getBackground)
    g.fillRect(x, y, width, height)
    createWidgetBorder.paintBorder(this, g, x, y, width, height)
  }
}

class PaintableSwitchDragger extends JPanel {
  setBackground(SWITCH_HANDLE)
  setBorder(org.nlogo.swing.Utils.createWidgetBorder)
  setOpaque(true)
}
