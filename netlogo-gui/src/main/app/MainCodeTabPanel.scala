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

  val codeTabContainer = new CodeTabContainer(frame, this)

  def getCodeTabContainer = codeTabContainer

  currentTab = codeTab

  def init(manager: FileManager, monitor: DirtyMonitor, moreTabs: (String, Component) *) {
    addTab(I18N.gui.get("tabs.code"), codeTab)
    setSelectedComponent(codeTab)
    initManagerMonitor(manager, monitor)
  }

  this.addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent) {
      if (me.getClickCount() == 1) {
        val currentTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        tabManager.setCurrentTab(currentTab)
      }
      if (me.getClickCount() == 2) {
        tabManager.getAppTabs.add(I18N.gui.get("tabs.code"), codeTab)
        tabManager.setMainCodeTabPanel(None)
        tabManager.switchToTabsCodeTab
        codeTabContainer.dispose
        //tabManager.switchToTabsCodeTab
      }
    }
  })

  codeTabContainer.addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) {
      tabManager.getAppTabs.add(I18N.gui.get("tabs.code"), codeTab)
      tabManager.setMainCodeTabPanel(None)
      tabManager.switchToTabsCodeTab
    }
  })

  codeTabContainer.addWindowFocusListener(new WindowAdapter() {
    override def  windowGainedFocus(e: WindowEvent) {
      // println("WindowFocusListener method called: windowGainedFocus.")
      // maybe add focus to text area
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
