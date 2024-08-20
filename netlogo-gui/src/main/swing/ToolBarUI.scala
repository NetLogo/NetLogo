// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.Dimension

import javax.swing.{ AbstractButton, Action, JButton, JToggleButton }
import javax.swing.border.Border
import java.awt.event.{ ActionEvent, ActionListener }

trait ToolBarButtonUI extends AbstractButton {
  override def getPreferredSize: Dimension = {
    val ps = super.getPreferredSize
    val dim = ps.height max ps.width
    new Dimension(dim, dim)
  }
}

class ToolBarToggleButton(action: Action) extends JToggleButton(action) with ToolBarButtonUI

class ToolBarActionButton(action: Action) extends JButton(action) with ToolBarButtonUI {
  override def getBorder: Border = null
}

class ToolBarButton(f: => Unit) extends JButton with ToolBarButtonUI {
  addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) { f }
  })
  override def getBorder: Border = null
}
