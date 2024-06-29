// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.JTabbedPane

class TabsPanel(val tabManager: TabManager) extends JTabbedPane with ChangeListener {
  setFocusable(false)

  addChangeListener(this)

  def stateChanged(e: ChangeEvent) =
    tabManager.switchedTabs(getSelectedComponent)
  
  def focusSelected() =
    getSelectedComponent.requestFocus
}
