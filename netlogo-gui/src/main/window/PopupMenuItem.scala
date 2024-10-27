// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Graphics, Rectangle, Toolkit }
import java.util.Map
import javax.swing.{ Action, JMenuItem }
import javax.swing.plaf.basic.BasicMenuItemUI

import org.nlogo.swing.Utils

object PopupMenuItem {
  private val DESKTOP_HINTS = Toolkit.getDefaultToolkit.getDesktopProperty("awt.font.desktophints").asInstanceOf[Map[_, _]]
}

class PopupMenuItem(action: Action) extends JMenuItem(action) {
  setUI(new BasicMenuItemUI {
    override def paintBackground(g: Graphics, menuItem: JMenuItem, bgColor: Color) {
      if (isArmed) {
        g.setColor(InterfaceColors.MENU_BACKGROUND_HOVER)
        g.fillRect(0, 0, menuItem.getWidth, menuItem.getHeight)
      }
    }

    override def paintText(g: Graphics, menuItem: JMenuItem, textRect: Rectangle, text: String) {
      val g2d = Utils.initGraphics2D(g)

      g2d.setRenderingHints(PopupMenuItem.DESKTOP_HINTS)

      if (!isEnabled)
        g2d.setColor(InterfaceColors.MENU_TEXT_DISABLED)
      else if (isArmed)
        g2d.setColor(InterfaceColors.MENU_TEXT_HOVER)
      else
        g2d.setColor(InterfaceColors.TOOLBAR_TEXT)

      val metrics = g2d.getFontMetrics(g2d.getFont)

      g2d.drawString(text, textRect.x + 4, textRect.y + textRect.height / 2 + metrics.getMaxAscent - metrics.getHeight / 2)
    }
  })
}
