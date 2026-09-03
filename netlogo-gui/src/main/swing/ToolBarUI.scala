// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Graphics
import javax.swing.{ Action, JToggleButton }

import org.nlogo.theme.InterfaceColors

class ToolBarActionButton(action: Action) extends Button(action) {
  setBorder(new ZoomableBorder(6, 8, 6, 12))

  override def zoomComponent(): Unit = {
    setIconTextGap(Utils.zoom(12))
  }
}

class ToolBarToggleButton(action: Action) extends JToggleButton(action) with Transparent with MouseUtils with Zoomable {
  setBorder(new ZoomableBorder(6, 8, 6, 12))
  setFocusable(false)
  setContentAreaFilled(false)

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    val diameter: Int = Utils.zoom(6)

    if (!isEnabled) {
      g2d.setColor(InterfaceColors.Transparent)
    } else if (isSelected) {
      g2d.setColor(InterfaceColors.toolbarToolSelected())
    } else if (isPressed) {
      g2d.setColor(InterfaceColors.toolbarControlBackgroundPressed())
    } else if (isHover) {
      g2d.setColor(InterfaceColors.toolbarControlBackgroundHover())
    } else {
      g2d.setColor(InterfaceColors.toolbarControlBackground())
    }

    g2d.fillRoundRect(0, 0, getWidth, getHeight, diameter, diameter)

    if (isSelected) {
      g2d.setColor(InterfaceColors.toolbarControlBorderSelected())
    } else {
      g2d.setColor(InterfaceColors.toolbarControlBorder())
    }

    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, diameter, diameter)

    if (isSelected) {
      setForeground(InterfaceColors.toolbarTextSelected())
    } else {
      setForeground(InterfaceColors.toolbarText())
    }

    super.paintComponent(g)
  }

  override def zoomComponent(): Unit = {
    setIconTextGap(Utils.zoom(12))
  }
}
