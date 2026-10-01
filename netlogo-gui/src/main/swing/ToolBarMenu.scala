// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.JLabel

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

abstract class ToolBarMenu(name: String) extends BoxRow(8) with RoundedBorderPanel with FocusUtils with ThemeSync {
  private val label = new JLabel(name) with Zoomable {
    setFocusable(false)
  }

  private val arrow = new DropdownArrow

  setBorder(new ZoomableBorder(6, 8, 6, 8))
  setFocusable(true)
  setDiameter(6)
  setFocusDiameter(6)
  setPrimaryAction(popup)

  enableHover()

  add(label)
  add(arrow)

  // This is so a user may treat this like a menu drop down:
  // clicking and holding, dragging to the item of their choice, and releasing.
  addMouseListener(new MouseAdapter {
    override def mousePressed(e: MouseEvent): Unit = popup()
  })

  def popup(): Unit = {
    val menu = new WrappingPopupMenu(this)
    populate(menu)
    menu.setVisible(false)
    menu.show(this, 0, getHeight)
    menu.pack()
    menu.syncTheme()
    menu.setVisible(true)
  }

  protected def populate(menu: PopupMenu): Unit

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.toolbarControlBackground())
    setBackgroundHoverColor(InterfaceColors.toolbarControlBackgroundHover())
    setBackgroundPressedColor(InterfaceColors.toolbarControlBackgroundPressed())
    setBorderColor(InterfaceColors.toolbarControlBorder())
    setFocusColor(InterfaceColors.focus())

    label.setForeground(InterfaceColors.toolbarText())
  }
}
