// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ Action, AbstractAction, ActionMap, InputMap, JComponent, JMenu, JMenuItem, JTabbedPane, KeyStroke }

import org.nlogo.app.codetab.{ CodeTab }
import org.nlogo.ide.{ JumpToDeclarationAction, ShiftActions, ShowUsageBox, ShowUsageBoxAction, ToggleComments }
import org.nlogo.swing.UserAction
import org.nlogo.window.Event

// The class AppTabManager handles relationships between tabs (the
// InterfaceTab, the InfoTab, the MainCodeTab and the included files tabs )
// and their containing classes - Tabs and CodeTabsPanel.
// When the MainCodeTab is in a separate window the CodeTabsPanel exists
// and contains all the CodeTabs, while the other tabs belong to Tabs
// (Tabs and CodeTabsPanel both being JTabbedPanes).
// Otherwise all the tabs belong to Tabs.
// AppTabManager uses the variable codeTabsPanelOption of type Option[CodeTabsPanel]
// to deal with the fact that CodeTabsPanel is instatiated only some of the time.
// AAB 10/2020

// In order to support the option of a separate code window, NetLogo internal
// code enforces constraints on tab order.
// Users should manipulate tabs they choose to add (in extensions for example)
// using only the publically available methods in the last section of AbstractTabsPanel. AAB 10/2020

// Understanding the constraints on tab order is useful for understanding the
// code controlling tabs.
// When there is no separate code window, all tabs belong to Tabs
// (the value of appTabsPanel in AppTabManager)
// The tab order is non-CodeTabs followed by CodeTabs.
// The non-CodeTab order is InterfaceTab, InfoTab, any user-created non-CodeTabs.
// The MainCodeTab is always the first CodeTab, the other CodeTabs are the
// TemporaryCodeTab which is used for included files, and the
// ExtendedCodeTab which is for use outside the NetLogo code base.
//
// When a separate code window exists, all the CodeTabs belong to the CodeTabsPanel.
// They retain the same relative order they would if they all belonged to Tabs.
//
// The terms CombinedIndex or combinedTabIndex refers to the index a tab would have
// if there were no separate code window. AAB 10/2020

