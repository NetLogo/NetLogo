// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Dimension, Graphics }
import java.awt.event.{ ActionEvent, ActionListener }
import javax.swing.{ AbstractAction, AbstractButton, Action, JButton, JToggleButton }

import org.nlogo.theme.InterfaceColors

trait AbstractToolBarButton extends AbstractButton with Transparent with MouseUtils {
  protected var square = true

  setFocusable(false)
  setContentAreaFilled(false)

  override def getPreferredSize: Dimension = {
    if (square) {
      val ps = super.getPreferredSize
      val dim = ps.height max ps.width
      new Dimension(dim, dim)
    } else {
      super.getPreferredSize
    }
  }

  override def paintComponent(g: Graphics): Unit = {
    val g2d = Utils.initGraphics2D(g)

    if (!isEnabled) {
      g2d.setColor(InterfaceColors.Transparent)
    } else if (isSelected) {
      g2d.setColor(InterfaceColors.toolbarToolSelected)
    } else if (isPressed) {
      g2d.setColor(InterfaceColors.toolbarToolPressed)
    } else if (isHover) {
      g2d.setColor(InterfaceColors.toolbarButtonHover)
    } else {
      g2d.setColor(InterfaceColors.Transparent)
    }

    g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)

    g2d.setColor(InterfaceColors.toolbarControlBorder)
    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 6, 6)

    setForeground(InterfaceColors.toolbarText)

    super.paintComponent(g)
  }
}

class ToolBarToggleButton(action: Action) extends JToggleButton(action) with AbstractToolBarButton {
  def this(name: String, function: () => Unit) = this(new AbstractAction(name) {
    override def actionPerformed(e: ActionEvent): Unit = {
      function()
    }
  })
}

class ToolBarActionButton(action: Action) extends JButton(action) with AbstractToolBarButton

class ToolBarButton(name: String, f: => Unit) extends JButton(name) with AbstractToolBarButton {
  addActionListener(new ActionListener {
    def actionPerformed(e: ActionEvent) { f }
  })
}
