// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.app

import java.awt.event.{ MouseAdapter, MouseEvent, WindowAdapter, WindowEvent }

import javax.swing.{ Action, JTabbedPane, KeyStroke }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import scala.collection.mutable

import org.nlogo.app.codetab.{ ExternalFileManager, MainCodeTab, TemporaryCodeTab }
import org.nlogo.app.common.{ Events => AppEvents }
import org.nlogo.core.I18N
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.swing.{ TabsMenu, UserAction }, UserAction.MenuAction
import org.nlogo.window.{ GUIWorkspace }
import org.nlogo.window.Event.LinkParent

class CodeTabsPanel(workspace:             GUIWorkspace,
                       interfaceTab:          InterfaceTab,
                       externalFileManager:   ExternalFileManager,
                       val mainCodeTab:           MainCodeTab,
                       val externalFileTabs:  mutable.Set[TemporaryCodeTab])
  extends AbstractTabs(workspace, interfaceTab, externalFileManager)
  with ChangeListener
  with LinkParent
  with org.nlogo.window.LinkRoot {

  locally {
    addChangeListener(this)
  }

  // frame is the App's AppFrame, (treated as a java.awt.Frame), which is the container for the
  // CodeTabContainer that contains CodeTabsPanel that contains
  // the MainCodeTab
  val frame = workspace.getFrame
  //aab? this.requestFocusInWindow
  val codeTabContainer = new CodeTabContainer(frame, this)
  val codeTabsPanel = this

  override def getMainCodeTab(): MainCodeTab = { mainCodeTab }
  def getCodeTabContainer = { codeTabContainer }

  codeTabContainer.setTitle("Code Tab Window")

  currentTab = mainCodeTab

  def init(manager: FileManager, monitor: DirtyMonitor) {
    addTab(I18N.gui.get("tabs.code"), mainCodeTab)
    initManagerMonitor(manager, monitor)
    tabManager.setSeparateCodeTabBindings(this)

    // Add keystrokes for actions from TabsMenu to the codeTabsPanel
    TabsMenu.tabActions(tabManager).foreach(action => {
      // Add the accelerator key if any to the input map and action map
      action.asInstanceOf[MenuAction].accelerator match {
        case None                =>
        case Some(accKey: KeyStroke) =>  {
          val actionName = action.getValue(Action.NAME) match {
            case s: String => s
            case _         => accKey.toString
          }
          tabManager.addCodeTabContainerKeyStroke(codeTabsPanel, accKey, action, actionName)
        }
      }
    })
  }

  this.addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent) {
      if (me.getClickCount() == 1) {
        val currentTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        tabManager.setCurrentTab(currentTab)
        currentTab.requestFocus()
      }
      if (me.getClickCount() == 1 && me.isControlDown) {
        val currentTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        if (currentTab.isInstanceOf[MainCodeTab]) {
          tabManager.switchToNoSeparateCodeWindow
        }
      }
    }
  })

  codeTabContainer.addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) {
      tabManager.switchToNoSeparateCodeWindow
    }
  })

  codeTabContainer.addWindowFocusListener(new WindowAdapter() {
    override def  windowGainedFocus(e: WindowEvent) {
      // maybe add focus to text area
      val currentTab = codeTabsPanel.getSelectedComponent
      tabManager.setCurrentTab(currentTab)
    }
  })

  def stateChanged(e: ChangeEvent) = {
    // for explanation see comment in Tabs.stateChanged
    if (tabManager.getSelectedAppTabIndex != -1) {
      val previousTab = tabManager.getCurrentTab
      currentTab = getSelectedComponent
      // Could happen in the case the CodeTabPanel has only the MainCodeTab
      if (currentTab == null) {
        currentTab = mainCodeTab
      }
      tabManager.setCurrentTab(currentTab)
      currentTab.requestFocus()
      new AppEvents.SwitchedTabsEvent(previousTab, currentTab).raise(this)
    }
  }
}
