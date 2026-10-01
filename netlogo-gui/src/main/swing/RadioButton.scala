// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Graphics }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, Icon, JRadioButton }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class RadioButton(action: Action)
  extends JRadioButton(action) with MouseUtils with Transparent with FocusUtils with Zoomable with ThemeSync {

  def this(text: String, function: () => Unit) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {
      function()
    }
  })

  setIcon(new Icon {
    def getIconWidth: Int = zoom(14)
    def getIconHeight: Int = zoom(14)

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
      val g2d = Utils.initGraphics2D(g)

      val width: Int = getIconWidth
      val height: Int = getIconHeight

      if (!isEnabled) {
        if (isSelected) {
          g2d.setColor(InterfaceColors.radioButtonBorder())
        } else {
          g2d.setColor(InterfaceColors.Transparent)
        }

        g2d.fillOval(x, y, width, height)

        g2d.setColor(InterfaceColors.radioButtonBorder())
        g2d.drawOval(x, y, width, height)
      } else if (isSelected) {
        if (isHover) {
          g2d.setColor(InterfaceColors.radioButtonSelectedHover())
        } else {
          g2d.setColor(InterfaceColors.radioButtonSelected())
        }

        g2d.fillOval(x, y, width, height)
      } else {
        if (isHover) {
          g2d.setColor(InterfaceColors.radioButtonBackgroundHover())
        } else {
          g2d.setColor(InterfaceColors.radioButtonBackground())
        }

        g2d.fillOval(x, y, width, height)

        g2d.setColor(InterfaceColors.radioButtonBorder())
        g2d.drawOval(x, y, width, height)
      }

      if (hasFocus && shouldPaintFocus) {
        if (isSelected) {
          g2d.setColor(InterfaceColors.focusAlternate())
        } else {
          g2d.setColor(InterfaceColors.focus())
        }

        g2d.drawRoundRect(x, y, width, height, width, height)
      }
    }
  })

  override def syncTheme(): Unit = {
    setForeground(InterfaceColors.dialogText())
  }

  override def paintFocus(g: Graphics): Unit = {} // focus done in paintIcon (Isaac B 9/30/26)
}
