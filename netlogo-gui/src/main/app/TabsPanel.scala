// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import org.nlogo.app.codetab.CodeTab
import org.nlogo.swing.{ FloatingTabbedPane, TabLabel }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class TabsPanel(val tabManager: TabManager) extends FloatingTabbedPane with ChangeListener with ThemeSync {
  addChangeListener(this)

  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent): Unit = {
      if (e.getClickCount == 1 && e.isControlDown && tabManager.getSelectedTab.isInstanceOf[CodeTab]) {
        if (tabManager.separateTabsWindow.isAncestorOf(tabManager.getSelectedTab)) {
          tabManager.switchWindow(false, true)
        } else {
          tabManager.switchWindow(true)
        }
      }
    }
  })

  override def addTabWithLabel(tab: Component, label: TabLabel): Unit = {
    super.addTabWithLabel(tab, label)

    if (System.getProperty("os.name").toLowerCase.startsWith("mac")) {
      setToolTipTextAt(getTabCount - 1, s"Cmd+${tabManager.getTotalTabIndex(tab) + 1}")
    } else {
      setToolTipTextAt(getTabCount - 1, s"Ctrl+${tabManager.getTotalTabIndex(tab) + 1}")
    }
  }

  def stateChanged(e: ChangeEvent): Unit =
    tabManager.switchedTabs(getSelectedComponent)

  override def syncTheme(): Unit = {
    setBackground(InterfaceColors.toolbarBackground())
  }
}
