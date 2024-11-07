// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import javax.swing.{ Action, Icon, JRadioButton }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class RadioButton(action: Action) extends JRadioButton(action) with HoverDecoration with ThemeSync {
  setOpaque(false)
  setBackground(InterfaceColors.TRANSPARENT)

  setIcon(new Icon {
    def getIconWidth: Int = 14
    def getIconHeight: Int = 14

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
      val g2d = Utils.initGraphics2D(g)

      if (isSelected) {
        if (isHover)
          g2d.setColor(InterfaceColors.RADIO_BUTTON_SELECTED_HOVER)
        else
          g2d.setColor(InterfaceColors.RADIO_BUTTON_SELECTED)

        g2d.fillOval(x, y, getIconWidth, getIconHeight)
      }

      else {
        if (isHover)
          g2d.setColor(InterfaceColors.RADIO_BUTTON_BACKGROUND_HOVER)
        else
          g2d.setColor(InterfaceColors.RADIO_BUTTON_BACKGROUND)

        g2d.fillOval(x, y, getIconWidth, getIconHeight)

        g2d.setColor(InterfaceColors.RADIO_BUTTON_BORDER)
        g2d.drawOval(x, y, getIconWidth, getIconHeight)
      }
    }
  })

  def syncTheme() {
    setForeground(InterfaceColors.DIALOG_TEXT)
  }
}
