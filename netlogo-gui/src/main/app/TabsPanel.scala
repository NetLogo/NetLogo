// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.JTabbedPane

import org.nlogo.app.codetab.CodeTab

class TabsPanel(val tabManager: TabManager) extends JTabbedPane with ChangeListener {
  setFocusable(false)

  addChangeListener(this)

  addMouseListener(new MouseAdapter {
    override def mouseClicked(e: MouseEvent) {
      if (e.getClickCount == 1 && e.isControlDown && tabManager.getSelectedTab.isInstanceOf[CodeTab]) {
        if (tabManager.separateTabsWindow.isAncestorOf(tabManager.getSelectedTab))
          tabManager.switchWindow(false, true)
        else
          tabManager.switchWindow(true)
      }
    }
  })

  def stateChanged(e: ChangeEvent) =
    tabManager.switchedTabs(getSelectedComponent)
  
  def focusSelected() =
    getSelectedComponent.requestFocus
}
