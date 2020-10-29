// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Component
import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.{ Action, AbstractAction, ActionMap, InputMap, JComponent, JTabbedPane, KeyStroke }

import org.nlogo.app.codetab.{ CodeTab }
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

class AppTabManager( val appTabsPanel:          Tabs,
                     var codeTabsPanelOption:   Option[CodeTabsPanel]) {

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

  def isCodeTabSeparate = { codeTabsPanelOption.isDefined }

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

  // Sum of the number of App Tabs and Code Tabs, regardless of
  // where they are contained.
  // The word Combined in a method name generally indicates that Tabs entity
  // and possible CodeTabsPanel entity are being dealt with in a combined way,
  // that is not visible to the code user. AAB 10/2020
  def getCombinedTabCount(): Int = {
    val appTabCount = appTabsPanel.getTabCount
    codeTabsPanelOption match {
      case None           => appTabCount
      case Some(codeTabsPanel) => appTabCount + codeTabsPanel.getTabCount
    }
  }

  private var currentTab: Component = { appTabsPanel.interfaceTab }

  def getCurrentTab(): Component = {
    currentTab
  }

  def setCurrentTab(tab: Component): Unit = {
    currentTab = tab
  }

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
        case None           => throw new IndexOutOfBoundsException
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
        case None           =>
      }
    }
    (null.asInstanceOf[AbstractTabsPanel], -1)
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
        // Move the tabs to the AppTabsPanel (Tabs), retaining order. AAB 10/2020
        for (_ <- 0 until codeTabsPanel.getTabCount) {
          appTabsPanel.add(codeTabsPanel.getTitleAt(0), codeTabsPanel.getComponentAt(0))
        }
        codeTabsPanel.getCodeTabContainer.dispose
        setCodeTabsPanelOption(None)
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

      // Add keystrokes for actions from menus to the codeTabsPanel. AAB 10/2020
      copyAppMenuBarAccelerators

      appTabsPanel.mainCodeTab.getPoppingCheckBox.setSelected(true)
      codeTabsPanel.setSelectedComponent(appTabsPanel.mainCodeTab)
      appTabsPanel.setSelectedComponent(appTabsPanel.interfaceTab)
      appTabsPanel.getAppFrame.addLinkComponent(codeTabsPanel.getCodeTabContainer)
      setSeparateCodeTabBindings(codeTabsPanel)
      Event.rehash()
    }
  }

  // Copy Accelerators from the Application Menu Bar to the code tab getCodeTabContainer
  def copyAppMenuBarAccelerators(): Unit = {
    codeTabsPanelOption match {
      case None                => // nothing to do
      case Some(codeTabsPanel) => {
        copyMenuBarAccelerators(appTabsPanel.getAppJFrame.getJMenuBar, codeTabsPanel)
      }
    }
  }

  // For a MenuBar - copy Accelerators
  def copyMenuBarAccelerators(menuBar: javax.swing.JMenuBar,  codeTabsPanel: CodeTabsPanel): Unit = {
    for (i <- 0 until menuBar.getMenuCount) {
      val  item = menuBar.getMenu(i)
      if (item != null) {
        copyMenuAccelerators(item, codeTabsPanel)
      }
    }
  }

  // For a Menu - copy Menu Items Accelerators
  def copyMenuAccelerators(menu: javax.swing.JMenu, codeTabsPanel: CodeTabsPanel): Unit = {
    for (i <- 0 until menu.getItemCount) {
      val  item = menu.getItem(i)
      if (item != null && item.getAccelerator != null) {
        addCodeTabContainerKeyStroke(codeTabsPanel, item.getAccelerator, item.getAction, item.getActionCommand)
      }
      if (item.isInstanceOf[javax.swing.JMenu]) {
        copyMenuAccelerators(item.asInstanceOf[javax.swing.JMenu], codeTabsPanel)
      }
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

  // The following methods with the prefix "__" may be useful for debugging.

  // Prints list of tabs in App Window and Separate Code Window (If any.)
  def __printAllTabs(): Unit = {
    println("\nAppTabsPanel count " + appTabsPanel.getTabCount)
    __printTabsOfTabsPanel(appTabsPanel)
    codeTabsPanelOption match {
      case Some(codeTabsPanel) => {
        println("CodeTabs count " + codeTabsPanel.getTabCount)
        __printTabsOfTabsPanel(codeTabsPanel)
      }
      case None           => println("No CodeTabs ")
    }
    println("")
  }

  // Prints list of tabs in a JTabbedPane
  def __printTabsOfTabsPanel(pane: JTabbedPane): Unit = {
    for (n <- 0 until pane.getTabCount) {
      App.__printSwingObject(pane.getComponentAt(n), "")
    }
  }

  // Print the names of all the current TabsMenu Actions
  def __printTabsMenuActions():Unit = {
    println("Actions:")
    org.nlogo.app.TabsMenu.tabActions(this).foreach(action => {
      action.asInstanceOf[org.nlogo.swing.UserAction.MenuAction].accelerator match {
        case None                =>
        case Some(accKey: javax.swing.KeyStroke) =>  {
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
    val contentPane = appTabsPanel.getAppJFrame.getContentPane.asInstanceOf[JComponent]
    __printInputMap(contentPane)
  }

  // Prints InputMap for Separate Code Window (If any.)
  def __printSeparateCodeFrameInputMap(): Unit = {
    codeTabsPanelOption match {
      case None           => println("No Separate Code Window.")
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
    val contentPane = appTabsPanel.getAppJFrame.getContentPane.asInstanceOf[JComponent]
    __printInputActionMaps(contentPane)
  }

  // Prints InputMap and ActionMap for Separate Code Window (If any.)
  def __printSeparateCodeFrameInputActionMaps(): Unit = {
    codeTabsPanelOption match {
      case None           => println("No Separate Code Window.")
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
  def __printMenuItems(menu: javax.swing.JMenu, level: Int): Unit = {
    for (i <- 0 until menu.getItemCount) {
      val  item = menu.getItem(i)
      if (item != null) {
        println(indent(level * 2) + item.getText)
        val accelerator = item.getAccelerator
        if (accelerator != null) {
          println(indent(level * 4) + "Accelerator: " + accelerator);
        }
        if (item.isInstanceOf[javax.swing.JMenu]) {
          __printMenuItems(item.asInstanceOf[javax.swing.JMenu], level + 1);
        }
      } else {
        println(indent(level * 2) + "Separater");
      }
    }
  }

  // Creates a string consisting of the specified number of spaces
  def indent(n: Int): String =
    List.fill(n)(' ').mkString

  // For a MenuBar - Prints Menus
  def __printMenuBar(menuBar: javax.swing.JMenuBar): Unit = {
    for (i <- 0 until menuBar.getMenuCount) {
      val  item = menuBar.getMenu(i)
      if (item != null) {
        println(item.getText() + " Menu");
        __printMenuItems(item, 1);
      } else {
        println("null Item");
      }
    }
  }

  // Prints the Application Menu Bar
  def __printAppMenuBar(): Unit = {
    val menuBar = appTabsPanel.getAppJFrame.getJMenuBar
    App.__printSwingObject(menuBar, "menu bar")
    __printMenuBar(menuBar)
  }

  // Print Accelerators from the Application Menu Bar
  def __printAppMenuBarAccelerators(): Unit = {
    __printMenuBarAccelerators(appTabsPanel.getAppJFrame.getJMenuBar)
  }

  // For a MenuBar - print Accelerators
  def __printMenuBarAccelerators(menuBar: javax.swing.JMenuBar): Unit = {
    for (i <- 0 until menuBar.getMenuCount) {
      val  item = menuBar.getMenu(i)
      if (item != null) {
        __printMenuAccelerators(item)
      }
    }
  }

  // For a Menu - print Menu Items Accelerators
  def __printMenuAccelerators(menu: javax.swing.JMenu): Unit = {
    for (i <- 0 until menu.getItemCount) {
      val  item = menu.getItem(i)
      if (item != null && item.getAccelerator != null) {
        println(item.getActionCommand + ": " + item.getAccelerator)
      }
      if (item.isInstanceOf[javax.swing.JMenu]) {
        __printMenuAccelerators(item.asInstanceOf[javax.swing.JMenu])
      }
    }
  }
}
