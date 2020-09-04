// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.Component

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

  def getAppsTab = appTabs
  def getCodeTab = appTabs.getCodeTab
  private var currentTab: Component = appTabs.interfaceTab

  def getTabOwner(tab: Component): AbstractTabs = {
    if (tab.isInstanceOf[MainCodeTab]) getMainCodeTabOwner else appTabs
  }

  // this will need work when move temp code tabs
  def getCodeTabOwner(tab: Component): AbstractTabs = {
    if (tab.isInstanceOf[MainCodeTab]) getMainCodeTabOwner else appTabs
  }

  def setSelectedCodeTab(tab: CodeTab): Unit = {
    getCodeTabOwner(tab).setSelectedComponent(tab)
  }

  def setCurrentTab(tab: Component): Unit = {
    currentTab = tab
  }

  def getCurrentTab(): Component = {
    currentTab
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
        // only need to do this if previous tab was interface tab
        appTabs.interfaceTab.getMonitorManager.hideAll()
        appTabs.getAppFrame.addLinkComponent(actualMainCodeTabPanel.getCodeTabContainer)
        //actualMainCodeTabPanel.codeTabContainer.requestFocus()
        //getAppsTab.codeTab.requestFocusInWindow()
        // add mouse listener, which should be not set when
        // there is no code tab
      }
  }
}