class AppTabManager(val appTabsPanel:          Tabs,
                    var codeTabsPanelOption:   Option[CodeTabsPanel]) {

  var menuBar: MenuBar = null

  var dirtyMonitor: DirtyMonitor = null

  // The appTabsPanel and the main code tab are unique unchanging entities
  // of class Tabs and MainCodeTab respectively. AAB 10/2020
  def getAppTabsPanel = { appTabsPanel }
  def getMainCodeTab = { appTabsPanel.getMainCodeTab }

  // The separate window and JTabbedPane containing the main code tab and
  // other code tabs can come in an out of existence, and are hence
  // represented by a scala Option, which has value 'None' when code tabs
  // are in the Application window and JTabbedPane. AAB 10/2020
  def setCodeTabsPanelOption(_codeTabsPanelOption: Option[CodeTabsPanel]): Unit = {
    codeTabsPanelOption = _codeTabsPanelOption
  }

  // might want access to these owner methods to be only in the app package
  // Need to carefully decide which methods are private. AAB 10/2020
  def getCodeTabsOwner() : AbstractTabsPanel = {
    codeTabsPanelOption match {
      case None           => appTabsPanel
      case Some(theValue) => theValue
    }
  }

  def getAppTabsOwner = { appTabsPanel }

  def getTabOwner(tab: Component): AbstractTabsPanel = {
    if (tab.isInstanceOf[CodeTab]) getCodeTabsOwner else appTabsPanel
  }

  def isCodeTabSeparate = { codeTabsPanelOption.isDefined }

  def getAppMenuBar: MenuBar = { menuBar }

  def setAppMenuBar(menuBar: MenuBar): Unit = { this.menuBar = menuBar }

  def getDirtyMonitor: DirtyMonitor = { dirtyMonitor }

  def setDirtyMonitor(dirtyMonitor: DirtyMonitor): Unit = { this.dirtyMonitor = dirtyMonitor }

  val appContentPane = appTabsPanel.getAppJFrame.getContentPane.asInstanceOf[JComponent]

  def setDirtyMonitorCodeWindow(): Unit = {
    codeTabsPanelOption match {
      case None                => dirtyMonitor.setCodeWindow(None)
      case Some(codeTabsPanel) => dirtyMonitor.setCodeWindow(Some(codeTabsPanel.getCodeTabContainer))
    }
  }

  // Generally the term "CodeTab" in method names refers to
  // any that is of class CodeTab. The term "MainCodeTab"
  // is usually used refer more specifically to the unique main code tab. AAB 10/2020
  def setSelectedCodeTab(tab: CodeTab): Unit = {
    getCodeTabsOwner.setSelectedComponent(tab)
  }

  // Generally the term "AppTab" in method names refers to
  // any tab in Tabs that is not of class CodeTab AAB 10/2020
  def setSelectedAppTab(index: Int): Unit = {
    appTabsPanel.setSelectedIndex(index)
  }

  def getSelectedAppTabIndex() = { appTabsPanel.getSelectedIndex }

  // Input: combinedTabIndex - index a tab would have if there were no separate code tab.
  // Returns (tabOwner, tabIndex)
  // tabOwner = the AbstractTabsPanel containing the indexed tab.
  // tabIndex = the index of the tab in tabOwner.
  // This method allows for the possibility that the appTabsPanel has no tabs,
  // although this should not occur in practice
  def ownerAndIndexFromCombinedIndex(combinedTabIndex: Int): (AbstractTabsPanel, Int) = {
    if (combinedTabIndex < 0) {
      throw new IndexOutOfBoundsException
    }
    val appTabCount = appTabsPanel.getTabCount

    // if the combinedTabIndex is too large for the appTabsPanel,
    // check if it can refer to the a separate code tab. AAB 10/2020
    if (combinedTabIndex >= appTabCount) {
      codeTabsPanelOption match {
        case None                => throw new IndexOutOfBoundsException
        case Some(codeTabsPanel) => {
          // combinedTabIndex could be too large for the two Panels combined. AAB 10/2020
          if (combinedTabIndex >= appTabCount + codeTabsPanel.getTabCount) {
            throw new IndexOutOfBoundsException
          }
          return(getCodeTabsOwner, combinedTabIndex - appTabCount)
        }
      }
    } else {
      return(appTabsPanel, combinedTabIndex)
    }
  }

  def combinedIndexFromOwnerAndIndex(tabOwner: AbstractTabsPanel, index: Int): Int = {
    if (tabOwner.isInstanceOf[CodeTabsPanel]) {
      return(index + appTabsPanel.getTabCount)
    } else {
      return(index)
    }
  }

  // Input: tab - a tab component
  // Returns (tabOwner, tabIndex)
  // where tabOwner is the AbstractTabsPanel containing the specified component
  // tabIndex = the index of the tab in tabOwner.
  // Returns (null, -1) if there is no tab owner for this tab component.
  def ownerAndIndexOfTab(tab: Component): (AbstractTabsPanel, Int) = {
    val tabIndex = appTabsPanel.indexOfComponent(tab)
    if (tabIndex != -1) {
      return(appTabsPanel, tabIndex)
    } else {
      codeTabsPanelOption match {
        case Some(codeTabsPanel) =>
          val aTabIndex = codeTabsPanel.indexOfComponent(tab)
          if (aTabIndex != -1) {
            return(codeTabsPanel, aTabIndex)
          }
        case None                =>
      }
    }
    (null.asInstanceOf[AbstractTabsPanel], -1)
  }

  def getTitleAtCombinedIndex(index: Int): String =  {
    val (tabOwner, tabIndex) = ownerAndIndexFromCombinedIndex(index)
    tabOwner.getTitleAt(tabIndex)
  }

  def getComponentAtCombinedIndex(index: Int): Component =  {
    val (tabOwner, tabIndex) = ownerAndIndexFromCombinedIndex(index)
    tabOwner.getComponentAt(tabIndex)
  }

  def setComponentAtCombinedIndex(index: Int, tab: Component): Unit = {
    val (tabOwner, tabIndex) = ownerAndIndexFromCombinedIndex(index)
    tabOwner.setComponentAt(tabIndex, tab)
  }

  def setPanelsSelectedIndex(index: Int): Unit =  {
    val (tabOwner, tabIndex) = ownerAndIndexFromCombinedIndex(index)
    setPanelsSelectedIndexHelper(tabOwner, tabIndex)
  }

  /**
    * Makes a tab component, specified by owner and index, selected or selectable, whether or not a separate code window exists.
    *
    * Usual effect: the specified component becomes the selected tab of the tabOwner.
    * An important alternative case occurs when a specified App Tab component is already selected.
    * This should only happen when focus is in the separate code tab window.
    * Swing will not allow the selection unless the Tab is first deselected. To deselect the tab without selecting
    * another tab, the selected tab index of the tabOwner must be set to -1.
    * The App Tab of interest is saved as the currentTab.
    * Documentation August 2021 - AAB
    *
    * @param tabOwner Owner of the Tab to be selected
    *
    * @param index    Index of the Tab to be selected
    */
  private def setPanelsSelectedIndexHelper(tabOwner: AbstractTabsPanel, tabIndex: Int): Unit = {
    val selectedIndex = tabOwner.getSelectedIndex
    if (selectedIndex == tabIndex) {
      // Saves selected tab as current tab
      tabOwner.setCurrentTab(tabOwner.getComponentAt(tabIndex))
      // Deselects the tab
      tabOwner.setSelectedIndex(-1)
    }
    tabOwner.setSelectedIndex(tabIndex)
    tabOwner.requestFocus
  }

  // Actions are created for use by the TabsMenu, and by accelerator keys AAB 10/2020
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

  // Does the work needed to go back to the no separate code window state
  def switchToNoSeparateCodeWindow(): Unit = {
    // nothing to do if CodeTabsPanel does not exist. AAB 10/2020

    codeTabsPanelOption match {
      case None                => // nothing to do
      case Some(codeTabsPanel) => {
        setCodeTabsPanelOption(None)
        // Move the tabs to the AppTabsPanel (Tabs), retaining order. AAB 10/2020
        for (_ <- 0 until codeTabsPanel.getTabCount) {
          appTabsPanel.add(codeTabsPanel.getTitleAt(0), codeTabsPanel.getComponentAt(0))
        }
        codeTabsPanel.getCodeTabContainer.dispose
        appTabsPanel.mainCodeTab.getPoppingCheckBox.setSelected(false)
        appTabsPanel.mainCodeTab.requestFocus
        appTabsPanel.getAppFrame.removeLinkComponent(codeTabsPanel.getCodeTabContainer)
        Event.rehash()
      } // end case where work was done. AAB 10/2020
    }
  }

  // Does the work needed to go back to the separate code window state
  def switchToSeparateCodeWindow(): Unit = {
    if (!isCodeTabSeparate) {
      val codeTabsPanel = new CodeTabsPanel(appTabsPanel.workspace,
        appTabsPanel.interfaceTab,
        appTabsPanel.externalFileManager,
        appTabsPanel.mainCodeTab,
        appTabsPanel.externalFileTabs)
      codeTabsPanelOption = Some(codeTabsPanel)
      codeTabsPanel.setTabManager(this)


      // The Selected Component in the App Tabs Panel will later need to be set
      // to the Interface Tab if the the CodeTab is currently the Selected Component
      // because Swing will make the Info Tab the Selected Componen after the Code Tab
      // moves (since it will have the highest index).
      val selectInterfaceTab = appTabsPanel.getSelectedComponent == appTabsPanel.mainCodeTab

      // Move tabs from appTabsPanel to codeTabsPanel.
      // Iterate starting at last tab so that indexing remains valid when
      // tabs are removed (add to codeTabsPanel), AAB 10/2020
      for (n <- appTabsPanel.getTabCount - 1 to 0 by -1 ) {
        val tabComponent = appTabsPanel.getComponentAt(n)
        if (tabComponent.isInstanceOf[CodeTab]) {
          // Tabs are read in reverse order, use index 0 to restore original order. AAB 10/2020
          codeTabsPanel.insertTab(appTabsPanel.getTitleAt(n),
           appTabsPanel.getIconAt(n),
           tabComponent,
           appTabsPanel.getToolTipTextAt(n),
           0)
        }
      }

      appTabsPanel.mainCodeTab.getPoppingCheckBox.setSelected(true)
      codeTabsPanel.setSelectedComponent(appTabsPanel.mainCodeTab)
      if (selectInterfaceTab) {
        appTabsPanel.setSelectedComponent(appTabsPanel.interfaceTab)
      }
      appTabsPanel.getAppFrame.addLinkComponent(codeTabsPanel.getCodeTabContainer)
      createCodeTabAccelerators()
      Event.rehash()
      codeTabsPanel.mainCodeTab.requestFocus
    }
  }

  // The separate code tab needs its own accelerators, plus ones from the existing menus
  def createCodeTabAccelerators(): Unit = {
    codeTabsPanelOption match {
      case None                =>
      case Some(codeTabsPanel) => {
        setSeparateCodeTabBindings()
        // Add keystrokes for actions from existing menus to the codeTabsPanel. AAB 10/2020
        copyMenuBarAccelerators()

        // Remove the command center related accelerators
        val contentPane = codeTabsPanel.getCodeTabContainer.getContentPane.asInstanceOf[JComponent]
        val slash = intKeyToMenuKeystroke(KeyEvent.VK_SLASH)
        val capC = intKeyToMenuKeystroke(KeyEvent.VK_C, withShift = true)
        removeComponentKeyStroke(contentPane, slash)
        removeComponentKeyStroke(contentPane, capC)
      }
    }
  }

  // For a MenuBar - copy Accelerators
  def copyMenuBarAccelerators(): Unit = {
    codeTabsPanelOption match {
      case None                =>
      case Some(codeTabsPanel) => {
        for (i <- 0 until getAppMenuBar.getMenuCount) {
          val  item = getAppMenuBar.getMenu(i)
          // getMenu returns null for separators
          if (item != null) {
            copyMenuAccelerators(item)
          }
        }
      }
    }
  }

  def copyMenuAcceleratorsByName(menuName: String): Unit = {
    getMenuByName(menuName) match {
      case None       => throw new Exception("No menu named " + menuName + "exists")
      case Some(menu) => copyMenuAccelerators(menu)
    }
  }

  // For a Menu - copy Menu Items Accelerators
  def copyMenuAccelerators(menu: JMenu): Unit = {
    require(menu != null)
    codeTabsPanelOption match {
      case None                =>
      case Some(codeTabsPanel) => {
        for (i <- 0 until menu.getItemCount) {
          val  item = menu.getItem(i)
          // getItem returns null for non-menu items such as separators
          // getAccelerator returns null when there is no accelerator
          if (item != null && item.getAccelerator != null) {
            addCodeTabContainerKeyStroke(item.getAccelerator, item.getAction, item.getActionCommand)
          }
          if (item.isInstanceOf[JMenu]) {
            copyMenuAccelerators(item.asInstanceOf[JMenu])
          }
        }
      }
    }
  }

  def addComponentKeyStroke(component: JComponent, mapKey: KeyStroke, action: Action, actionName: String): Unit = {
    val inputMap: InputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    val actionMap: ActionMap = component.getActionMap();
    inputMap.put(mapKey, actionName)
    actionMap.put(actionName, action)
  }

  // removes a mapKey from both the inputMap and actionMap of a component
  def removeComponentKeyStroke(component: JComponent, mapKey: KeyStroke): Unit = {
    val inputMap: InputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    val actionMap: ActionMap = component.getActionMap();
    val actionName = inputMap.get(mapKey)
    inputMap.remove(mapKey)

    if (actionName != null) {
      actionMap.remove(actionName)
    }
  }

  def removeCodeTabContainerAccelerators(): Unit = {
    codeTabsPanelOption match {
      case None                =>
      case Some(codeTabsPanel) => {
        val contentPane = codeTabsPanel.getCodeTabContainer.getContentPane.asInstanceOf[JComponent]
        val inputMap: InputMap = contentPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        val actionMap: ActionMap = contentPane.getActionMap();
        inputMap.clear
        actionMap.clear
      }
    }
  }

  def addCodeTabContainerKeyStroke(mapKey: KeyStroke, action: Action, actionName: String): Unit = {
    codeTabsPanelOption match {
      case None                =>
      case Some(codeTabsPanel) => {
        val contentPane = codeTabsPanel.getCodeTabContainer.getContentPane.asInstanceOf[JComponent]
        addComponentKeyStroke(contentPane, mapKey, action, actionName)
      }
    }
  }

  def addAppFrameKeyStroke(mapKey: KeyStroke, action: Action, actionName: String): Unit = {
    addComponentKeyStroke(appContentPane, mapKey, action, actionName)
  }

  def intKeyToMenuKeystroke(key: Int, withShift: Boolean = false, withAlt: Boolean = false): KeyStroke = {
    UserAction.KeyBindings.keystroke(key, withMenu = true, withShift = withShift, withAlt = withAlt)
  }

  def setAppCodeTabBindings(): Unit = {
    addAppFrameKeyStroke(intKeyToMenuKeystroke(KeyEvent.VK_W, withShift = true), SeparateCodeTabsAction, "popOutCodeTab")
  }

  def setSeparateCodeTabBindings(): Unit = {
    // These code tab specific Edit menu hotkeys are needed
    // Comment / Uncomment: meta pressed SEMICOLON
    // Shift Left: meta pressed OPEN_BRACKET
    // Shift Right: meta pressed CLOSE_BRACKET
    // Show Usage: meta pressed U
    // Jump To Declaration: meta pressed E
    addCodeTabContainerKeyStroke(intKeyToMenuKeystroke(KeyEvent.VK_SEMICOLON), new ToggleComments(), "CommentUncomment")
    addCodeTabContainerKeyStroke(intKeyToMenuKeystroke(KeyEvent.VK_OPEN_BRACKET), new ShiftActions.Left(), "ShiftLeft")
    addCodeTabContainerKeyStroke(intKeyToMenuKeystroke(KeyEvent.VK_CLOSE_BRACKET), new ShiftActions.Right(), "ShiftRight")
    val showUsageBox = new ShowUsageBox(getMainCodeTab.editorFactory.colorizer)
    addCodeTabContainerKeyStroke(intKeyToMenuKeystroke(KeyEvent.VK_U), new ShowUsageBoxAction(showUsageBox), "ShowUsage")
    addCodeTabContainerKeyStroke(intKeyToMenuKeystroke(KeyEvent.VK_E), new JumpToDeclarationAction(), "JumpToDeclaration")

    // Close the separate code tab: meta pressed W
    addCodeTabContainerKeyStroke(intKeyToMenuKeystroke(KeyEvent.VK_W), RejoinCodeTabsAction, "popInCodeTab")
  }

  // Get Named App Menu
  def getMenuByName(menuName: String): Option[JMenu] = {
    if (getAppMenuBar == null) {
      return None
    }
    for (i <- 0 until getAppMenuBar.getMenuCount) {
      val  item = getAppMenuBar.getMenu(i)
      if (item != null) {
        if (item.getText() == menuName) {
          return Some(item)
        }
      }
    }
    None
  }

  // *** Begin official tab manipulation methods ***
  // Code outside the org.nlogo.app and org.nlogo.app.codetab packages must
  // use these methods when manipulating the tabs in the main NetLogo application window
  // - the Interface and  Info tabs, and non-code tabs added by extensions, or tabs that may belong
  // to either the  main NetLogo application window or to a separate Code tab window -
  // - the Code tab and included file tabs , and code tabs added by extensions
  // (for example LevelSpace).
  // Other methods may become protected in the future, and code making
  // use of tab indices may fail now or in the future. AAB 10/2020

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
      val codeTabsOwner = getCodeTabsOwner.asInstanceOf[JTabbedPane]
      if (tab.isInstanceOf[CodeTab]) {
        // If it is a code tab, it goes at the end of JTabbedPane that owns CodeTabs.
        // It becomes the last menu item. AAB 10/2020
        codeTabsOwner.insertTab(title, icon, tab, tip, codeTabsOwner.getTabCount)
        appTabsPanel.addMenuItem(getTotalTabCount - 1, title)
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
    require (tab != null)
    val (tabOwner, _) = ownerAndIndexOfTab(tab)
    if (tabOwner != null) {
      tabOwner.remove(tab)
      appTabsPanel.updateTabsMenu()
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
    * @throws Exception if one Tab is a CodeTab and the other is not.
    */
  def replaceTab(oldTab: Component, newTab: Component): Unit = {
    require(oldTab != null && newTab != null)

    if (oldTab.isInstanceOf[CodeTab] && !oldTab.isInstanceOf[CodeTab]) {
      throw new Exception("A CodeTab must be replaced by a CodeTab")
    }

    if (!oldTab.isInstanceOf[CodeTab] && oldTab.isInstanceOf[CodeTab]) {
      throw new Exception("A non-CodeTab must be replaced by a non-CodeTab")
    }

    val (tabOwner, tabIndex) = ownerAndIndexOfTab(oldTab)
    if (tabOwner != null) {
      tabOwner.setComponentAt(tabIndex, newTab)
      appTabsPanel.updateTabsMenu
    } else {
      throw new Exception("The old code tab does not belong to a Tabs Panel")
    }
  }

  /**
    * Return the number of tabs in the Application Window JTabbedPane plus
    * those in the separate code window (if it exists).
    */
  def getTotalTabCount(): Int = {
    val appTabCount = appTabsPanel.getTabCount
    codeTabsPanelOption match {
      case None                => appTabCount
      case Some(codeTabsPanel) => appTabCount + codeTabsPanel.getTabCount
    }
  }

  /**
    * Makes a tab component selected or selectable, whether or not a separate code window exists.
    *
    * Usual effect: the specified component becomes the selected tab of the tabOwner.
    * An important alternative case occurs when a specified App Tab component is already selected.
    * This should only happen when focus is in the separate code tab window.
    * Swing will not allow the selection unless the Tab is first deselected. To deselect the tab without selecting
    * another tab, the selected tab index of the tabOwner must be set to -1.
    * The App Tab of interest is saved as the currentTab.
    * Documentation update August 2021 - AAB
    *
    * @param tab the Component to be selected
    */
  def setPanelsSelectedComponent(tab: Component): Unit = {
    require(tab != null)
    val (tabOwner, tabIndex) = ownerAndIndexOfTab(tab)
    setPanelsSelectedIndexHelper(tabOwner, tabIndex)
  }

  /**
   * Gets selected non-code tab if any , whether or not separate code window exists.
   */
  def getSelectedNonCodeTabComponent(): Option[Component] = {
    val index = appTabsPanel.getSelectedIndex
    if (index == -1) {
      return None
    }
    val tab = appTabsPanel.getComponentAt(index)
    if (tab.isInstanceOf[CodeTab]) {
      return None
    }
    return Some(tab)
  }

  /**
   * Gets selected code tab if any , whether or not separate code window exists.
   */
   def getSelectedCodeTabComponent(): Option[Component] = {
     codeTabsPanelOption match {
       case None                => {
         val index = appTabsPanel.getSelectedIndex
         if (index == -1) {
           return None
         }
         val tab = appTabsPanel.getComponentAt(index)
         if (tab.isInstanceOf[CodeTab]) {
           return Some(tab)
         } else {
           return None
         }
       }
       case Some(codeTabsPanel) => {
         val index = codeTabsPanel.getSelectedIndex
         if (index == -1) {
           return None
         }
         val tab = codeTabsPanel.getComponentAt(index)
         // All tabs in codeTabsPanel are code tabs 11/2020 AAB
          return Some(tab)
       }
     }
   }

// *** End official tab manipulation methods *** AAB 10/2020.

  // *** Begin debugging tools.
  // they begin with the prefix "__" AAB 10/2020.

  // Prints list of tabs in App Window and Separate Code Window (If any.)
  def __printAllTabs(): Unit = {
    println("\nAppTabsPanel count " + appTabsPanel.getTabCount)
    __printTabsOfTabsPanel(appTabsPanel)
    codeTabsPanelOption match {
      case Some(codeTabsPanel) => {
        println("CodeTabs count " + codeTabsPanel.getTabCount)
        __printTabsOfTabsPanel(codeTabsPanel)
      }
      case None                => println("No CodeTabs ")
    }
    println("")
  }

  // Prints list of tabs in a JTabbedPane
  def __printTabsOfTabsPanel(pane: JTabbedPane): Unit = {
    if (pane != null) {
      for (n <- 0 until pane.getTabCount) {
        __printSwingObject(pane.getComponentAt(n), "")
      }
    }
  }

  // Print the names of all the current TabsMenu Actions
  def __printTabsMenuActions():Unit = {
    println("Actions:")
    org.nlogo.app.TabsMenu.tabActions(this).foreach(action => {
      action.asInstanceOf[org.nlogo.swing.UserAction.MenuAction].accelerator match {
        case None                    =>
        case Some(accKey: KeyStroke) =>  {
          val actionName = action.getValue(javax.swing.Action.NAME) match {
            case s: String => s
            case _         => accKey.toString
          }
          println("  " + actionName + ": " + accKey)
        }
      }
    })
  }

  // Prints InputMap for App Window
  def __printAppFrameInputMap(): Unit = {
    __printInputMap(appContentPane)
  }

  // Prints InputMap for Separate Code Window (If any.)
  def __printSeparateCodeFrameInputMap(): Unit = {
    codeTabsPanelOption match {
      case None                => println("No Separate Code Window.")
      case Some(codeTabsPanel) => {
        val contentPane = codeTabsPanel.getCodeTabContainer.getContentPane.asInstanceOf[JComponent]
        __printInputMap(contentPane)
      }
    }
  }

  // Prints InputMap for specified JComponent.
  def __printInputMap(component: JComponent): Unit = {
    val inputMap: InputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    println("Input Map")
    for (key <- inputMap.allKeys) {
      println(key + ": " + inputMap.get(key))
    }
    println(" ")
  }

  // Prints InputMap and ActionMap for App Window
  def __printAppFrameInputActionMaps(): Unit = {
    __printInputActionMaps(appContentPane)
  }

  // Prints InputMap and ActionMap for Separate Code Window (If any.)
  def __printSeparateCodeFrameInputActionMaps(): Unit = {
    codeTabsPanelOption match {
      case None                => println("No Separate Code Window.")
      case Some(codeTabsPanel) => {
        val contentPane = codeTabsPanel.getCodeTabContainer.getContentPane.asInstanceOf[JComponent]
        __printInputActionMaps(contentPane)
      }
    }
  }

  // Prints InputMap and ActionMap for specified JComponent.
  def __printInputActionMaps(component: JComponent): Unit = {
    val inputMap: InputMap = component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    val actionMap: ActionMap = component.getActionMap();
    println("Input Map")
    for (key <- inputMap.allKeys) {
      println(key + ": " + inputMap.get(key))
    }
    println(" ")
    println("Action Map")
    for (key <- actionMap.allKeys) {
      println(key + ": " + actionMap.get(key))
    }
    println(" ")
    println(" ")
  }

  // For a Menu - prints Menu Items and their Accelerators
  def __printMenuItems(menu: JMenu, level: Int): Unit = {
    if (menu == null) { return }
    for (i <- 0 until menu.getItemCount) {
      val  item = menu.getItem(i)
      if (item != null) {
        println(indent(level * 2) + item.getText)
        val accelerator = item.getAccelerator
        if (accelerator != null) {
          println(indent(level * 4) + "Accelerator: " + accelerator);
        }
        if (item.isInstanceOf[JMenu]) {
          __printMenuItems(item.asInstanceOf[JMenu], level + 1);
        }
      } else {
        println(indent(level * 2) + "Separator");
      }
    }
  }

  // For a Menu Item - prints Menu Item its Accelerator or
  // if it is a JMenu prints Menu Items and their Accelerators
  def __printMenuItem(menuItem: JMenuItem, level: Int): Unit = {
    if (menuItem == null) { return }
    if (menuItem.isInstanceOf[JMenu]) {
      __printMenuItems(menuItem.asInstanceOf[JMenu], 1);
    } else {
      println(menuItem.getText)
      val accelerator = menuItem.getAccelerator
      if (accelerator != null) {
        println(indent(level * 2) + "Accelerator: " + accelerator);
      }
    }
  }

  // Get Named Item from Menu
  def __getMenuItembyName(menu: JMenu, menuItemName: String): Option[JMenuItem] = {
    if (menu == null) {
      return None
    }
    for (i <- 0 until menu.getItemCount) {
      val  item = menu.getItem(i)
      if (item != null) {
        if (item.getText() == menuItemName) {
          return Some(item)
        }
      }
    }
    None
  }

  // Get Named Item from  named Menu
  def __getMenuItembyNameAndMenuName(menuName: String, menuItemName: String): Option[JMenuItem] = {
    getMenuByName(menuName)  match {
      case None       => return(None)
      case Some(menu) =>  {
        return __getMenuItembyName(menu, menuItemName)
      }
    }
  }

  def __printMenuItembyNameAndMenuName(menuName: String, menuItemName: String): Unit = {
    __getMenuItembyNameAndMenuName(menuName, menuItemName) match {
      case None           => println("Menu Item '" + menuName + ":" + menuItemName + "' does not exist.")
      case Some(menuItem) => __printMenuItem(menuItem, 1)
    }
  }

  // Creates a string consisting of the specified number of spaces
  def indent(n: Int): String =
    List.fill(n)(' ').mkString

  // For App MenuBar - Prints Menus
  def __printAppMenuBar(): Unit = {
    __printSwingObject(getAppMenuBar, "menu bar")
    for (i <- 0 until getAppMenuBar.getMenuCount) {
      val  item = getAppMenuBar.getMenu(i)
      if (item != null) {
        println(item.getText() + " Menu")
        __printMenuItems(item, 1)
      } else {
        println("null Item")
      }
    }
  }

  // For App MenuBar - Prints Named Menu
  def __printAppMenuByName(menuName: String): Unit = {
    getMenuByName(menuName).fold(println(menuName + " Menu not found"))( {
      println(menuName + " Menu")
      __printMenuItems(_, 1)
    })
  }

  // For a MenuBar - Print Accelerators of Named Menu
  def __printAppMenuAcceleratorsByName(menuName: String): Unit = {
      getMenuByName(menuName)  match {
      case None       => println(menuName + " Menu not found")
      case Some(menu) =>  {
        println(menuName + " Menu")
        __printMenuAccelerators(menu)
      }
    }
  }

  // For App MenuBar - print Accelerators
  def __printAppMenuBarAccelerators(): Unit = {
    for (i <- 0 until getAppMenuBar.getMenuCount) {
      val  item = getAppMenuBar.getMenu(i)
      if (item != null) {
        __printMenuAccelerators(item)
      }
    }
  }

  // For a Menu - print Menu Items Accelerators
  def __printMenuAccelerators(menu: JMenu): Unit = {
    if (menu == null) { return }
    for (i <- 0 until menu.getItemCount) {
      val  item = menu.getItem(i)
      if (item != null && item.getAccelerator != null) {
        println(item.getActionCommand + ": " + item.getAccelerator)
      }
      if (item.isInstanceOf[JMenu]) {
        __printMenuAccelerators(item.asInstanceOf[JMenu])
      }
    }
  }

  def __getSimpleName(c: Any) : String = {
    if (c == null){
      return "<null>"
    } else {
      return c.getClass.getSimpleName
    }
  }

  def __printSwingObject(obj: java.awt.Component, description: String): Unit = {
    val some = Option(obj) // Because Option(null) = None 11/2020 AAB
    var objID = ""
    some match {
      case None           =>
      case Some(theValue) => objID = System.identityHashCode(obj) + ", "
    }
    println(description + " " + objID + __getSimpleName(obj))
  }

  def __printOptionSwingObject(obj: Option[java.awt.Component], description: String): Unit = {
    obj match {
      case None            => println(description + " None")
      case Some(theObject) =>  __printSwingObject(theObject, description)
    }
  }

  def __countMenuItembyNameAndMenuName(menuName: String, menuItemName: String): Int = {
    getMenuByName(menuName)  match {
      case None       => return 0
      case Some(menu) =>  {
        var itemCount = 0
        for (i <- 0 until menu.getItemCount) {
          val item = menu.getItem(i)
          if (item != null) {
            if (item.getText() == menuItemName) {
              itemCount = itemCount + 1
            }
          }
        }
        return itemCount
      }
    }
  }

  def __printWindowEventInfo(e: java.awt.event.WindowEvent): Unit = {
    println("    " + "ID: " + e.getID)
    println("    " + "source: " + __getSimpleName(e.getSource))
    println("    " + "window: " + __getSimpleName(e.getWindow))
    println("    " + "opposite window: " + __getSimpleName(e.getOppositeWindow))
  }

  def __printStateInfo(previousTab: Component, currentTab: Component): Unit = {
    println("    Previous Tab: " + __getSimpleName(previousTab))
    println("    Current Tab: " + __getSimpleName(currentTab))
    val owner = getCodeTabsOwner
    println("    CodeTabOwner " + __getSimpleName(owner) + " Selected Index: " + owner.getSelectedIndex)
  }

  def __printFocusOwner(topContainer: javax.swing.JFrame): Unit = {
    __printFocusOwner(topContainer, false)
  }

  def __printFocusOwner(topContainer: javax.swing.JFrame, fullInfo: Boolean): Unit = {
    if (fullInfo) {
      println("    " + __getSimpleName(topContainer) + " is Active " + topContainer.isActive())
      println("    " + __getSimpleName(topContainer) + " is Focused " + topContainer.isFocused())
    }
    if (topContainer.isFocused()) {
      val focusOwner = topContainer.getFocusOwner
      if (focusOwner != null) {
        println("    Focus owner is " + __getSimpleName(focusOwner))
      } else {
        println("    No focus owner.")
      }
    } else {
      val prevFocusOwner = topContainer.getMostRecentFocusOwner
      if (prevFocusOwner != null) {
        println("    Focus owner is " + __getSimpleName(prevFocusOwner))
      } else {
        println("    No previous focus owner.")
      }
    }
  }

  def __printHideUndoMenuCounts(): Unit = {
    println("    Hide count: " + __countMenuItembyNameAndMenuName("Tools", "Hide Command Center"))
    println("    Undo count: " + __countMenuItembyNameAndMenuName("Edit", "Undo"))
  }

  // Print some Events with their state variables

  def __printCompiledEvent(e: org.nlogo.window.Events.CompiledEvent, msg: String = ""): Unit = {
    println(msg + "CompiledEvent ")
    println(s"  sourceOwner = ${e.sourceOwner}")
    println(s"  program = ${e.program}")
    println(s"  procedure = ${e.procedure}")
    println(s"  error = ${e.error}")
  }

  def __printCompileMoreSourceEvent(e: org.nlogo.window.Events.CompileMoreSourceEvent, msg: String = ""): Unit = {
    println(msg + "CompileMoreSourceEvent:")
    println(s"  owner = ${e.owner}")
  }

  def __printDirtyEvent(e: org.nlogo.window.Events.DirtyEvent, msg: String = ""): Unit = {
    println(msg + "DirtyEvent:")
    println(s"  path = ${e.path}")
  }

  def __printExternalFileSavedEvent(e: org.nlogo.window.Events.ExternalFileSavedEvent, msg: String = ""): Unit = {
    println(msg + "ExternalFileSavedEvent:")
    println(s"  path = ${e.path}")
  }

  def __printRuntimeErrorEvent(e: org.nlogo.window.Events.RuntimeErrorEvent, msg: String = ""): Unit = {
    println(msg + "RuntimeErrorEvent:")
    println(s"  jobOwner = ${e.jobOwner}")
    println(s"  sourceOwner = ${e.sourceOwner}")
    println(s"  pos = ${e.pos}")
    println(s"  length = ${e.length}")
  }

  def __printSwitchedTabsEvent(e: org.nlogo.app.common.Events.SwitchedTabsEvent, msg: String = ""): Unit = {
    println(msg + "SwitchedTabsEvent:")
    println(s"  oldTab = ${e.oldTab}")
    println(s"  newTab = ${e.newTab}")
  }

  // *** End debugging tools AAB 10/2020.
}
