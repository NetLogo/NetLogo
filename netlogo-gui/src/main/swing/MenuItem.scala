// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, Graphics, Insets, Rectangle }
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JCheckBoxMenuItem, JMenuItem }
import javax.swing.plaf.basic.{ BasicCheckBoxMenuItemUI, BasicMenuItemUI }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class MenuItem(action: Action, showIcon: Boolean = true) extends JMenuItem(action) with Zoomable with ThemeSync {
  def this(text: String, function: () => Unit) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {
      function()
    }
  })

  def this(text: String, showIcon: Boolean) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {}
  }, showIcon)

  def this(text: String) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent): Unit = {}
  }, true)

  private val itemUI = new MenuItemUI

  setUI(itemUI)

  syncTheme()

  override def getInsets: Insets =
    Utils.zoomInsets(super.getInsets)

  def updateEnabled(): Unit = {
    if (getAction.isInstanceOf[UserAction.MenuAction])
      configurePropertiesFromAction(getAction)

    syncTheme()
  }

  override def zoomComponent(): Unit = {
    setIconTextGap(Utils.zoom(4))

    itemUI.zoom()
  }

  override def syncTheme(): Unit = {
    itemUI.syncTheme()

    if (!showIcon)
      setIcon(null)
  }

  private class MenuItemUI extends BasicMenuItemUI with ThemeSync {
    override def paintText(g: Graphics, menuItem: JMenuItem, rect: Rectangle, text: String): Unit = {
      super.paintText(g, menuItem, rect, text)

      val icon = getIcon

      if (!isEnabled && icon != null)
        getIcon.paintIcon(MenuItem.this, g, rect.x - icon.getIconWidth - getIconTextGap,
                          getHeight / 2 - icon.getIconHeight / 2)
    }

    def zoom(): Unit = {
      acceleratorFont = acceleratorFont.deriveFont(Utils.zoom(12f))
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
}

class PopupCheckBoxMenuItem(action: Action) extends JCheckBoxMenuItem(action) with Zoomable with ThemeSync {
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

  override def zoomComponent(): Unit = {
    setIconTextGap(Utils.zoom(4))
  }

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
