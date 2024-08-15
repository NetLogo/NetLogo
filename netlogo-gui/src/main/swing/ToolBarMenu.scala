// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, Dimension, Graphics }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JButton, JPopupMenu, SwingConstants }

abstract class ToolBarMenu(name: String) extends JButton(name) {
  setHorizontalAlignment(SwingConstants.LEFT)
  setBackground(Color.WHITE)
  setRolloverEnabled(false)
  setAction(new AbstractAction(name) {
    override def actionPerformed(e: ActionEvent): Unit = popup()
  })
  // This is so a user may treat this like a menu drop down:
  // clicking and holding, dragging to the item of their choice, and releasing.
  addMouseListener(new MouseAdapter() {
    override def mousePressed(e: MouseEvent): Unit = doClick()
  })

  def popup(): Unit = {
    val menu = new WrappingPopupMenu
    populate(menu)
    menu.setVisible(false)
    menu.show(this, 0, getHeight)
    menu.pack()
    menu.setVisible(true)
  }

  protected def populate(menu: JPopupMenu): Unit

  override def getPreferredSize: Dimension =
    new Dimension(super.getPreferredSize.width + 15, super.getPreferredSize.height)
  
  override def paintBorder(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)
    g2d.setColor(new Color(150, 150, 150))
    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 4, 4)
  }

  override def paintComponent(g: Graphics): Unit = {
    super.paintComponent(g)
    val g2d = Utils.initGraphics2D(g)
    g2d.setColor(new Color(100, 100, 100))
    g2d.drawLine(getWidth - 13, getHeight / 2 - 2, getWidth - 9, getHeight / 2 + 2)
    g2d.drawLine(getWidth - 9, getHeight / 2 + 2, getWidth - 5, getHeight / 2 - 2)
  }
}
