// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.Component

import org.nlogo.app.codetab.{ CodeTab, MainCodeTab }

// The class AppTabManager handles relationships between tabs (JPanels) and the two
// classes Tabs and MainCodeTabPanel that are the JTabbedPanes that contain them.

class AppTabManager( val appTabs:          AbstractTabs,
                     val mainCodeTabPanel: AbstractTabs) {

  def getAppTabs = appTabs
  def getMainCodeTabPanel = mainCodeTabPanel
  def getMainCodeTabOwner = mainCodeTabPanel
  appTabs.setName("AppTabs")
  mainCodeTabPanel.setName("MainCodeTabPanel")

  def getCodeTab = getMainCodeTabOwner.asInstanceOf[MainCodeTabPanel].getCodeTab
  def getAppsTab = appTabs.asInstanceOf[Tabs]
  private var currentTab: Component = appTabs.interfaceTab

  def getCodeTabOwner(tab: CodeTab): AbstractTabs = {
    if (tab.isInstanceOf[MainCodeTab]) mainCodeTabPanel else appTabs
  }

  def getTabOwner(tab: Component): AbstractTabs = {
    if (tab.isInstanceOf[MainCodeTab]) mainCodeTabPanel else appTabs
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
}
