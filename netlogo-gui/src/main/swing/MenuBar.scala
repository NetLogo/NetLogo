// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Graphics
import javax.swing.JMenuBar

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class MenuBar extends JMenuBar with ThemeSync {
  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.menuBackground())
    g2d.fillRect(0, 0, getWidth, getHeight)
  }

  override def paintBorder(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(InterfaceColors.menuBarBorder())
    g2d.drawLine(0, getHeight - 1, getWidth, getHeight - 1)
  }

  override def syncTheme(): Unit = {
    getSubElements.foreach(_ match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    })
  }
}
