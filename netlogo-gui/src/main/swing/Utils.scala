// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Component, Font, Graphics, Graphics2D, Image, RenderingHints }
import java.awt.event.KeyEvent
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import javax.swing.{ Action, Icon, ImageIcon, InputMap, JComponent, JDialog, JWindow, KeyStroke }

import org.nlogo.core.I18N

object Utils {
  private var uiScale = 1.0

  def getUIScale: Double =
    uiScale

  def setUIScale(value: Double): Unit = {
    uiScale = value
  }

  def icon(path: String): ImageIcon = new ImageIcon(getClass.getResource(path))
  def icon(path: String, w: Int, h: Int): ImageIcon = new CenteredImageIcon(icon(path), w, h)

  def iconScaled(zoom: ZoomHelpers, path: String, width: Int, height: Int): ScalableIcon =
    new ScalableIcon(zoom, icon(path).getImage, width, height)

  def iconScaledWithColor(zoom: ZoomHelpers, path: String, width: Int, height: Int,
                          color: () => Color): ScalableIconWithColor =
    new ScalableIconWithColor(zoom, icon(path).getImage, width, height, color)

  def font(path: String): Font =
    Font.createFont(Font.TRUETYPE_FONT, getClass.getResourceAsStream(path))

  def alert(message: String, continueText: String): Unit = {
    new OptionPane(null, I18N.gui.get("common.messages.notice"), message, Seq(continueText), OptionPane.Icons.info)
  }

  def alert(title: String, message: String, details: String, continueText: String): Unit = {
    new OptionPane(null, title, s"$message\n\n$details", Seq(continueText), OptionPane.Icons.info)
  }

  /// Esc key handling in dialogs

  def addEscKeyAction(dialog: JDialog, action: Action): Unit =
    addEscKeyAction(dialog.getRootPane, action)

  def addEscKeyAction(window: JWindow, action: Action): Unit =
    addEscKeyAction(window.getRootPane, action)

  def addEscKeyAction(component: JComponent, action: Action): Unit =
    addEscKeyAction(component, component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), action)

  def addEscKeyAction(component: JComponent, inputMap: InputMap, action: Action): Unit = {
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "ESC_ACTION")
    component.getActionMap.put("ESC_ACTION", action)
  }

  def initGraphics2D(g: Graphics): Graphics2D = {
    val g2d = g.asInstanceOf[Graphics2D]
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    g2d
  }
}

class ScalableIcon(zoom: ZoomHelpers, image: Image, width: Int, height: Int) extends Icon {
  protected var lastZoom = 0f

  protected var icon: Icon = new ImageIcon(image)

  override def getIconWidth: Int = zoom.zoom(width)
  override def getIconHeight: Int = zoom.zoom(height)

  protected def updateIcon(): Unit = {
    if (lastZoom != zoom.getZoomFactor) {
      icon = new ImageIcon(image.getScaledInstance(zoom.zoom(width * Utils.getUIScale.toFloat).toInt,
                                                   zoom.zoom(height * Utils.getUIScale.toFloat).toInt,
                                                   Image.SCALE_SMOOTH))

      lastZoom = zoom.getZoomFactor
    }
  }

  override def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
    updateIcon()

    val g2d = Utils.initGraphics2D(g)

    val transform = g2d.getTransform
    val scaleX = transform.getScaleX
    val scaleY = transform.getScaleY

    val scaled = transform.clone.asInstanceOf[AffineTransform]

    scaled.concatenate(AffineTransform.getScaleInstance(1.0 / scaleX, 1.0 / scaleY))

    g2d.setTransform(scaled)

    icon.paintIcon(c, g2d, (x * scaleX).toInt, (y * scaleY).toInt)

    g2d.setTransform(transform)
  }
}

class ScalableIconWithColor(zoom: ZoomHelpers, image: Image, width: Int, height: Int, color: () => Color)
  extends ScalableIcon(zoom, image, width, height) {

  private var lastColor: Color = color()

  override def updateIcon(): Unit = {
    val newColor: Color = color()

    if (lastZoom != zoom.getZoomFactor || lastColor != newColor) {
      icon = new ImageIcon(image.getScaledInstance(zoom.zoom(width * Utils.getUIScale.toFloat).toInt,
                                                   zoom.zoom(height * Utils.getUIScale.toFloat).toInt,
                                                   Image.SCALE_SMOOTH))

      if (getIconWidth > 0 && getIconHeight > 0) {
        val buffered = new BufferedImage(getIconWidth * 2, getIconHeight * 2, BufferedImage.TYPE_INT_ARGB)

        icon.paintIcon(null, buffered.getGraphics, 0, 0)

        for (y <- 0 until buffered.getHeight) {
          for (x <- 0 until buffered.getWidth) {
            val c1 = buffered.getRGB(x, y)
            val c2 = newColor.getRGB

            val r = ((c1 & 255) * (c2 & 255)) / 255
            val g = (((c1 >> 8) & 255) * ((c2 >> 8) & 255)) / 255
            val b = (((c1 >> 16) & 255) * ((c2 >> 16) & 255)) / 255
            val a = (((c1 >> 24) & 255) * ((c2 >> 24) & 255)) / 255

            buffered.setRGB(x, y, r | (g << 8) | (b << 16) | (a << 24))
          }
        }

        icon = new ImageIcon(buffered)
      }

      lastZoom = zoom.getZoomFactor
      lastColor = newColor
    }
  }
}
