// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Dimension, Graphics }
import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.{ AbstractButton, Action, JButton, JToggleButton }

import org.nlogo.theme.InterfaceColors

trait AbstractToolBarButton extends AbstractButton with Transparent with HoverDecoration {
  private var pressedColor: Color = null

  def setPressedColor(color: Color) {
    pressedColor = color
  }

  override def getPreferredSize: Dimension = {
    val ps = super.getPreferredSize
    val dim = ps.height max ps.width
    new Dimension(dim, dim)
  }

  override def paintComponent(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)

    if (isSelected) {
      if (pressedColor == null)
        g2d.setColor(InterfaceColors.toolbarButtonPressed)
      else
        g2d.setColor(pressedColor)
    }

    else if (isHover)
      g2d.setColor(InterfaceColors.toolbarButtonHover)
    else
      g2d.setColor(InterfaceColors.Transparent)

    g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)

    setForeground(InterfaceColors.toolbarText)

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
