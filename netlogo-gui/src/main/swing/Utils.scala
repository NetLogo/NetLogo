// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Frame }
import java.awt.event.KeyEvent

import javax.swing.{ Action, BorderFactory, ImageIcon, InputMap, JComponent, JDialog,
  JWindow, KeyStroke }

final object Utils {
  val utilsClass = getClass
  def icon(path: String): ImageIcon = new ImageIcon(utilsClass.getResource(path))
  def icon(path: String, w: Int, h: Int): ImageIcon = new CenteredImageIcon(icon(path), w, h)

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

  /// borders

  private val WidgetBorder = BorderFactory.createCompoundBorder(
    BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY),
    BorderFactory.createRaisedBevelBorder)

  private val WidgetPressedBorder = BorderFactory.createCompoundBorder(
    BorderFactory.createMatteBorder(1, 1, 0, 0, java.awt.Color.GRAY),
    BorderFactory.createLoweredBevelBorder)

  def createWidgetBorder() = WidgetBorder
  def createWidgetPressedBorder() = WidgetPressedBorder

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
}
