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


  // Begin methods intended for use outside of NetLogo code, e.g
  // for use in extensions. AAB 10/2020.

  /**
   * Adds a Component to the appropriate NetLogo JTabbedPane.
   * Also adds an entry to the Tabs Menu
   * If a separate code window exists, a CodeTab will be added to its JTabbedPane,
   * Otherwise the Component will be added to the Application Window JTabbedPane. AAB 10/2020.
   * New Components appear to the right of previous Components of the same
   * category non-CodeTabs or CodeTabs.
   *
   * @param tab the Component to add
   * @param title the title of the tab; may be <code>null</code>
   * @param icon the icon for the tab; may be <code>null</code>
   * @param tip the associated tooltip
   */
  def addNewTab(tab: Component, title: String = null, icon: javax.swing.Icon = null, tip: String = null): Unit = {
      if (tab == null) { throw new Exception("Tab component may not be null.") }
      val appTabsPanel = tabManager.getAppTabsPanel
      val codeTabsOwner = tabManager.getCodeTabsOwner
      if (tab.isInstanceOf[CodeTab]) {
        // If it is a code tab, it goes at the end of JTabbedPane that owns CodeTabs.
        // It becomes the last menu item. AAB 10/2020
        codeTabsOwner.insertTab(title, icon, tab, tip, codeTabsOwner.getTabCount)
        appTabsPanel.addMenuItem(tabManager.getCombinedTabCount - 1, title)
      } else {
        if (codeTabsOwner.isInstanceOf[CodeTabsPanel]) {
          // If there is a separate CodeTab Window, the the tab goes at the end of Apps JTabbedPane. AAB 10/2020
          appTabsPanel.insertTab(title, icon, tab, tip, appTabsPanel.getTabCount)
          appTabsPanel.addMenuItem(appTabsPanel.getTabCount - 1, title)
        } else {
          // Otherwise the tab goes after the other non-code-tabs, right before the
          // MainCodeTab. AAB 10/2020
          val index = appTabsPanel.indexOfComponent(getMainCodeTab)
          // Shouldn't fail. Is error handling needed? AAB 10/2020
          appTabsPanel.addMenuItem(index - 1, title)
        }
      }
    }

  /**
    * Removes the specified Component from its parent JTabbedPane
    * and remove it from the Tabs Menu
    *
    * @param tab The Component to remove.
    */
   def removeTab(tab: Component): Unit = {
    val (tabOwner, _) = tabManager.ownerAndIndexOfTab(tab)
    if (tabOwner != null) {
      tabOwner.remove(tab)
      tabManager.appTabsPanel.updateTabsMenu()
    }
  }

  /**
    * Replaces the specified Component with another Component in its parent JTabbedPane
    * If one of the Component is an instance of CodeTab, the other Component must be as well
    * in order to maintain the separate groupings of non-CodeTabs and CodeTabs
    *.
    * @param oldTab The tab to be removed
    * @param newTab The tab to replace it with
    *
    * @throw Exception if one Tab is a CodeTab and the other is not.
    */
  def replaceTab(oldTab: Component, newTab: Component): Unit = {

    if (oldTab.isInstanceOf[CodeTab] && !oldTab.isInstanceOf[CodeTab]) {
      throw new Exception("A CodeTab must be replaced by a CodeTab")
    }

    if (!oldTab.isInstanceOf[CodeTab] && oldTab.isInstanceOf[CodeTab]) {
      throw new Exception("A non-CodeTab must be replaced by a non-CodeTab")
    }

    val (tabOwner, tabIndex) = tabManager.ownerAndIndexOfTab(oldTab)
    if (tabOwner != null) {
      tabOwner.setComponentAt(tabIndex, newTab)
      tabManager.appTabsPanel.updateTabsMenu
    } else {
      throw new Exception("The old code tab does not belong to a Tabs Panel")
    }
  }

  // Return the number of tabs in the Application Window JTabbedPane plus
  // those in the separate code window (if it exists).
  def getTotalTabCount(): Int = {
    tabManager.getCombinedTabCount
  }

  /**
   * Makes a tab component selected, whether or not separate code window exists.
   *
   * @param tab the Component to be selected
   */
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

}
