// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Adjustable, Color, Component, Dimension, Graphics, Rectangle }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ JButton, JComponent, JScrollBar, JScrollPane, ScrollPaneConstants }
import javax.swing.plaf.basic.BasicScrollBarUI

import org.nlogo.theme.InterfaceColors

class ScrollPane(component: Component, vScroll: Int = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                 hScroll: Int = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  extends JScrollPane(component, vScroll, hScroll) {

  // these need org.nlogo.swing prefix because JScrollPane already has a ScrollBar class (Isaac B 2/24/25)
  setHorizontalScrollBar(new org.nlogo.swing.ScrollBar(Adjustable.HORIZONTAL))
  setVerticalScrollBar(new org.nlogo.swing.ScrollBar(Adjustable.VERTICAL))

  override def setBackground(color: Color): Unit = {
    super.setBackground(color)

    getViewport.setBackground(color)
    getHorizontalScrollBar.setBackground(color)
    getVerticalScrollBar.setBackground(color)
  }
}

class ScrollBar(orientation: Int) extends JScrollBar(orientation) with HoverDecoration {
  private var mouseDown = false

  setUI(new ScrollBarUI)

  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = {
      mouseDown = true

      repaint()
    }

    override def mouseReleased(e: MouseEvent): Unit = {
      mouseDown = false

      repaint()
    }
  })

  private class ScrollBarUI extends BasicScrollBarUI {
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

      g2d.setColor(InterfaceColors.scrollBarBackground)
      g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
    }

    override def paintThumb(g: Graphics, c: JComponent, bounds: Rectangle): Unit = {
      val g2d = Utils.initGraphics2D(g)

      if (isHover || mouseDown) {
        g2d.setColor(InterfaceColors.scrollBarForegroundHover)
      } else {
        g2d.setColor(InterfaceColors.scrollBarForeground)
      }

      val radius = bounds.width.min(bounds.height)

      g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, radius, radius)
    }
  }
}
