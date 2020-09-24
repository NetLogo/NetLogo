// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction }

import org.nlogo.core.I18N
import org.nlogo.app.codetab.{ CodeTab, MainCodeTab }

// The class AppTabManager handles relationships between tabs (JPanels) and the two
// classes Tabs and MainCodeTabPanel that are the JTabbedPanes that contain them.

class AppTabManager( val appTabs:          Tabs,
                     var mainCodeTabPanel: Option[MainCodeTabPanel]) {

  def getAppTabs = appTabs
  val frame = getAppsTab.workspace.getFrame

  def setMainCodeTabPanel(_mainCodeTabPanel: Option[MainCodeTabPanel]): Unit = {
    mainCodeTabPanel = _mainCodeTabPanel
  }

  def getMainCodeTabOwner =
    mainCodeTabPanel match {
      case None           => appTabs
      case Some(theValue) => theValue
    }
    // aab this might not be needed

  def isCodeTabSeparate =
    !getMainCodeTabOwner.isInstanceOf[Tabs]

  def isCodeTabAttached =
    getMainCodeTabOwner.isInstanceOf[Tabs]

  def getAppsTab = appTabs
  def getCodeTab = appTabs.getCodeTab
  private var currentTab: Component = appTabs.interfaceTab
  // this will need work when move temp code tabs

  def getTabOwner(tab: Component): AbstractTabs = {
    if (tab.isInstanceOf[MainCodeTab]) getMainCodeTabOwner else appTabs
  }

  def getCodeTabOwner(tab: Component): AbstractTabs = {
    if (tab.isInstanceOf[MainCodeTab]) getMainCodeTabOwner else appTabs
  }

  def setSelectedCodeTab(tab: CodeTab): Unit = {
    getCodeTabOwner(tab).setSelectedComponent(tab)
  }

  def setSelectedAppTab(index: Int): Unit = {
    appTabs.setSelectedIndex(index)
  }

  def getSelectedAppTabComponent() = appTabs.getSelectedComponent

  def getSelectedAppTabIndex() = appTabs.getSelectedIndex

  def setCurrentTab(tab: Component): Unit = {
    currentTab = tab
  }

  def getCurrentTab(): Component = {
    currentTab
  }
  def getTotalTabCount(): Int = {
    val appTabCount = appTabs.getTabCount
    mainCodeTabPanel match {
      case None           => appTabCount
      case Some(thePanel) => appTabCount + thePanel.getTabCount
    }
  }

  object SwitchFocusAction1 extends AbstractAction("Toggle") {
    def actionPerformed(e: ActionEvent) {
      // If index is already selected, unselect it
      val index = 0
      val selectedIndex = getSelectedAppTabIndex
      if (selectedIndex == index) {
        setSelectedAppTab(-1)
      }
      setSelectedAppTab(index)
    }
  }

  object SwitchFocusAction2 extends AbstractAction("Toggle") {
    def actionPerformed(e: ActionEvent) {
      // If index is already selected, unselect it
      val index = 1
      val selectedIndex = getSelectedAppTabIndex
      if (selectedIndex == index) {
        setSelectedAppTab(-1)
      }
      setSelectedAppTab(index)
    }
  }


  // will need to throw some Exceptions

  // Before the detachable code tab capability was added
  // the integers associated with tabs in the menu and the keyboard shortcuts
  // were directly connected to an index in JTabbedPane (usually an instance of the class Tabs ),
  // The tabs menu refers to tabs as TabTitle Command-Key index + 1
  // For example
  // Interface Ctrl 1
  // Tab Ctrl 2
  // Code Ctrl 3
  // File1.nls Ctrl 4
  // New File 1 Ctrl 5
  // Besides using the menu item, the user can also use the corresponding
  // keyboard shortcut. For example Ctrl 3 to access the code tab.
  // When the code tab detaches, the same number must be converted into
  // an index into the MainCodeTabPanel.
  // The following terminology will be useful.
  // appTabs = application JTabbedPane (Tabs)
  // codeTabs = mainCodeTabPanel
  // nAppTabs = the number of tabs in appTabs at the moment
  // origTabIndx = the index of a tab in appTabs when there is no mainCodeTabPanel
  // codeTabIndx = the index of a tab in mainCodeTabPanel (if it exists)
  // When the codetab is not detached
  // origTabIndx is an index in appTabs
  // When the codetab is detached
  // for origTabIndx < nAppTabs: origTabIndx is an index in appTabs
  // origTabIndx >= nAppTabs: codeTabIndx = origTabIndx - nAppTabs is an index in mainCodeTabPanel

  // Input: origTabIndx - index a tab would have if there were no separate code tab.
  // Returns (tabOwner, tabIndex)
  // tabOwner = the AbstractTabs containing the indexed tab.
  // tabIndex = the index of the tab in tabOwner.
  // This method allows for the possibility that the appTabs has no tabs,
  // although this should not occur in practice
  @throws (classOf[IndexOutOfBoundsException])
  def computeIndexPlus(origTabIndx: Int): (AbstractTabs, Int) = {
    if (origTabIndx < 0) {
      throw new IndexOutOfBoundsException
    }
    var tabOwner = appTabs.asInstanceOf[AbstractTabs]
    val appTabCount = appTabs.getTabCount
    var tabIndex = origTabIndx

    // if the origTabIndx is too large for the appTabs,
    // check if it can refer to the a separate code tab
    if (origTabIndx >= appTabCount) {
      mainCodeTabPanel match {
        case None           => throw new IndexOutOfBoundsException
        case Some(thePanel) => {
          // origTabIndx could be too large for the two Panels combined
          if (origTabIndx >= appTabCount + thePanel.getTabCount) {
            throw new IndexOutOfBoundsException
          }
          tabOwner = getMainCodeTabOwner
          tabIndex =  origTabIndx - appTabCount
        }
      }
    }
    (tabOwner, tabIndex)
  }

  // Input: tab - a tab component
  // Returns (tabOwner, tabIndex)
  // where tabOwner is the AbstractTabs containing the specified component
  // tabIndex = the index of the tab in tabOwner.
  // Returns (null, -1) if there is no tab owner for this tab component.
  def indexPlusOfTabComponent(tab: Component): (AbstractTabs, Int) = {
    var tabOwner = null.asInstanceOf[AbstractTabs]
    var tabIndex = appTabs.indexOfTabComponent(tab)
    if (tabIndex != -1) {
      tabOwner = appTabs
    } else {
      mainCodeTabPanel match {
        case Some(thePanel) => tabIndex = thePanel.indexOfTabComponent(tab)
          if (tabIndex != -1) {
            tabOwner = thePanel
          }
        case None           =>
      }
    }
    (tabOwner, tabIndex)
    //(tabOwner.asInstanceOf[AbstractTabs], tabIndex)
  }

  // Input: origTabIndx - index a tab would have if there were no separate code tab.
  // Returns (tabOwner, tabComponent)
  // tabOwner = the AbstractTabs containing the indexed tab.
  // tabComponent = the tab in tabOwner referenced by origTabIndx.
  @throws (classOf[IndexOutOfBoundsException])
  def getTabComponentPlus(origTabIndx: Int): (AbstractTabs, Component) = {
    val (tabOwner, tabIndex) = computeIndexPlus(origTabIndx)
    val tabComponent = tabOwner.getTabComponentAt(tabIndex)
    (tabOwner, tabComponent)
  }

  def switchToTabsCodeTab(): Unit = {
    // nothing to do if code tab is already part of Tabs
    val codeTabOwner = getMainCodeTabOwner
    if (!codeTabOwner.isInstanceOf[Tabs]) {
      getAppTabs.add(I18N.gui.get("tabs.code"), getAppTabs.codeTab)
      mainCodeTabPanel match {
        case Some(theValue) => theValue.getCodeTabContainer.dispose
        case None           =>
      }
      setMainCodeTabPanel(None)
      getAppTabs.codeTab.requestFocus
      // need to remove component, because will no longer exist
      // aab fix this appTabs.getAppFrame.removeLinkComponent(actualMainCodeTabPanel.getCodeTabContainer)
    }
  }

  def switchToSeparateCodeWindow(): Unit = {
    val codeTabOwner = getMainCodeTabOwner
    // Only act if code tab is part of the Tabs panel.
    // Otherwise it is already detached.
    if (codeTabOwner.isInstanceOf[Tabs]) {
      val actualMainCodeTabPanel = new MainCodeTabPanel(getAppsTab.workspace,
        getAppsTab.interfaceTab,
        getAppsTab.externalFileManager,
        getAppsTab.codeTab,
        getAppsTab.externalFileTabs)

        mainCodeTabPanel = Some(actualMainCodeTabPanel)
        actualMainCodeTabPanel.setTabManager(this)
        actualMainCodeTabPanel.add(I18N.gui.get("tabs.code"), getAppsTab.codeTab)
        actualMainCodeTabPanel.setSelectedComponent(getAppsTab.codeTab)
        getAppsTab.setSelectedComponent(appTabs.interfaceTab)
        appTabs.getAppFrame.addLinkComponent(actualMainCodeTabPanel.getCodeTabContainer)
        // add mouse listener, which should be not set when
        // there is no code tab
      }
  }
}
