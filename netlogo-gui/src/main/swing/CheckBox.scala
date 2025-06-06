// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, Icon, JCheckBox }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class CheckBox(text: String = "") extends JCheckBox(text) with MouseUtils with ThemeSync {
  def this(action: Action) = {
    this(action.getValue(Action.NAME).toString)

    setAction(action)
  }

  def this(text: String, function: (Boolean) => Unit) = {
    this(text)

    // this is a workaround for a strange internal error that occurs if you
    // try to call isSelected directly in the AbstractAction (Isaac B 2/21/25)
    val selected = () => { isSelected }

    setAction(new AbstractAction(text) {
      def actionPerformed(e: ActionEvent): Unit = {
        function(selected())
      }
    })
  }

  setIcon(new Icon {
    def getIconWidth: Int = 14
    def getIconHeight: Int = 14

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
      val g2d = Utils.initGraphics2D(g)

      if (isSelected) {
        if (isEnabled) {
          if (isHover) {
            g2d.setColor(InterfaceColors.checkboxBackgroundSelectedHover())
          } else {
            g2d.setColor(InterfaceColors.checkboxBackgroundSelected())
          }
        } else {
          g2d.setColor(InterfaceColors.checkboxBackgroundDisabled())
        }

        g2d.fillRoundRect(x, y, 14, 14, 4, 4)

        g2d.setColor(InterfaceColors.checkboxCheck())
        g2d.drawLine(x + 3, y + 7, x + 5, y + 10)
        g2d.drawLine(x + 5, y + 10, x + 10, y + 3)
      } else {
        if (isHover && isEnabled) {
          g2d.setColor(InterfaceColors.checkboxBackgroundUnselectedHover())
        } else {
          g2d.setColor(InterfaceColors.checkboxBackgroundUnselected())
        }

        g2d.fillRoundRect(x, y, 14, 14, 4, 4)

        g2d.setColor(InterfaceColors.checkboxBorder())
        g2d.drawRoundRect(x, y, 14, 14, 4, 4)
      }
    }
  })

  override def syncTheme(): Unit = {} // sync done in paintIcon (Isaac B 11/4/24)
}
