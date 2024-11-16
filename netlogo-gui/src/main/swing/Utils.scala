// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Font, Graphics, Graphics2D, Image, RenderingHints }
import java.awt.event.KeyEvent
import java.awt.image.BufferedImage
import javax.swing.{ Action, ImageIcon, InputMap, JComponent, JDialog, JWindow, KeyStroke }

import org.nlogo.core.I18N

final object Utils {
  def icon(path: String): ImageIcon = new ImageIcon(getClass.getResource(path))
  def icon(path: String, w: Int, h: Int): ImageIcon = new CenteredImageIcon(icon(path), w, h)

  def iconScaled(path: String, width: Int, height: Int) =
    new ImageIcon(icon(path).getImage.getScaledInstance(width, height, Image.SCALE_SMOOTH))
  
  def iconScaledWithColor(path: String, width: Int, height: Int, color: Color): ImageIcon = {
    val image = iconScaled(path, width, height)
    val buffered = new BufferedImage(image.getIconWidth, image.getIconHeight, BufferedImage.TYPE_INT_ARGB)

    image.paintIcon(null, buffered.getGraphics, 0, 0)

    for (y <- 0 until buffered.getHeight) {
      for (x <- 0 until buffered.getWidth) {
        val c1 = buffered.getRGB(x, y)
        val c2 = color.getRGB

        val r = ((c1 & 255) * (c2 & 255)) / 255
        val g = (((c1 >> 8) & 255) * ((c2 >> 8) & 255)) / 255
        val b = (((c1 >> 16) & 255) * ((c2 >> 16) & 255)) / 255
        val a = (((c1 >> 24) & 255) * ((c2 >> 24) & 255)) / 255

        buffered.setRGB(x, y, r | (g << 8) | (b << 16) | (a << 24))
      }
    }

    new ImageIcon(buffered)
  }

  def font(path: String): Font =
    Font.createFont(Font.TRUETYPE_FONT, getClass.getResourceAsStream(path))

  def alert(message: String, continueText: String): Unit = {
    new OptionPane(null, I18N.gui.get("common.messages.notice"), message, List(continueText), OptionPane.Icons.INFO)
  }

  def alert(title: String, message: String, details: String, continueText: String): Unit = {
    new OptionPane(null, title, s"$message\n\n$details", List(continueText), OptionPane.Icons.INFO)
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
