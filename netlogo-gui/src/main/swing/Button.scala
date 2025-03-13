// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Graphics
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JButton, JToggleButton }
import javax.swing.border.EmptyBorder

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class Button(action: Action) extends JButton(action) with RoundedBorderPanel with ThemeSync {
  def this(text: String, function: () => Unit) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent) {
      function()
    }
  })

  setDiameter(6)
  enableHover()
  setBorder(new EmptyBorder(3, 12, 3, 12))
  setFocusable(false)
  setContentAreaFilled(false)

  syncTheme()

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.toolbarControlBackground)
    setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover)
    setBorderColor(InterfaceColors.toolbarControlBorder)
    setForeground(InterfaceColors.toolbarText)
  }
}

class ToggleButton(action: Action) extends JToggleButton(action) with RoundedBorderPanel with ThemeSync {
  def this(text: String, function: () => Unit) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent) {
      function()
    }
  })

  setDiameter(6)
  enableHover()
  setBorder(new EmptyBorder(3, 12, 3, 12))
  setFocusable(false)
  setContentAreaFilled(false)

  syncTheme()

  override def isHover: Boolean =
    super.isHover || isSelected

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.toolbarControlBackground)
    setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover)
    setBorderColor(InterfaceColors.toolbarControlBorder)
    setForeground(InterfaceColors.toolbarText)
  }
}

class DialogButton(primary: Boolean, action: Action) extends Button(action) {
  def this(primary: Boolean, text: String, function: () => Unit) = this(primary, new AbstractAction(text) {
    override def actionPerformed(e: ActionEvent): Unit = {
      function()
    }
  })

  def this(primary: Boolean, text: String, function: (String) => Unit) = this(primary, text, () => function(text))

  setFocusable(true)

  syncTheme()

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)

    if (hasFocus) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.toolbarControlFocus)
      g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, getDiameter, getDiameter)
    }
  }

  override def syncTheme(): Unit = {
    if (primary) {
      setBackgroundColor(InterfaceColors.primaryButtonBackground)
      setBackgroundHoverColor(InterfaceColors.primaryButtonBackgroundHover)
      setBorderColor(InterfaceColors.primaryButtonBorder)
      setForeground(InterfaceColors.primaryButtonText)
    } else {
      setBackgroundColor(InterfaceColors.secondaryButtonBackground)
      setBackgroundHoverColor(InterfaceColors.secondaryButtonBackgroundHover)
      setBorderColor(InterfaceColors.secondaryButtonBorder)
      setForeground(InterfaceColors.secondaryButtonText)
    }
  }
}
