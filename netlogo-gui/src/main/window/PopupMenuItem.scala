// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JCheckBoxMenuItem, JMenuItem }
import javax.swing.plaf.basic.{ BasicCheckBoxMenuItemUI, BasicMenuItemUI }

class PopupMenuItem(action: Action) extends JMenuItem(action) with ThemeSync {
  def this(text: String) = this(new AbstractAction(text) {
    def actionPerformed(e: ActionEvent) {}
  })

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

  def syncTheme() {
    itemUI.syncTheme()
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
