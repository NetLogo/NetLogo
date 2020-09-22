// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.{ Component }
import java.awt.event.{ MouseEvent }
import javax.swing.{ JFrame, JTabbedPane, SwingConstants }
import javax.swing.plaf.ComponentUI

import org.nlogo.app.codetab.{ CodeTab, ExternalFileManager, MainCodeTab }
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.window.{ GUIWorkspace }

// AbstractTabs contains functionality common to Tabs and MainCodeTabPanel

abstract class AbstractTabs(val workspace:           GUIWorkspace,
                            val interfaceTab:        InterfaceTab,
                            val externalFileManager: ExternalFileManager)
  extends JTabbedPane(SwingConstants.TOP) {

  locally {
    setOpaque(false)
    setFocusable(false)
    if (System.getProperty("os.name").startsWith("Mac")) {
      try {
        val ui = Class.forName("org.nlogo.app.MacTabbedPaneUI").newInstance.asInstanceOf[ComponentUI]
        setUI(ui)
      } catch {
        case e: ClassNotFoundException =>
      }
    }
  }

  val jframe = workspace.getFrame.asInstanceOf[JFrame]
  var tabManager: AppTabManager = null
  def setTabManager( myTabManager: AppTabManager ) : Unit = {
    tabManager = myTabManager
  }

  def getCodeTab():MainCodeTab
  def getTabManager() = tabManager
  def getAppFrame() = workspace.getFrame.asInstanceOf[AppFrame]
  var fileManager: FileManager = null
  var dirtyMonitor: DirtyMonitor = null
  var currentTab: Component = interfaceTab

  def initManagerMonitor(manager: FileManager, monitor: DirtyMonitor): Unit =  {
    fileManager = manager
    dirtyMonitor = monitor
    assert(fileManager != null && dirtyMonitor != null)
  }

//  @throws (classOf[IndexOutOfBoundsException])
  def setSelectedIndexAdjusted(index: Int): Unit =  {
    val (tabOwner, tabIndex) = getTabManager.computeIndexPlus(index)
    if (index >= 2) {
      println("tabIndex " + tabIndex )
      App.printSwingObject(tabOwner, "tabOwner ")
    }
    //         actualMainCodeTabPanel.setSelectedComponent(getAppsTab.codeTab)

    getCodeTab.requestFocus
    tabOwner.setSelectedIndex(tabIndex)
    //tabOwner.setSelectedComponent(tabOwner.getComponentAt(tabIndex))
  }

  def getTitleAtAdjusted(index: Int): String =  {
    val (tabOwner, tabIndex) = getTabManager.computeIndexPlus(index)
    tabOwner.getTitleAt(tabIndex)
  }

  override def requestFocus() = currentTab.requestFocus()

  def getIndexOfComponent(tab: CodeTab): Int =
    (0 until getTabCount).find(n => getComponentAt(n) == tab).get

//  def forAllCodeTabs = tabManager.forAllCodeTabs(_)

  override def processMouseMotionEvent(e: MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }
}
