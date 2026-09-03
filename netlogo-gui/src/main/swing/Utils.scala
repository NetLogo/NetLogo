// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Component, Container, Dimension, Font, Graphics, Graphics2D, Image, Insets, Rectangle,
                  RenderingHints, Window }
import java.awt.event.KeyEvent
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import javax.swing.{ Action, Icon, ImageIcon, InputMap, JComponent, JDialog, JWindow, KeyStroke }

import org.nlogo.core.I18N

object Utils {
  private var zoomFactor = 1f
  private var uiScale = 1.0

  def getZoomFactor: Float =
    zoomFactor

  def setZoomFactor(zoomFactor: Float): Unit = {
    this.zoomFactor = zoomFactor
  }

  def zoom(value: Int): Int =
    (value * zoomFactor).toInt

  def zoom(value: Float): Float =
    value * zoomFactor

  def zoomClamped(value: Int): Int =
    (value * zoomFactor).toInt.max(1)

  def zoomClamped(value: Float): Float =
    (value * zoomFactor).max(1f)

  def zoomSize(size: Dimension): Dimension =
    new Dimension(zoom(size.width), zoom(size.height))

  def zoomInsets(insets: Insets): Insets =
    new Insets(zoom(insets.top), zoom(insets.left), zoom(insets.bottom), zoom(insets.right))

  def zoomBounds(bounds: Rectangle): Rectangle =
    new Rectangle(Utils.zoom(bounds.x), Utils.zoom(bounds.y), Utils.zoom(bounds.width), Utils.zoom(bounds.height))

  def zoomComponents(component: Component): Unit = {
    component match {
      case container: Container =>
        container.getComponents.foreach(zoomComponents)

      case _ =>
    }

    component match {
      case zoomable: Zoomable =>
        zoomable.zoom()

      case _ =>
    }
  }

  def zoomMenuBar(menuBar: MenuBar): Unit = {
    menuBar.getComponents.foreach {
      case zoomable: Zoomable =>
        zoomable.zoom()

      case _ =>
    }
  }

  def zoomWindow(window: Window): Unit = {
    window.getComponents.foreach(Utils.zoomComponents)

    window match {
      case zoomable: ZoomableWindow =>
        zoomable.zoomWindow()

      case _ =>
    }
  }

  def unzoom(value: Int): Int =
    (value / zoomFactor).toInt

  def unzoomBounds(bounds: Rectangle): Rectangle =
    new Rectangle(unzoom(bounds.x), unzoom(bounds.y), unzoom(bounds.width), unzoom(bounds.height))

  def getUIScale: Double =
    uiScale

  def setUIScale(value: Double): Unit = {
    uiScale = value
  }

  def icon(path: String): ImageIcon = new ImageIcon(getClass.getResource(path))
  def icon(path: String, w: Int, h: Int): ImageIcon = new CenteredImageIcon(icon(path), w, h)

  def iconScaled(path: String, width: Int, height: Int): ScalableIcon =
    new ScalableIcon(icon(path).getImage, width, height)

  def iconScaledWithColor(path: String, width: Int, height: Int, color: () => Color): ScalableIconWithColor =
    new ScalableIconWithColor(icon(path).getImage, width, height, color)

  def font(path: String): Font =
    Font.createFont(Font.TRUETYPE_FONT, getClass.getResourceAsStream(path))

  def alert(message: String, continueText: String): Unit = {
    new OptionPane(null, I18N.gui.get("common.messages.notice"), message, Seq(continueText), OptionPane.Icons.Info)
  }

  def alert(title: String, message: String, details: String, continueText: String): Unit = {
    new OptionPane(null, title, s"$message\n\n$details", Seq(continueText), OptionPane.Icons.Info)
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

class ScalableIcon(image: Image, width: Int, height: Int) extends Icon {
  protected var lastZoom = 0f

  protected var icon: Icon = new ImageIcon(image)

  override def getIconWidth: Int = Utils.zoom(width)
  override def getIconHeight: Int = Utils.zoom(height)

  protected def updateIcon(): Unit = {
    if (lastZoom != Utils.getZoomFactor) {
      icon = new ImageIcon(image.getScaledInstance(Utils.zoom(width * Utils.getUIScale.toFloat).toInt,
                                                   Utils.zoom(height * Utils.getUIScale.toFloat).toInt,
                                                   Image.SCALE_SMOOTH))

      lastZoom = Utils.getZoomFactor
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

class ScalableIconWithColor(image: Image, width: Int, height: Int, color: () => Color)
  extends ScalableIcon(image, width, height) {

  private var lastColor: Color = color()

  override def updateIcon(): Unit = {
    val newColor: Color = color()

    if (lastZoom != Utils.getZoomFactor || lastColor != newColor) {
      icon = new ImageIcon(image.getScaledInstance(Utils.zoom(width * Utils.getUIScale.toFloat).toInt,
                                                   Utils.zoom(height * Utils.getUIScale.toFloat).toInt,
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

      lastZoom = Utils.getZoomFactor
      lastColor = newColor
    }
  }
}
