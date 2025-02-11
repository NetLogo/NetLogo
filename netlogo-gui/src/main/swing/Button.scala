// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

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
