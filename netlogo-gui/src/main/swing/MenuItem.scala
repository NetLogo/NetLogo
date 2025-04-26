// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, Graphics, Rectangle }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JCheckBoxMenuItem, JMenuItem }
import javax.swing.plaf.basic.{ BasicCheckBoxMenuItemUI, BasicMenuItemUI }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class MenuItem(action: Action, showIcon: Boolean = true) extends JMenuItem(action) with ThemeSync {
  def this(text: String, showIcon: Boolean) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {}
  }, showIcon)

  def this(text: String) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {}
  }, true)

  private val itemUI = new BasicMenuItemUI with ThemeSync {
    override def paintText(g: Graphics, menuItem: JMenuItem, rect: Rectangle, text: String): Unit = {
      super.paintText(g, menuItem, rect, text)

      val icon = getIcon

      if (!isEnabled && icon != null)
        getIcon.paintIcon(MenuItem.this, g, rect.x - icon.getIconWidth - getIconTextGap,
                          getHeight / 2 - icon.getIconHeight / 2)
    }

    override def syncTheme(): Unit = {
      setForeground(InterfaceColors.toolbarText())

      selectionBackground = InterfaceColors.menuBackgroundHover()
      selectionForeground = InterfaceColors.menuTextHover()
      acceleratorForeground = InterfaceColors.toolbarText()
      acceleratorSelectionForeground = InterfaceColors.menuTextHover()
      disabledForeground = InterfaceColors.menuTextDisabled()
    }
  }

  setUI(itemUI)
  syncTheme()

  override def syncTheme(): Unit = {
    itemUI.syncTheme()

    if (!showIcon)
      setIcon(null)
  }
}

class PopupCheckBoxMenuItem(action: Action) extends JCheckBoxMenuItem(action) with ThemeSync {
  private val itemUI = new BasicCheckBoxMenuItemUI with ThemeSync {
    override def syncTheme(): Unit = {
      setForeground(InterfaceColors.toolbarText())

      selectionBackground = InterfaceColors.menuBackgroundHover()
      selectionForeground = InterfaceColors.menuTextHover()
      acceleratorForeground = InterfaceColors.toolbarText()
      acceleratorSelectionForeground = InterfaceColors.menuTextHover()
      disabledForeground = InterfaceColors.menuTextDisabled()
    }
  }

  setUI(itemUI)

  override def syncTheme(): Unit = {
    itemUI.syncTheme()
  }
}

class CustomMenuItem(component: Component, action: Action) extends MenuItem(action) {
  locally {
    val insets = getInsets

    setPreferredSize(new Dimension(component.getPreferredSize.width + insets.left + insets.right,
                                   component.getPreferredSize.height + insets.top + insets.bottom))
  }

  add(component)
}
