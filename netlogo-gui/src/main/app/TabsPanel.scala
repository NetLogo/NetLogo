// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Component, Dimension, Graphics, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ MouseAdapter, MouseEvent, MouseMotionAdapter }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.{ JComponent, JLabel, JPanel, JTabbedPane, SwingConstants }
import javax.swing.plaf.basic.BasicTabbedPaneUI

import org.nlogo.app.codetab.{ CodeTab, MainCodeTab }
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.{ CloseButton, FloatingTabbedPane, TabLabel, Utils }
import org.nlogo.theme.InterfaceColors

class TabsPanel(val tabManager: TabManager) extends FloatingTabbedPane with ChangeListener {
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
}
