// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Adjustable, Color, Component, Dimension, Graphics, Rectangle }
import javax.swing.{ JButton, JComponent, JScrollBar, JScrollPane, ScrollPaneConstants }
import javax.swing.plaf.basic.BasicScrollBarUI

import org.nlogo.theme.InterfaceColors

class ScrollPane(component: Component, vScroll: Int = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                 hScroll: Int = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  extends JScrollPane(component, vScroll, hScroll) {

  // this is needed because JScrollPane defines its own ScrollBar class (Isaac B 2/25/25)
  import org.nlogo.swing.{ ScrollBar => NLScrollBar }

  setHorizontalScrollBar(new NLScrollBar(Adjustable.HORIZONTAL))
  setVerticalScrollBar(new NLScrollBar(Adjustable.VERTICAL))

  override def setBackground(color: Color): Unit = {
    super.setBackground(color)

    getViewport.setBackground(color)
    getHorizontalScrollBar.setBackground(color)
    getVerticalScrollBar.setBackground(color)
  }
}

class ScrollBar(orientation: Int) extends JScrollBar(orientation) with MouseUtils {
  setUnitIncrement(50)
  setUI(new ScrollBarUI)

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

      g2d.setColor(InterfaceColors.scrollBarBackground())
      g2d.fillRect(bounds.x, bounds.y, bounds.width, bounds.height)
    }

    override def paintThumb(g: Graphics, c: JComponent, bounds: Rectangle): Unit = {
      val g2d = Utils.initGraphics2D(g)

      if (isHover || isPressed) {
        g2d.setColor(InterfaceColors.scrollBarForegroundHover())
      } else {
        g2d.setColor(InterfaceColors.scrollBarForeground())
      }

      val radius = bounds.width.min(bounds.height)

      g2d.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, radius, radius)
    }
  }
}
