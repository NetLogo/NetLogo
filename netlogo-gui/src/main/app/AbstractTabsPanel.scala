// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.Component
import java.awt.event.MouseEvent
import javax.swing.{ JFrame, JTabbedPane, SwingConstants }
import javax.swing.plaf.ComponentUI

import org.nlogo.app.codetab.{ CodeTab, ExternalFileManager, MainCodeTab }
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.window.GUIWorkspace

// AbstractTabsPanel contains functionality common to Tabs and CodeTabsPanel. AAB 10/2020

abstract class AbstractTabsPanel(val workspace:           GUIWorkspace,
                                 val interfaceTab:        InterfaceTab,
                                 val externalFileManager: ExternalFileManager)
  extends JTabbedPane(SwingConstants.TOP) {

  locally {
    setOpaque(false)
    setFocusable(false)
    if (System.getProperty("os.name").startsWith("Mac")) {
      try {
        val ui = Class.forName("org.nlogo.app.MacTabbedPaneUI").newInstance.asInstanceOf[ComponentUI]
        setUI(ui)
      } catch {
        case e: ClassNotFoundException =>
      }
    }
  }

  val jframe =  workspace.getFrame.asInstanceOf[JFrame]
  var tabManager: AppTabManager = null
  def setTabManager( myTabManager: AppTabManager ) : Unit = {
    tabManager = myTabManager
  }

  def getMainCodeTab(): MainCodeTab // abstract
  def getTabManager() = { tabManager }
  def getAppFrame() = { workspace.getFrame.asInstanceOf[AppFrame] }
  def getAppJFrame() = { jframe }
  var fileManager: FileManager = null
  var dirtyMonitor: DirtyMonitor = null
  var currentTab: Component = interfaceTab

  def initManagerMonitor(manager: FileManager, monitor: DirtyMonitor): Unit =  {
    fileManager = manager
    dirtyMonitor = monitor
    assert(fileManager != null && dirtyMonitor != null)
  }

  override def processMouseMotionEvent(e: MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }

  override def requestFocus() = { currentTab.requestFocus() }

  def getCodeTabsOwner(): JTabbedPane = {
    tabManager.getCodeTabsOwner.asInstanceOf[JTabbedPane]
  }

  def getTitleAtCombinedIndex(index: Int): String =  {
    val (tabOwner, tabIndex) = tabManager.ownerAndIndexFromCombinedIndex(index)
    tabOwner.getTitleAt(tabIndex)
  }

  def getComponentAtCombinedIndex(index: Int): Component =  {
    val (tabOwner, tabIndex) = tabManager.ownerAndIndexFromCombinedIndex(index)
    tabOwner.getComponentAt(tabIndex)
  }

  def setComponentAtCombinedIndex(index: Int, tab: Component): Unit = {
    val (tabOwner, tabIndex) = tabManager.ownerAndIndexFromCombinedIndex(index)
    tabOwner.setComponentAt(tabIndex, tab)
  }

  def setSelectedIndexPanels(index: Int): Unit =  {
    val (tabOwner, tabIndex) = tabManager.ownerAndIndexFromCombinedIndex(index)
    if (tabOwner.isInstanceOf[CodeTabsPanel]) {
      tabOwner.requestFocus
      tabOwner.setSelectedIndex(tabIndex)
    } else {
      val selectedIndex = tabManager.getSelectedAppTabIndex
      if (selectedIndex == tabIndex) {
        tabManager.setSelectedAppTab(-1)
      }
        tabManager.setSelectedAppTab(tabIndex)
      }
  }

  def getIndexOfCodeTab(tab: CodeTab): Int = {
    val index = getCodeTabsOwner.indexOfComponent(tab)
    index + tabManager.getAppTabsOwner.getTabCount
  }

}
