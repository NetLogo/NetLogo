// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Component, Graphics }
import javax.swing.{ Action, Icon, JCheckBox }

import org.nlogo.swing.{ HoverDecoration, Utils }

class CheckBox(text: String) extends JCheckBox(text) with HoverDecoration {
  def this(action: Action) = {
    this(action.getValue(Action.NAME).toString)

    setAction(action)
  }

  setIcon(new Icon {
    def getIconWidth: Int = 14
    def getIconHeight: Int = 14

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
      val g2d = Utils.initGraphics2D(g)

      if (isSelected) {
        if (isHover)
          g2d.setColor(InterfaceColors.CHECKBOX_BACKGROUND_SELECTED_HOVER)
        else
          g2d.setColor(InterfaceColors.CHECKBOX_BACKGROUND_SELECTED)

        g2d.fillRoundRect(x, y, 14, 14, 4, 4)

        g2d.setColor(InterfaceColors.CHECKBOX_CHECK)
        g2d.drawLine(x + 3, y + 7, x + 5, y + 10)
        g2d.drawLine(x + 5, y + 10, x + 10, y + 3)
      }

      else {
        if (isHover)
          g2d.setColor(InterfaceColors.CHECKBOX_BACKGROUND_UNSELECTED_HOVER)
        else
          g2d.setColor(InterfaceColors.CHECKBOX_BACKGROUND_UNSELECTED)

        g2d.fillRoundRect(x, y, 14, 14, 4, 4)

        g2d.setColor(InterfaceColors.CHECKBOX_BORDER)
        g2d.drawRoundRect(x, y, 14, 14, 4, 4)
      }
    }
  })
}
