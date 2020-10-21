// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ Action, AbstractAction, ActionMap, InputMap, JComponent, JTabbedPane, KeyStroke }

import org.nlogo.app.codetab.{ CodeTab }
import org.nlogo.swing.{ TabsMenu, UserAction }, UserAction.MenuAction

// The class AppTabManager handles relationships between tabs (the
// InterfaceTab, the InfoTab, the MainCodeTab and the included files tabs )
// and their containing classes - Tabs and CodeTabsPanel.
// When the MainCodeTab is in a separate window the CodeTabsPanel exists
// and contains all the CodeTabs, while the other tabs belong to Tabs
// (Tabs and CodeTabsPanel both being JTabbedPanes).
// Otherwise all the tabs belong to Tabs.
// AppTabManager uses the variable codeTabsPanelOption of type Option[CodeTabsPanel]
// to deal with the fact that CodeTabsPanel is instatiated only some of the time.

class AppTabManager( val appTabsPanel:          Tabs,
                     var codeTabsPanelOption: Option[CodeTabsPanel]) {

  // The appTabsPanel and the main code tab are unique unchanging entities
  // of class Tabs and MainCodeTab respectively
  def getAppTabsPanel = { appTabsPanel }
  def getMainCodeTab = { appTabsPanel.getMainCodeTab }

  // The separate window and JTabbedPane containing the main code tab and
  // other code tabs can come in an out of existence, and are hence
  // represented by a scala Option, which has value 'None' when code tabs
  // are in the Application window and JTabbedPane
  def setCodeTabsPanelOption(_codeTabsPanelOption: Option[CodeTabsPanel]): Unit = {
    codeTabsPanelOption = _codeTabsPanelOption
  }

  // might want access to these owner methods to be only in the app package
  // Need to carefully decide which methods are private
  def getCodeTabsOwner = {
    codeTabsPanelOption match {
      case None           => appTabsPanel
      case Some(theValue) => theValue
    }
  }

  def getAppTabsOwner = { appTabsPanel }

  def getTabOwner(tab: Component): AbstractTabsPanel = {
    if (tab.isInstanceOf[CodeTab]) getCodeTabsOwner else appTabsPanel
  }

  def isCodeTabSeparate = {
    codeTabsPanelOption match {
      case None           => false
      case Some(theValue) => true
    }
  }

  // Generally the term "CodeTab" in method names refers to
  // any that is of class CodeTab. The term "MainCodeTab"
  // is usually used refer more specifically to the unique main code tab.
  def setSelectedCodeTab(tab: CodeTab): Unit = {
    getCodeTabsOwner.setSelectedComponent(tab)
  }

  // Generally the term "AppTab" in method names refers to
  // any tab in Tabs that is not of class CodeTab
  def setSelectedAppTab(index: Int): Unit = {
    appTabsPanel.setSelectedIndex(index)
  }

  def getSelectedAppTabComponent() = { appTabsPanel.getSelectedComponent }

  def getSelectedAppTabIndex() = { appTabsPanel.getSelectedIndex }

  // Sum of the number of App Tabs and Code Tabs, regardless of
  // where they are contained.
  // The word Combined in a method name generally indicates that Tabs entity
  // and possible CodeTabsPanel entity are being dealt with in a combined way,
  // that is not visible to the code user.
  def getCombinedTabCount(): Int = {
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
  // combinedTabIndx = the index of a tab in appTabsPanel when there is no codeTabsPanel
  // codeTabIndx = the index of a tab in codeTabsPanel (if it exists)
  // When the codetab is not detached
  // combinedTabIndx is an index in appTabsPanel
  // When the codetab is detached
  // for combinedTabIndx < nAppTabsPanel: combinedTabIndx is an index in appTabsPanel
  // combinedTabIndx >= nAppTabsPanel: codeTabIndx = combinedTabIndx - nAppTabsPanel is an index in codeTabsPanel

  // Input: combinedTabIndx - index a tab would have if there were no separate code tab.
  // Returns (tabOwner, tabIndex)
  // tabOwner = the AbstractTabsPanel containing the indexed tab.
  // tabIndex = the index of the tab in tabOwner.
  // This method allows for the possibility that the appTabsPanel has no tabs,
  // although this should not occur in practice
  def ownerAndIndexFromCombinedIndex(combinedTabIndx: Int): (AbstractTabsPanel, Int) = {
    if (combinedTabIndx < 0) {
      throw new IndexOutOfBoundsException
    }
    var tabOwner = appTabsPanel.asInstanceOf[AbstractTabsPanel]
    val appTabCount = appTabsPanel.getTabCount
    var tabIndex = combinedTabIndx

    // if the combinedTabIndx is too large for the appTabsPanel,
    // check if it can refer to the a separate code tab
    if (combinedTabIndx >= appTabCount) {
      codeTabsPanelOption match {
        case None           => throw new IndexOutOfBoundsException
        case Some(thePanel) => {
          // combinedTabIndx could be too large for the two Panels combined
          if (combinedTabIndx >= appTabCount + thePanel.getTabCount) {
            throw new IndexOutOfBoundsException
          }
          tabOwner = getCodeTabsOwner
          tabIndex =  combinedTabIndx - appTabCount
        }
      }
    }
    (tabOwner, tabIndex)
  }

  // Input: tab - a tab component
  // Returns (tabOwner, tabIndex)
  // where tabOwner is the AbstractTabsPanel containing the specified component
  // tabIndex = the index of the tab in tabOwner.
  // Returns (null, -1) if there is no tab owner for this tab component.
  def ownerAndIndexOfTab(tab: Component): (AbstractTabsPanel, Int) = {
    var tabOwner = null.asInstanceOf[AbstractTabsPanel]
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

  // Input: combinedTabIndx - index a tab would have if there were no separate code tab.
  // Returns (tabOwner, tabComponent)
  // tabOwner = the AbstractTabsPanel containing the indexed tab.
  // tabComponent = the tab in tabOwner referenced by combinedTabIndx.
  def getTabAtCombinedTabIndx(combinedTabIndx: Int): (AbstractTabsPanel, Component) = {
    val (tabOwner, tabIndex) = ownerAndIndexFromCombinedIndex(combinedTabIndx)
    val tabComponent = tabOwner.getComponentAt(tabIndex)
    (tabOwner, tabComponent)
  }

  object RejoinCodeTabsAction extends AbstractAction("PopCodeTabIn") {
    def actionPerformed(e: ActionEvent) {
      switchToNoSeparateCodeWindow
    }
  }

  object SeparateCodeTabsAction extends AbstractAction("PopCodeTabOut") {
    def actionPerformed(e: ActionEvent) {
      switchToSeparateCodeWindow
    }
  }

  def switchToSpecifiedCodeWindowState(isSeparate: Boolean): Unit = {
    if (isSeparate) {
      switchToSeparateCodeWindow
    } else {
      switchToNoSeparateCodeWindow
    }
  }

  def switchToNoSeparateCodeWindow(): Unit = {
    // nothing to do if CodeTabsPanel does not exist

    codeTabsPanelOption match {
      case None                =>
      case Some(codeTabsPanel) => {
        for (n <- 0 until codeTabsPanel.getTabCount) {
          appTabsPanel.add(codeTabsPanel.getTitleAt(0), codeTabsPanel.getComponentAt(0))
        }
        codeTabsPanel.getCodeTabContainer.dispose
        setCodeTabsPanelOption(None)
        appTabsPanel.mainCodeTab.getPoppingCheckBox.setSelected(false)
        appTabsPanel.mainCodeTab.requestFocus
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
        appTabsPanel.mainCodeTab,
        appTabsPanel.externalFileTabs)

        // aab maybe some of this should be in an init method shared with
        // CodeTabsPanel
        codeTabsPanelOption = Some(codeTabsPanel)
        codeTabsPanel.setTabManager(this)

        // Move tabs from appTabsPanel to codeTabsPanel.
        // Iterate starting at last tab so that indexing remains valid when
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

        // Might need to reorder the TabsMenu
        // aab add code here

        // Add keystrokes for actions from TabsMenu to the codeTabsPanel
        TabsMenu.tabActions(this).foreach(action => {
          // Add the accelerator key if any to the input map and action map
          action.asInstanceOf[MenuAction].accelerator match {
            case None                =>
            case Some(accKey: KeyStroke) =>  {
              val actionName = action.getValue(Action.NAME) match {
                case s: String => s
                case _         => accKey.toString
              }
              addCodeTabContainerKeyStroke(codeTabsPanel, accKey, action, actionName)
            }
          }
        })
        appTabsPanel.mainCodeTab.getPoppingCheckBox.setSelected(true)
        codeTabsPanel.setSelectedComponent(appTabsPanel.mainCodeTab)
        appTabsPanel.setSelectedComponent(appTabsPanel.interfaceTab)
        appTabsPanel.getAppFrame.addLinkComponent(codeTabsPanel.getCodeTabContainer)
        setSeparateCodeTabBindings(codeTabsPanel)
        // add mouse listener, which should be not set when
        // there is no code tab
      }
  }

  def addComponentKeyStroke(component: JComponent, mapKey: KeyStroke, action: Action, actionName: String): Unit = {
    val inputMap: InputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    val actionMap: ActionMap = component.getActionMap();
    inputMap.put(mapKey, actionName)
    actionMap.put(actionName, action)
  }

  def addCodeTabContainerKeyStroke(codeTabsPanel: CodeTabsPanel, mapKey: KeyStroke, action: Action, actionName: String): Unit = {
    val contentPane = codeTabsPanel.getCodeTabContainer.getContentPane.asInstanceOf[JComponent]
    addComponentKeyStroke(contentPane, mapKey, action, actionName)
  }

  def addAppFrameKeyStroke(mapKey: KeyStroke, action: Action, actionName: String): Unit = {
    val contentPane = appTabsPanel.getAppJFrame.getContentPane.asInstanceOf[JComponent]
    addComponentKeyStroke(contentPane, mapKey, action, actionName)
  }

  def intKeyToMenuKeystroke(key: Int): KeyStroke = {
    UserAction.KeyBindings.keystroke(key, withMenu = true, withAlt = false)
  }

  def setAppCodeTabBindings(): Unit = {
    addAppFrameKeyStroke(intKeyToMenuKeystroke(KeyEvent.VK_OPEN_BRACKET), SeparateCodeTabsAction, "popOutCodeTab")
  }

  def setSeparateCodeTabBindings(codeTabsPanel: CodeTabsPanel): Unit = {
    addCodeTabContainerKeyStroke(codeTabsPanel, intKeyToMenuKeystroke(KeyEvent.VK_CLOSE_BRACKET), RejoinCodeTabsAction, "popInCodeTab")
  }

}
