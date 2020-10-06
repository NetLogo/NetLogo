// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ Action, AbstractAction, ActionMap, InputMap, JComponent, JTabbedPane }

import org.nlogo.app.codetab.{ CodeTab }
import org.nlogo.swing.{ UserAction }

// The class AppTabManager handles relationships between tabs (JPanels) and the two
// classes Tabs and CodeTabsPanel that are the JTabbedPanes that contain them.

class AppTabManager( val appTabsPanel:          Tabs,
                     var codeTabsPanelOption: Option[CodeTabsPanel]) {

  // The appTabsPanel and the main code tab are unique unchanging entities
  // of class Tabs and MainCodeTab respectively
  def getAppTabsPanel = { appTabsPanel }
  def getCodeTab = { appTabsPanel.getCodeTab }

  // The separate window and JTabbedPane containing the main code tab and
  // other code tabs can come in an out of existence, and are hence
  // represented by a scala Option, which has value 'None' when code tabs
  // are in the Application window and JTabbedPane
  def setCodeTabsPanelOption(_codeTabsPanelOption: Option[CodeTabsPanel]): Unit = {
    codeTabsPanelOption = _codeTabsPanelOption
  }

  // might want access to these owner methods to be only in the app package
  def getCodeTabsOwner = {
    codeTabsPanelOption match {
      case None           => appTabsPanel
      case Some(theValue) => theValue
    }
  }

  def getAppTabsOwner = { appTabsPanel }

  def getTabOwner(tab: Component): AbstractTabs = {
    if (tab.isInstanceOf[CodeTab]) getCodeTabsOwner else appTabsPanel
  }

  def isCodeTabSeparate = {
    codeTabsPanelOption match {
      case None           => false
      case Some(theValue) => true
    }
  }

  def setSelectedCodeTab(tab: CodeTab): Unit = {
    getCodeTabsOwner.setSelectedComponent(tab)
  }

  def setSelectedAppTab(index: Int): Unit = {
    appTabsPanel.setSelectedIndex(index)
  }

  def getSelectedAppTabComponent() = { appTabsPanel.getSelectedComponent }

  def getSelectedAppTabIndex() = { appTabsPanel.getSelectedIndex }

  def getTotalTabCount(): Int = {
    val appTabCount = appTabsPanel.getTabCount
    codeTabsPanelOption match {
      case None           => appTabCount
      case Some(thePanel) => appTabCount + thePanel.getTabCount
    }
  }

  private var currentTab: Component = { appTabsPanel.interfaceTab }

  def getCurrentTab(): Component = {
    currentTab
  }

  def setCurrentTab(tab: Component): Unit = {
    currentTab = tab
  }

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
  // an index into the CodeTabsPanel.
  // The following terminology will be useful.
  // appTabsPanel = application JTabbedPane (Tabs)
  // codeTabsPanel = CodeTabsPanel when it exists
  // nAppTabsPanel = the number of tabs in appTabsPanel at the moment
  // origTabIndx = the index of a tab in appTabsPanel when there is no codeTabsPanel
  // codeTabIndx = the index of a tab in codeTabsPanel (if it exists)
  // When the codetab is not detached
  // origTabIndx is an index in appTabsPanel
  // When the codetab is detached
  // for origTabIndx < nAppTabsPanel: origTabIndx is an index in appTabsPanel
  // origTabIndx >= nAppTabsPanel: codeTabIndx = origTabIndx - nAppTabsPanel is an index in codeTabsPanel

  // Input: origTabIndx - index a tab would have if there were no separate code tab.
  // Returns (tabOwner, tabIndex)
  // tabOwner = the AbstractTabs containing the indexed tab.
  // tabIndex = the index of the tab in tabOwner.
  // This method allows for the possibility that the appTabsPanel has no tabs,
  // although this should not occur in practice
  @throws (classOf[IndexOutOfBoundsException])
  def computeIndexPlus(origTabIndx: Int): (AbstractTabs, Int) = {
    if (origTabIndx < 0) {
      throw new IndexOutOfBoundsException
    }
    var tabOwner = appTabsPanel.asInstanceOf[AbstractTabs]
    val appTabCount = appTabsPanel.getTabCount
    var tabIndex = origTabIndx

    // if the origTabIndx is too large for the appTabsPanel,
    // check if it can refer to the a separate code tab
    if (origTabIndx >= appTabCount) {
      codeTabsPanelOption match {
        case None           => throw new IndexOutOfBoundsException
        case Some(thePanel) => {
          // origTabIndx could be too large for the two Panels combined
          if (origTabIndx >= appTabCount + thePanel.getTabCount) {
            throw new IndexOutOfBoundsException
          }
          tabOwner = getCodeTabsOwner
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
  def ownerAndIndexOfTab(tab: Component): (AbstractTabs, Int) = {
    var tabOwner = null.asInstanceOf[AbstractTabs]
    var tabIndex = appTabsPanel.indexOfComponent(tab)
    if (tabIndex != -1) {
      tabOwner = appTabsPanel
    } else {
      codeTabsPanelOption match {
        case Some(thePanel) => tabIndex = thePanel.indexOfComponent(tab)
          if (tabIndex != -1) {
            tabOwner = thePanel
          }
        case None           =>
      }
    }
    (tabOwner, tabIndex)
  }

  def printAllTabs(): Unit = {
    println("\nAppTabsPanel count " + appTabsPanel.getTabCount)
    printTabsOfTabsPanel(appTabsPanel)
    codeTabsPanelOption match {
      case Some(thePanel) => {
        println("CodeTabs count " + thePanel.getTabCount)
        printTabsOfTabsPanel(thePanel)
      }
      case None           => println("No CodeTabs ")
    }
    println("")
  }

  def printTabsOfTabsPanel(pane: JTabbedPane): Unit = {
    for (n <- 0 until pane.getTabCount) {
      App.printSwingObject(pane.getComponentAt(n), "")
    }
  }

  // Input: origTabIndx - index a tab would have if there were no separate code tab.
  // Returns (tabOwner, tabComponent)
  // tabOwner = the AbstractTabs containing the indexed tab.
  // tabComponent = the tab in tabOwner referenced by origTabIndx.
  @throws (classOf[IndexOutOfBoundsException])
  def getTabComponentPlus(origTabIndx: Int): (AbstractTabs, Component) = {
    val (tabOwner, tabIndex) = computeIndexPlus(origTabIndx)
    val tabComponent = tabOwner.getComponentAt(tabIndex)
    (tabOwner, tabComponent)
  }

  def switchToTabsCodeTab(): Unit = {
    // nothing to do if CodeTabsPanel does not exist

    codeTabsPanelOption match {
      case None                =>
      case Some(codeTabsPanel) => {
        for (n <- 0 until codeTabsPanel.getTabCount) {
          appTabsPanel.add(codeTabsPanel.getTitleAt(0), codeTabsPanel.getComponentAt(0))
        }
        codeTabsPanel.getCodeTabContainer.dispose
        setCodeTabsPanelOption(None)
        appTabsPanel.codeTab.requestFocus
      // need to remove component, because will no longer exist
      // aab fix this appTabsPanel.getAppFrame.removeLinkComponent(actualCodeTabsPanel.getCodeTabContainer)
      } // end case where work was done
    }
  }

  def switchToSeparateCodeWindow(): Unit = {
    // Only act if code tab is part of the Tabs panel.
    // Otherwise it is already detached.
    if (!isCodeTabSeparate) {
      val codeTabsPanel = new CodeTabsPanel(appTabsPanel.workspace,
        appTabsPanel.interfaceTab,
        appTabsPanel.externalFileManager,
        appTabsPanel.codeTab,
        appTabsPanel.externalFileTabs)

        // aab maybe some of this should be in an init method shared with
        // CodeTabsPanel
        codeTabsPanelOption = Some(codeTabsPanel)
        addDeleteCodeTabButton(codeTabsPanel)
        codeTabsPanel.setTabManager(this)
        // iterate starting at last tab so that indexing remains valid when
        // tabs are removed (add to codeTabsPanel)
        //val startIndex:Int = appTabsPanel.getTabCount - 1
        for (n <- appTabsPanel.getTabCount - 1 to 0 by -1 ) {
          val tabComponent = appTabsPanel.getComponentAt(n)
          if (tabComponent.isInstanceOf[CodeTab]) {
            // Tabs are read in reverse order, use index 0 to restore original order
            codeTabsPanel.insertTab(appTabsPanel.getTitleAt(n),
             appTabsPanel.getIconAt(n),
             tabComponent,
             appTabsPanel.getToolTipTextAt(n),
             0)
          }
        }
        codeTabsPanel.setSelectedComponent(appTabsPanel.codeTab)
        appTabsPanel.setSelectedComponent(appTabsPanel.interfaceTab)
        appTabsPanel.getAppFrame.addLinkComponent(codeTabsPanel.getCodeTabContainer)
        setSeparateCodeTabBindings(codeTabsPanel)
        // add mouse listener, which should be not set when
        // there is no code tab
      }
  }

  def implementCodeTabSeparationState(isSeparate: Boolean): Unit = {
    if (isSeparate) {
      switchToSeparateCodeWindow
    } else {
      switchToTabsCodeTab
    }
  }

  def addComponentKeys(component: JComponent, key: Int, action: Action, actionName: String): Unit = {
    val inputMap: InputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    val actionMap: ActionMap = component.getActionMap();
    var mapKey = UserAction.KeyBindings.keystroke(key, withMenu = true, withAlt = false)
    inputMap.put(mapKey, actionName)
    actionMap.put(actionName, action)
    mapKey = UserAction.KeyBindings.keystroke(key, withMenu = true, withAlt = true)
    inputMap.put(mapKey, actionName)
    actionMap.put(actionName, action)
    mapKey = UserAction.KeyBindings.keystroke(key, withMenu = false, withAlt = true)
    inputMap.put(mapKey, actionName)
    actionMap.put(actionName, action)
  }

  def addCodeTabContainerKeys(codeTabsPanel: CodeTabsPanel, key: Int, action: Action, actionName: String): Unit = {
    val contentPane = codeTabsPanel.getCodeTabContainer.getContentPane.asInstanceOf[JComponent]
    addComponentKeys(contentPane, key, action, actionName)
  }

  def addAppFrameKeys(key: Int, action: Action, actionName: String): Unit = {
    val contentPane = appTabsPanel.getAppJFrame.getContentPane.asInstanceOf[JComponent]
    addComponentKeys(contentPane, key, action, actionName)
  }

  def setAppCodeTabBindings(): Unit = {
    addAppFrameKeys(KeyEvent.VK_9, KillSeparateCodeTab, "popInCodeTab")
    addAppFrameKeys(KeyEvent.VK_8, CreateSeparateCodeTab, "popOutCodeTab")
    addAppFrameKeys(KeyEvent.VK_CLOSE_BRACKET, KillSeparateCodeTab, "popInCodeTab")
    addAppFrameKeys(KeyEvent.VK_OPEN_BRACKET, CreateSeparateCodeTab, "popOutCodeTab")
  }

  def setSeparateCodeTabBindings(codeTabsPanel: CodeTabsPanel): Unit = {
    addCodeTabContainerKeys(codeTabsPanel, KeyEvent.VK_1, SwitchFocusAction1, "switchFocus1")
    addCodeTabContainerKeys(codeTabsPanel, KeyEvent.VK_2, SwitchFocusAction2, "switchFocus2")
    addCodeTabContainerKeys(codeTabsPanel, KeyEvent.VK_9, KillSeparateCodeTab, "popInCodeTab")
    addCodeTabContainerKeys(codeTabsPanel, KeyEvent.VK_CLOSE_BRACKET, KillSeparateCodeTab, "popInCodeTab")
  }

// these objects could also be private classes
  object SwitchFocusAction1 extends AbstractAction("Toggle1") {
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

  object SwitchFocusAction2 extends AbstractAction("Toggle2") {
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

  object KillSeparateCodeTab extends AbstractAction("PopCodeTabIn") {
    def actionPerformed(e: ActionEvent) {
      switchToTabsCodeTab
    }
  }

  object CreateSeparateCodeTab extends AbstractAction("PopCodeTabOut") {
    def actionPerformed(e: ActionEvent) {
      switchToSeparateCodeWindow
    }
  }

  object Empty extends AbstractAction("Empty") {
    def actionPerformed(e: ActionEvent) {
      // If index is already selected, unselect it
    }
  }

  def addDeleteCodeTabButton(codeTabsPanel: CodeTabsPanel ): Unit = {
    codeTabsPanel.getCodeTabContainer.getReattachPopOut.addActionListener(KillSeparateCodeTab)
  }
}
