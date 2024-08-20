// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Color, FlowLayout, Graphics }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, JButton, JLabel, JPopupMenu }

abstract class ToolBarMenu(name: String) extends JButton {
  setOpaque(false)
  setBackground(new Color(0, 0, 0, 0))
  setBorder(null)

  setLayout(new FlowLayout)

  add(new JLabel(name))
  add(new DropdownArrow)

  setAction(new AbstractAction {
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

  override def paintComponent(g: Graphics) {
    val g2d = Utils.initGraphics2D(g)

    g2d.setColor(Color.WHITE)
    g2d.fillRoundRect(0, 0, getWidth, getHeight, 6, 6)
    g2d.setColor(new Color(150, 150, 150))
    g2d.drawRoundRect(0, 0, getWidth - 1, getHeight - 1, 6, 6)

    super.paintComponent(g)
  }
}
