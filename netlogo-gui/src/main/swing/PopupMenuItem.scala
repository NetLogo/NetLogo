// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JCheckBoxMenuItem, JMenuItem }
import javax.swing.plaf.basic.{ BasicCheckBoxMenuItemUI, BasicMenuItemUI }

import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class PopupMenuItem(action: Action, showIcon: Boolean = true) extends JMenuItem(action) with ThemeSync {
  def this(text: String, showIcon: Boolean) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent) {}
  }, showIcon)

  def this(text: String) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent) {}
  }, true)

  private val itemUI = new BasicMenuItemUI with ThemeSync {
    def syncTheme() {
      setForeground(InterfaceColors.TOOLBAR_TEXT)

      selectionBackground = InterfaceColors.MENU_BACKGROUND_HOVER
      selectionForeground = InterfaceColors.MENU_TEXT_HOVER
      acceleratorForeground = InterfaceColors.TOOLBAR_TEXT
      acceleratorSelectionForeground = InterfaceColors.MENU_TEXT_HOVER
      disabledForeground = InterfaceColors.MENU_TEXT_DISABLED
    }
  }

  setUI(itemUI)
  syncTheme()

  def syncTheme() {
    itemUI.syncTheme()

    if (!showIcon)
      setIcon(null)
  }
}

class PopupCheckBoxMenuItem(action: Action) extends JCheckBoxMenuItem(action) with ThemeSync {
  private val itemUI = new BasicCheckBoxMenuItemUI with ThemeSync {
    def syncTheme() {
      setForeground(InterfaceColors.TOOLBAR_TEXT)

      selectionBackground = InterfaceColors.MENU_BACKGROUND_HOVER
      selectionForeground = InterfaceColors.MENU_TEXT_HOVER
      acceleratorForeground = InterfaceColors.TOOLBAR_TEXT
      acceleratorSelectionForeground = InterfaceColors.MENU_TEXT_HOVER
      disabledForeground = InterfaceColors.MENU_TEXT_DISABLED
    }
  }

  setUI(itemUI)

  def syncTheme() {
    itemUI.syncTheme()
  }
}
