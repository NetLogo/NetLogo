// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Component }
import javax.swing.{ JScrollPane, ScrollPaneConstants }

class ScrollPane(component: Component, vScroll: Int = ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                 hScroll: Int = ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  extends JScrollPane(component, vScroll, hScroll) {

  override def setBackground(color: Color) {
    super.setBackground(color)

    getViewport.setBackground(color)
    getHorizontalScrollBar.setBackground(color)
    getVerticalScrollBar.setBackground(color)
  }
}
