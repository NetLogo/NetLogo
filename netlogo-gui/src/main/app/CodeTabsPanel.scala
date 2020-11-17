// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.app

import java.awt.event.{ MouseAdapter, MouseEvent, WindowAdapter, WindowEvent }

import javax.swing.JTabbedPane
import javax.swing.event.{ ChangeEvent, ChangeListener }

import scala.collection.mutable

import org.nlogo.app.codetab.{ ExternalFileManager, MainCodeTab, TemporaryCodeTab }
import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.core.I18N
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.window.GUIWorkspace
import org.nlogo.window.Event
import org.nlogo.window.Event.LinkParent
import org.nlogo.window.Events._

// When a separate code tab window is created, an instance of this class owns the CodeTabs.
// When there is no separate code tab window, no such instance exists, and all
// CodeTabs belong to Tabs. [ Thinking of Tabs as AppTabsPanel makes the parallels between
// CodeTabsPanel and Tabs clearer - both are JTabbedPanes that contain and manage tabs.]
// CodeTabsPanel and Tabs are both instances of AbstractTabsPanel, which implements their shared behavior. AAB 10/2020

class CodeTabsPanel(workspace:             GUIWorkspace,
                    interfaceTab:          InterfaceTab,
                    externalFileManager:   ExternalFileManager,
                    val mainCodeTab:       MainCodeTab,
                    val externalFileTabs:  mutable.Set[TemporaryCodeTab])
  extends AbstractTabsPanel(workspace, interfaceTab, externalFileManager)
  with ChangeListener
  with AfterLoadEvent.Handler
  with LinkParent
  with org.nlogo.window.LinkRoot {

  locally {
    addChangeListener(this)
  }

  // frame is the App's AppFrame, (treated as a java.awt.Frame) AAB 10/2020
  val frame = workspace.getFrame

  // CodeTabContainer contains the CodeTabsPanel and is owned by frame
  // It is currently implemented as a JDialog. AAB 10/2020
  val codeTabContainer = new CodeTabContainer(frame, this)
  val codeTabsPanel = this

  def asAbstractTabsPanel = { this.asInstanceOf[AbstractTabsPanel] }

  override def getMainCodeTab(): MainCodeTab = { mainCodeTab }
  def getCodeTabContainer = { codeTabContainer }

  codeTabContainer.setTitle("Code Tab Window")

  currentTab = mainCodeTab

  // Because of the order in which elements of the NetLogo application come into being
  // the CodeTabsPanel cannot be fully built when it is first instantiated.
  // These steps are complete by the init method. AAB 10/2020
  def init(manager: FileManager, monitor: DirtyMonitor) {
    addTab(I18N.gui.get("tabs.code"), mainCodeTab)
    initManagerMonitor(manager, monitor)

    // Currently Ctrl-CLOSE_BRACKET = Ctrl-] closes the separate code window. AAB 10/2020
    tabManager.setSeparateCodeTabBindings()
    getAppFrame.addLinkComponent(getCodeTabContainer)
    Event.rehash()
  }

  def handle(e: AfterLoadEvent) = {
    tabManager.createCodeTabAccelerators()
  }

  this.addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent) {
      // A single mouse click switches focus to a tab. AAB 10/2020
      if (me.getClickCount() == 1) {
        val currentTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        tabManager.setCurrentTab(currentTab)
        currentTab.requestFocus()
      }
      // A single mouse control-click on the MainCodeTab in a separate window
      // closes the code window, and takes care of the bookkeeping. AAB 10/2020
      if (me.getClickCount() == 1 && me.isControlDown) {
        val currentTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        if (currentTab.isInstanceOf[MainCodeTab]) {
          tabManager.switchToNoSeparateCodeWindow
        }
      }
    }
  })

  // If the user closes the code window, take care of the bookkeeping. AAB 10/2020
  codeTabContainer.addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) {
      tabManager.switchToNoSeparateCodeWindow
    }
  })

  // If focus returns to the code tab window, make its currentTab
  // be selected. AAB 10/2020
  codeTabContainer.addWindowFocusListener(new WindowAdapter() {
    override def  windowGainedFocus(e: WindowEvent) {
      val currentTab = codeTabsPanel.getSelectedComponent
      tabManager.setCurrentTab(currentTab)
    }
  })

  def stateChanged(e: ChangeEvent) = {
    // for explanation of index -1, see comment in Tabs.stateChanged. AAB 10/2020
    if (tabManager.getSelectedAppTabIndex != -1) {
      val previousTab = tabManager.getCurrentTab
      currentTab = getSelectedComponent
      // currentTab could be null in the case where the CodeTabPanel has only the MainCodeTab. AAB 10/2020
      if (currentTab == null) {
        currentTab = mainCodeTab
      }
      tabManager.setCurrentTab(currentTab)
      currentTab.requestFocus()
      // The SwitchedTabsEvent will cause compilation when the user leaves an edited CodeTab. AAB 10/2020
      new AppEvents.SwitchedTabsEvent(previousTab, currentTab).raise(this)
    }
  }
}
