// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Graphics
import javax.swing.{ Action, JToggleButton }
import javax.swing.border.EmptyBorder

import org.nlogo.theme.InterfaceColors

class ToolBarActionButton(action: Action) extends Button(action) {
  setBorder(new EmptyBorder(6, 8, 6, 12))
  setIconTextGap(12)
}

class ToolBarToggleButton(action: Action) extends JToggleButton(action) with Transparent with MouseUtils {
  setBorder(new EmptyBorder(6, 8, 6, 12))
  setFocusable(false)
  setContentAreaFilled(false)
  setIconTextGap(12)

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (!isEnabled) {
      g2d.setColor(InterfaceColors.Transparent)
    } else if (isSelected) {
      g2d.setColor(InterfaceColors.toolbarToolSelected)
    } else if (isPressed) {
      g2d.setColor(InterfaceColors.toolbarControlBackgroundPressed)
    } else if (isHover) {
      g2d.setColor(InterfaceColors.toolbarControlBackgroundHover)
    } else {
      g2d.setColor(InterfaceColors.toolbarControlBackground)
    }

    g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)

    g2d.setColor(InterfaceColors.toolbarControlBorder)
    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 6, 6)

    setForeground(InterfaceColors.toolbarText)

    super.paintComponent(g)
  }
}
