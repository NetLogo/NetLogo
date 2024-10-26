// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Dimension, Graphics }
import java.awt.event.{ ActionEvent, ActionListener }

import javax.swing.{ AbstractButton, Action, JButton, JToggleButton }

trait AbstractToolBarButton extends AbstractButton with HoverDecoration {
  private var pressedColor = new Color(0, 0, 0, 0)
  private var hoverColor = new Color(0, 0, 0, 0)

  setOpaque(false)
  setBackground(new Color(0, 0, 0, 0))

  def setPressedColor(color: Color) {
    pressedColor = color
  }

  def setHoverColor(color: Color) {
    hoverColor = color
  }

  override def getPreferredSize: Dimension = {
    val ps = super.getPreferredSize
    val dim = ps.height max ps.width
    new Dimension(dim, dim)
  }

  override def paintComponent(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)

    if (isSelected)
      g2d.setColor(pressedColor)
    else if (isHover)
      g2d.setColor(hoverColor)
    else
      g2d.setColor(new Color(0, 0, 0, 0))

    g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)

    super.paintComponent(g)
  }
}

class ToolBarToggleButton(action: Action) extends JToggleButton(action) with AbstractToolBarButton

class ToolBarActionButton(action: Action) extends JButton(action) with AbstractToolBarButton

class ToolBarButton(name: String, f: => Unit) extends JButton(name) with AbstractToolBarButton {
  addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) { f }
  })
}
