// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import javax.swing.{ Action, JButton }
import javax.swing.border.EmptyBorder

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class Button(action: Action) extends JButton(action) with RoundedBorderPanel with ThemeSync {
  setDiameter(6)
  enableHover()
  setBorder(new EmptyBorder(3, 12, 3, 12))
  setFocusable(false)
  setContentAreaFilled(false)

  def syncTheme() {
    setBackgroundColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND)
    setBackgroundHoverColor(InterfaceColors.TOOLBAR_CONTROL_BACKGROUND_HOVER)
    setBorderColor(InterfaceColors.TOOLBAR_CONTROL_BORDER)
    setForeground(InterfaceColors.TOOLBAR_TEXT)
  }
}
