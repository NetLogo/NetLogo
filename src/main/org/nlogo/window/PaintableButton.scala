// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints.KEY_ANTIALIASING
import java.awt.RenderingHints.VALUE_ANTIALIAS_ON

import org.nlogo.swing.Utils.createWidgetBorder

import ButtonWidget.ButtonType
import ButtonWidget.FOREVER_GRAPHIC
import ButtonWidget.FOREVER_GRAPHIC_DARK
import InterfaceColors.BUTTON_BACKGROUND
import javax.swing.JComponent


trait PaintableButton {
  self: JComponent =>

  def buttonType: ButtonType
  def buttonUp: Boolean
  def running: Boolean
  def forever: Boolean
  def keyEnabled: Boolean
  def disabledWaitingForSetup: Boolean
  def actionKeyString: String
  def displayName: String
  def error: Exception

  setBackground(BUTTON_BACKGROUND)
  setBorder(createWidgetBorder)

  /// painting
  override def paintComponent(g: Graphics) {
    g.asInstanceOf[Graphics2D].setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON)
    def drawAsUp = buttonUp && !running
    def getPaintColor = if (drawAsUp) getBackground else getForeground
    def paintButtonRectangle(g: Graphics) {
      g.setColor(getPaintColor)
      g.fillRect(0, 0, getWidth(), getHeight())
      def renderImages(g: Graphics, dark: Boolean) {
        def maybePaintForeverImage() {
          if (forever) {
            val image = if (dark) FOREVER_GRAPHIC_DARK else FOREVER_GRAPHIC
            image.paintIcon(this, g, getWidth() - image.getIconWidth - 4, getHeight() - image.getIconHeight - 4)
          }
        }
        def maybePaintAgentImage() {
          buttonType.img(dark).map(_.paintIcon(this, g, 3, 3))
        }
        maybePaintForeverImage()
        maybePaintAgentImage()
      }
      renderImages(g, !drawAsUp)
    }
    def paintKeyboardShortcut(g: Graphics) {
      if (actionKeyString != "") {
        val ax = getSize().width - 4 - g.getFontMetrics.stringWidth(actionKeyString)
        val ay = g.getFontMetrics.getMaxAscent + 2
        if (drawAsUp) g.setColor(if (keyEnabled) Color.BLACK else Color.GRAY)
        else g.setColor(if (keyEnabled && forever) getBackground else Color.BLACK)
        g.drawString(actionKeyString, ax - 1, ay)
      }
    }
    def paintButtonText(g: Graphics) {
      val stringWidth = g.getFontMetrics.stringWidth(displayName)
      val color = {
        val c = if (drawAsUp) getForeground else getBackground
        if (error != null) c else if (disabledWaitingForSetup) Color.GRAY else c
      }
      g.setColor(color)
      val availableWidth = getSize().width - 8
      val shortString = org.nlogo.awt.Fonts.shortenStringToFit(displayName, availableWidth, g.getFontMetrics)
      val nx = if (stringWidth > availableWidth) 4 else (getSize().width / 2) - (stringWidth / 2)
      val labelHeight = g.getFontMetrics.getMaxDescent + g.getFontMetrics.getMaxAscent
      val ny = (getSize().height / 2) + (labelHeight / 2)
      g.drawString(shortString, nx, ny) //if (disabledWaitingForSetup) Color.GRAY
    }
    paintButtonRectangle(g)
    paintButtonText(g)
    paintKeyboardShortcut(g)
  }
}
