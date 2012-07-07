// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import org.nlogo.awt.RowLayout
import java.awt.{Graphics,Dimension,Component, Color}
import javax.swing.{SwingConstants,JCheckBox,AbstractButton,JComponent,JToolBar}

object ToolBar {
  // It'd be easier to just use JToolBar.Separator,but at least
  // on Aqua it just produces a gap,no visible separator this
  // doesn't seem good enough to me - ST 7/30/03
  class Separator extends JComponent {
    override def getMinimumSize = new Dimension(19,25)
    override def getPreferredSize = getMinimumSize
    override def paintComponent(g:Graphics) {
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
  setLayout(new RowLayout(5,Component.LEFT_ALIGNMENT,Component.CENTER_ALIGNMENT))

  def addControls(): Unit

  // we add the controls at addNotify time so that subclasses have the chance to fully construct
  // themselves before addControls() is called - ST 9/2/03
  override def addNotify(){
    super.addNotify()
    addControls()
    for(comp<-getComponents) {
      comp.setFocusable(false)
      org.nlogo.awt.Fonts.adjustDefaultFont(comp)
    }
    // kinda kludgy but we don't want to have the text below
    // the checker in the checkbox in the Code tab ev 8/24/06
    for(comp<-getComponents.collect{case b:AbstractButton => b}; if(!comp.isInstanceOf[JCheckBox])){
      comp.setVerticalTextPosition(SwingConstants.BOTTOM)
      comp.setHorizontalTextPosition(SwingConstants.CENTER)
    }
  }
}
