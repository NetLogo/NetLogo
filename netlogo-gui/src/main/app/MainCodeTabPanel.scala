// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.app

import java.awt.{ Component }
import java.awt.event.{ MouseAdapter, MouseEvent, WindowAdapter, WindowEvent }

import javax.swing.{ JTabbedPane }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import scala.collection.mutable

import org.nlogo.app.codetab.{ ExternalFileManager, MainCodeTab, TemporaryCodeTab }
import org.nlogo.core.I18N
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.window.{ GUIWorkspace }
import org.nlogo.window.Event.LinkParent

class MainCodeTabPanel(workspace:             GUIWorkspace,
                       interfaceTab:          InterfaceTab,
                       externalFileManager:   ExternalFileManager,
                       val codeTab:           MainCodeTab,
                       val externalFileTabs:  mutable.Set[TemporaryCodeTab])
  extends AbstractTabs(workspace, interfaceTab, externalFileManager)
  with ChangeListener
  with LinkParent
  with org.nlogo.window.LinkRoot {

  locally {
    addChangeListener(this)
  }

  // frame is the App's AppFrame, (treated as a java.awt.Frame), which is the container for the
  // CodeTabContainer that contains MainCodeTabPanel that contains
  // the MainCodeTab
  val frame = workspace.getFrame
  //aab? this.requestFocusInWindow
  val codeTabContainer = new CodeTabContainer(frame, this)
  val mainCodeTabPanel = this

  override def getCodeTab(): MainCodeTab = codeTab
  def getCodeTabContainer = codeTabContainer

  codeTabContainer.setTitle("Code Tab Window")

  currentTab = codeTab

  def init(manager: FileManager, monitor: DirtyMonitor, moreTabs: (String, Component) *) {
    addTab(I18N.gui.get("tabs.code"), codeTab)
    initManagerMonitor(manager, monitor)
    tabManager.setSeparateCodeTabBindings(this)
    tabManager.addDeleteCodeTabButton(this)
  }

  this.addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent) {
      if (me.getClickCount() == 1) {
        val currentTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        tabManager.setCurrentTab(currentTab)
        currentTab.requestFocus()
      }
    }
  })

  codeTabContainer.addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) {
      tabManager.switchToTabsCodeTab
    }
  })

  codeTabContainer.addWindowFocusListener(new WindowAdapter() {
    override def  windowGainedFocus(e: WindowEvent) {
      // maybe add focus to text area
      val currentTab = mainCodeTabPanel.getSelectedComponent
      tabManager.setCurrentTab(currentTab)
      // don't want to go to browser, come back and then compile
      // will need to consider case with included FileSaveGroup
      // if (tabManager.getCodeTab.dirty) {
      //   new AppEvents.SwitchedTabsEvent(tabManager.getCodeTab, currentTab).raise(getTabs)
      // }
    }
  })

  def stateChanged(e: ChangeEvent) = {
    currentTab = getSelectedComponent
    if (currentTab == null) {
      currentTab = codeTab
    }
    tabManager.setCurrentTab(currentTab)
    currentTab.requestFocus()
  }
}
