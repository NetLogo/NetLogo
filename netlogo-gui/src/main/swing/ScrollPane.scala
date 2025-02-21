// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Component, Dimension, Graphics, Rectangle }
import javax.swing.{ JButton, JComponent, JScrollPane, ScrollPaneConstants }
import javax.swing.plaf.basic.BasicScrollBarUI

import org.nlogo.theme.InterfaceColors

class ScrollPane(component: Component, vScroll: Int = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                 hScroll: Int = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  extends JScrollPane(component, vScroll, hScroll) {

  getHorizontalScrollBar.setUI(new ScrollBarUI(false))
  getVerticalScrollBar.setUI(new ScrollBarUI(true))

  override def setBackground(color: Color): Unit = {
    super.setBackground(color)

    getViewport.setBackground(color)
    getHorizontalScrollBar.setBackground(color)
    getVerticalScrollBar.setBackground(color)
  }

  private class ScrollBarUI(vertical: Boolean) extends BasicScrollBarUI {
    override def createDecreaseButton(orientation: Int): JButton = {
      new JButton {
        override def getPreferredSize: Dimension =
          new Dimension(0, 0)
      }
    }

    override def createIncreaseButton(orientation: Int): JButton = {
      new JButton {
        override def getPreferredSize: Dimension =
          new Dimension(0, 0)
      }
    }

    override def paintTrack(g: Graphics, c: JComponent, bounds: Rectangle): Unit = {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(getBackground)
      g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
    }

    override def paintThumb(g: Graphics, c: JComponent, bounds: Rectangle): Unit = {
      val g2d = Utils.initGraphics2D(g)

      g2d.setColor(InterfaceColors.scrollBarForeground)

      if (vertical) {
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, bounds.width, bounds.width)
      } else {
        g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, bounds.height, bounds.height)
      }
    }
  }
}
