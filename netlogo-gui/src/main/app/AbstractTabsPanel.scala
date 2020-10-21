// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.{ Component }
import java.awt.event.{ MouseEvent }
import javax.swing.{ JFrame, JTabbedPane, SwingConstants }
import javax.swing.plaf.ComponentUI

import org.nlogo.app.codetab.{ CodeTab, ExtendedCodeTab, ExternalFileManager, MainCodeTab }
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.window.{ GUIWorkspace }

// AbstractTabsPanel contains functionality common to Tabs and CodeTabsPanel

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

  def setPanelsSelectedComponent(tab: Component): Unit = {
    val (tabOwner, tabIndex) = tabManager.ownerAndIndexOfTab(tab)
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

  // Begin methods intended for use outside of NetLogo code, e.g
  // for use in extensions

  /**
    * Removes the specified Component from the appropriate JTabbedPane
    *
    * @param tab The Component to remove.
    */
   def removeTab(tab: Component): Unit = {
    val (tabOwner, _) = tabManager.ownerAndIndexOfTab(tab)
    if (tabOwner != null) {
      tabOwner.remove(tab)
    }
  }

  /**
    * Replaces the specified ExtendedCodeTab with another ExtendedCodeTab in its JTabbedPane
    *
    * @param oldTab The tab to be removed
    * @param newTab The tab to replace it with
    */
  def replaceTab(oldTab: ExtendedCodeTab, newTab: ExtendedCodeTab): Unit = {
    val (tabOwner, tabIndex) = tabManager.ownerAndIndexOfTab(oldTab)
    if (tabOwner != null) {
      tabOwner.setComponentAt(tabIndex, newTab)
    }
  }

  /**
   * This method adds a tab to the appropriate JTabbedPane.
   *
   * @param tab the Component to add
   * @param title the title of the tab; may be <code>null</code>
   * @param icon the icon for the tab; may be <code>null</code>
   * @param tip the associated tooltip
   */
  def addNewTab(tab: Component, title: String = null, icon: javax.swing.Icon = null, tip: String = null): Unit = {
      // improve error handling
      if (tab == null) { throw new Exception }
      val codeTabsOwner = tabManager.getCodeTabsOwner
      if (tab.isInstanceOf[CodeTab]) {
        // if it is a code tab, it goes at the end of JTabbedPane that owns CodeTabs
        codeTabsOwner.insertTab(title, icon, tab, tip, codeTabsOwner.getTabCount)
      } else {
        val appTabsPanel = tabManager.getAppTabsPanel
        if (codeTabsOwner.isInstanceOf[CodeTabsPanel]) {
          // If there is a separate CodeTab Window, the tab it goes at the end of Apps JTabbedPane
          appTabsPanel.insertTab(title, icon, tab, tip, appTabsPanel.getTabCount)
        } else {
          // Otherwise the tab goes after the other non-code-tabs, right before the
          // MainCodeTab
          val index = appTabsPanel.indexOfComponent(getMainCodeTab)
          // shouldn't fail ? error handling
          appTabsPanel.insertTab(title, icon, tab, tip, index)
        }
      }
    }

  def getTotalTabCount(): Int = {
    tabManager.getCombinedTabCount
  }
}
