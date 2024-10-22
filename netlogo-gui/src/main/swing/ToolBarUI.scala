// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Dimension, Graphics }
import java.awt.event.{ ActionEvent, ActionListener }

import javax.swing.{ AbstractButton, Action, JButton, JToggleButton }
import javax.swing.border.Border
import javax.swing.plaf.basic.BasicToggleButtonUI

trait AbstractToolBarButton extends AbstractButton {
  override def getPreferredSize: Dimension = {
    val ps = super.getPreferredSize
    val dim = ps.height max ps.width
    new Dimension(dim, dim)
  }
}

class ToolBarButtonUI extends BasicToggleButtonUI {
  private var color = new Color(0, 0, 0, 0)

  override def paintButtonPressed(g: Graphics, b: AbstractButton) {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(color)
    g2d.fillRoundRect(0, 0, b.getWidth, b.getHeight, 6, 6)
  }

  def setColor(color: Color) {
    this.color = color
  }
}

class ToolBarToggleButton(action: Action) extends JToggleButton(action) with AbstractToolBarButton {
  setOpaque(false)
  setBackground(new Color(0, 0, 0, 0))

  private val buttonUI = new ToolBarButtonUI

  setUI(buttonUI)

  def setColor(color: Color) {
    buttonUI.setColor(color)
  }
}

class ToolBarActionButton(action: Action) extends JButton(action) with AbstractToolBarButton {
  override def getBorder: Border = null
}

class ToolBarButton(name: String, f: => Unit) extends JButton(name) with AbstractToolBarButton {
  addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) { f }
  })
  override def getBorder: Border = null
}
