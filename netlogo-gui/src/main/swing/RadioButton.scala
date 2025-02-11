// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, Icon, JRadioButton }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class RadioButton(action: Action) extends JRadioButton(action) with HoverDecoration with Transparent with ThemeSync {
  def this(text: String, function: () => Unit) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {
      function()
    }
  })

  setIcon(new Icon {
    def getIconWidth: Int = 14
    def getIconHeight: Int = 14

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
      val g2d = Utils.initGraphics2D(g)

      if (isSelected) {
        if (isHover) {
          g2d.setColor(InterfaceColors.radioButtonSelectedHover)
        } else {
          g2d.setColor(InterfaceColors.radioButtonSelected)
        }

        g2d.fillOval(x, y, getIconWidth, getIconHeight)
      }

      else {
        if (isHover) {
          g2d.setColor(InterfaceColors.radioButtonBackgroundHover)
        } else {
          g2d.setColor(InterfaceColors.radioButtonBackground)
        }

        g2d.fillOval(x, y, getIconWidth, getIconHeight)

        g2d.setColor(InterfaceColors.radioButtonBorder)
        g2d.drawOval(x, y, getIconWidth, getIconHeight)
      }
    }
  })

  override def syncTheme(): Unit = {
    setForeground(InterfaceColors.dialogText)
  }
}
