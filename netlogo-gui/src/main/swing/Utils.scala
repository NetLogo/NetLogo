// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Font, Frame, Graphics, Graphics2D, Image, RenderingHints }
import java.awt.event.KeyEvent

import javax.swing.{ Action, ImageIcon, InputMap, JComponent, JDialog, JWindow, KeyStroke }

final object Utils {
  def icon(path: String): ImageIcon = new ImageIcon(getClass.getResource(path))
  def icon(path: String, w: Int, h: Int): ImageIcon = new CenteredImageIcon(icon(path), w, h)

  def iconScaled(path: String, width: Int, height: Int) =
    new ImageIcon(icon(path).getImage.getScaledInstance(width, height, Image.SCALE_SMOOTH))

  def font(path: String): Font =
    Font.createFont(Font.TRUETYPE_FONT, getClass.getResourceAsStream(path))

  def alert(message: String, continueText: String): Unit = {
    val bogusFrame = new Frame
    bogusFrame.pack() // otherwise OptionDialog will fail to get font metrics
    OptionDialog.showMessage(bogusFrame, "Notice", message, Array(continueText))
  }

  def alert(title: String, message: String, details: String, continueText: String): Unit = {
    val bogusFrame = new Frame
    bogusFrame.pack() // otherwise OptionDialog will fail to get font metrics
    OptionDialog.showMessage(bogusFrame, title, s"$message\n\n$details", Array(continueText))
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
