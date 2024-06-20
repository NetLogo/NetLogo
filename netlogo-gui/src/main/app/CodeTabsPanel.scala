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

class CodeTabsPanel(workspace:            GUIWorkspace,
                    interfaceTab:         InterfaceTab,
                    externalFileManager:  ExternalFileManager,
                    val mainCodeTab:      MainCodeTab,
                    val externalFileTabs: mutable.Set[TemporaryCodeTab])
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
  val codeTabContainer = new CodeTabContainer(frame, this)
  val codeTabsPanel = this

  override def getMainCodeTab(): MainCodeTab = { mainCodeTab }
  def getCodeTabContainer = { codeTabContainer }

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
    override def mouseClicked(me: MouseEvent): Unit =  {
      // A single mouse control-click on the MainCodeTab when it is in a separate window
      // closes the separate code window, and takes care of the bookkeeping. AAB 10/2020
      if (me.getClickCount() == 1 && me.isControlDown) {
        val clickedTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        if (clickedTab.isInstanceOf[MainCodeTab]) {
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

  def stateChanged(e: ChangeEvent) = {
    // for explanation of index -1, see comment in Tabs.stateChanged. AAB 10/2020
    if (!tabManager.switchingCodeTabs && tabManager.getSelectedAppTabIndex != -1) {
      val previousTab = getCurrentTab
      currentTab = getSelectedComponent
      // currentTab could be null in the case where the CodeTabPanel has only the MainCodeTab. AAB 10/2020
      if (currentTab == null) {
        currentTab = mainCodeTab
      }
      // The SwitchedTabsEvent will cause compilation when the user leaves an edited CodeTab. AAB 10/2020
      new AppEvents.SwitchedTabsEvent(previousTab, currentTab).raise(this)
    }
  }

  java.awt.EventQueue.invokeLater(new Runnable() {
    override def run(): Unit = {
      codeTabContainer.toFront()
      codeTabContainer.repaint()
    }
  })
}
