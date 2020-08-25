// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.app

import java.awt.{ Component }
import java.awt.event.{ MouseAdapter, MouseEvent }
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

  def getCodeTab = codeTab  // aab this might not be needed

  currentTab = codeTab

  def init(manager: FileManager, monitor: DirtyMonitor, moreTabs: (String, Component) *) {
    addTab(I18N.gui.get("tabs.code"), codeTab)
    initManagerMonitor(manager, monitor)
  }

  this.addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent) {
      val currentTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
      tabManager.setCurrentTab(currentTab)
    }
  })

  def stateChanged(e: ChangeEvent) = {
    currentTab = getSelectedComponent
    tabManager.setCurrentTab(currentTab)
    currentTab.requestFocus()
  }
}
