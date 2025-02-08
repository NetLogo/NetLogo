// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, Icon, JCheckBox }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class CheckBox(text: String = "") extends JCheckBox(text) with HoverDecoration with ThemeSync {
  def this(action: Action) = {
    this(action.getValue(Action.NAME).toString)

    setAction(action)
  }

  def this(text: String, function: () => Unit) = {
    this(text)

    setAction(new AbstractAction(text) {
      def actionPerformed(e: ActionEvent): Unit = {
        function()
      }
    })
  }

  setIcon(new Icon {
    def getIconWidth: Int = 14
    def getIconHeight: Int = 14

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
      val g2d = Utils.initGraphics2D(g)

      if (isSelected) {
        if (isHover && isEnabled) {
          g2d.setColor(InterfaceColors.CHECKBOX_BACKGROUND_SELECTED_HOVER)
        } else {
          g2d.setColor(InterfaceColors.CHECKBOX_BACKGROUND_SELECTED)
        }

        g2d.fillRoundRect(x, y, 14, 14, 4, 4)

        g2d.setColor(InterfaceColors.CHECKBOX_CHECK)
        g2d.drawLine(x + 3, y + 7, x + 5, y + 10)
        g2d.drawLine(x + 5, y + 10, x + 10, y + 3)
      } else {
        if (isHover && isEnabled) {
          g2d.setColor(InterfaceColors.CHECKBOX_BACKGROUND_UNSELECTED_HOVER)
        } else {
          g2d.setColor(InterfaceColors.CHECKBOX_BACKGROUND_UNSELECTED)
        }

        g2d.fillRoundRect(x, y, 14, 14, 4, 4)

        g2d.setColor(InterfaceColors.CHECKBOX_BORDER)
        g2d.drawRoundRect(x, y, 14, 14, 4, 4)
      }
    }
  })

  def syncTheme(): Unit = {} // sync done in paintIcon (Isaac B 11/4/24)
}
