// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent }
import javax.swing.{ AbstractAction, BoxLayout, JButton, JLabel }

abstract class ToolBarMenu(name: String) extends JButton with Transparent {
  setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
  setBorder(new ZoomableBorder(6, 8, 6, 8))

  protected val label = new JLabel(name)
  protected val arrow = new DropdownArrow

  add(label)
  add(new HorizontalStrut(8))
  add(arrow)

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
    menu.syncTheme()
    menu.setVisible(true)
  }

  protected def populate(menu: PopupMenu): Unit
}
