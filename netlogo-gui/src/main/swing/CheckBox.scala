// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ BasicStroke, Component, Graphics, Stroke }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, Icon, JCheckBox }

import org.nlogo.theme.InterfaceColors

class CheckBox(text: String = "") extends JCheckBox(text) with MouseUtils {
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
    def getIconWidth: Int = Utils.zoom(14)
    def getIconHeight: Int = Utils.zoom(14)

    def paintIcon(c: Component, g: Graphics, x: Int, y: Int): Unit = {
      val g2d = Utils.initGraphics2D(g)

      val size: Int = getIconWidth
      val diameter: Int = Utils.zoom(4)

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

        g2d.fillRoundRect(x, y, size, size, diameter, diameter)

        val stroke: Stroke = g2d.getStroke

        g2d.setStroke(new BasicStroke(Utils.zoom(1f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND))
        g2d.setColor(InterfaceColors.checkboxCheck())
        g2d.drawLine(x + Utils.zoom(3), y + Utils.zoom(7), x + Utils.zoom(5), y + Utils.zoom(10))
        g2d.drawLine(x + Utils.zoom(5), y + Utils.zoom(10), x + Utils.zoom(10), y + Utils.zoom(3))
        g2d.setStroke(stroke)
      } else {
        if (isHover && isEnabled) {
          g2d.setColor(InterfaceColors.checkboxBackgroundUnselectedHover())
        } else {
          g2d.setColor(InterfaceColors.checkboxBackgroundUnselected())
        }

        g2d.fillRoundRect(x, y, size, size, diameter, diameter)

        g2d.setColor(InterfaceColors.checkboxBorder())
        g2d.drawRoundRect(x, y, size, size, diameter, diameter)
      }
    }
  })

  override def getIconTextGap: Int =
    Utils.zoom(super.getIconTextGap)
}
