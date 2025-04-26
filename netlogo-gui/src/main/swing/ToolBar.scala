// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Component, Dimension, Graphics }
import javax.swing.{ JComponent, JToolBar }

import org.nlogo.awt.RowLayout

object ToolBar {
  // It'd be easier to just use JToolBar.Separator,but at least
  // on Aqua it just produces a gap,no visible separator this
  // doesn't seem good enough to me - ST 7/30/03
  class Separator extends JComponent {
    override def getMinimumSize = new Dimension(19,25)
    override def getPreferredSize = getMinimumSize
    override def paintComponent(g:Graphics): Unit = {
      g.setColor(Color.GRAY)
      for(i<-0 until 3) {
        g.drawLine(getWidth / 2 + i, 0, getWidth / 2 + i, getHeight - 2)
        if (i != 0) // because the aesthetically perfect width turned out to be odd
          g.drawLine(getWidth / 2 - i, 0, getWidth / 2 - i, getHeight - 2)
      }
    }
  }
}

abstract class ToolBar extends JToolBar {
  setFloatable(false)
  setLayout(new RowLayout(10, Component.LEFT_ALIGNMENT, Component.CENTER_ALIGNMENT))

  def addControls(): Unit

  // we add the controls at addNotify time so that subclasses have the chance to fully construct
  // themselves before addControls() is called - ST 9/2/03
  override def addNotify(): Unit = {
    super.addNotify()
    addControls()
  }
}
