// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.Component

import org.nlogo.app.codetab.{ CodeTab, MainCodeTab }

// The class AppTabManager handles relationships between tabs (JPanels) and the two
// classes Tabs and MainCodeTabPanel that are the JTabbedPanes that contain them.

class AppTabManager( val appTabs:          Tabs,
                     var mainCodeTabPanel: Option[MainCodeTabPanel]) {

//class AppTabManager( val appTabs:          AbstractTabs,
//                    var mainCodeTabPanel: Option[AbstractTabs]) {


  def getAppTabs = appTabs
  // aab def getMainCodeTabPanel = mainCodeTabPanel
  def setMainCodeTabPanel(_mainCodeTabPanel: Option[MainCodeTabPanel]): Unit = {
    mainCodeTabPanel = _mainCodeTabPanel
  }

  def getMainCodeTabOwner =
    mainCodeTabPanel match {
      case None           => appTabs
      case Some(theValue) => theValue
    }
    // aab this might not be needed

  // aab def getAppsTab = appTabs.asInstanceOf[Tabs]
  def getAppsTab = appTabs
  def getCodeTab = appTabs.getCodeTab
  private var currentTab: Component = appTabs.interfaceTab

  def getCodeTabOwner(tab: CodeTab): AbstractTabs = {
    if (tab.isInstanceOf[CodeTab]) getMainCodeTabOwner else appTabs
  }

  def getTabOwner(tab: Component): AbstractTabs = {
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

  def switchToSeparateCodeWindow(): Unit = {
    // nothing to do if code tab is already separate
    val codeTabOwner = getCodeTabOwner _
    if (codeTabOwner.isInstanceOf[MainCodeTabPanel]) return
    val actualMainCodeTabPanel = new MainCodeTabPanel(getAppsTab.workspace,
      getAppsTab.interfaceTab,
      getAppsTab.externalFileManager,
      getAppsTab.codeTab,
      getAppsTab.externalFileTabs)
    mainCodeTabPanel = Some(actualMainCodeTabPanel)
    actualMainCodeTabPanel.setTabManager(this)

    // add mouse listener, which should be not set when
    // there is no code tab
    actualMainCodeTabPanel.init(getAppsTab.fileManager, getAppsTab.dirtyMonitor)
  }
}
