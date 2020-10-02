// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app
import java.awt.{ Component }
import java.awt.event.{ MouseEvent }
import javax.swing.{ JFrame, JTabbedPane, SwingConstants }
import javax.swing.plaf.ComponentUI

// aab import org.nlogo.app.codetab.{ }
import org.nlogo.app.codetab.{ CodeTab, ExternalFileManager, MainCodeTab }
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.window.{ GUIWorkspace }

// AbstractTabs contains functionality common to Tabs and CodeTabsPanel

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
  def getAppJFrame() = jframe
  var fileManager: FileManager = null
  var dirtyMonitor: DirtyMonitor = null
  var currentTab: Component = interfaceTab

  def initManagerMonitor(manager: FileManager, monitor: DirtyMonitor): Unit =  {
    fileManager = manager
    dirtyMonitor = monitor
    assert(fileManager != null && dirtyMonitor != null)
  }

  override def processMouseMotionEvent(e: MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }

  override def requestFocus() = currentTab.requestFocus()

  def getCodeTabsOwner(): JTabbedPane = {
    getTabManager.getCodeTabsOwner.asInstanceOf[JTabbedPane]
  }

  def getTitleAtAdjusted(index: Int): String =  {
    val (tabOwner, tabIndex) = getTabManager.computeIndexPlus(index)
    tabOwner.getTitleAt(tabIndex)
  }

//  @throws (classOf[IndexOutOfBoundsException])
  def setSelectedIndexPanels(index: Int): Unit =  {
    val (tabOwner, tabIndex) = getTabManager.computeIndexPlus(index)
    // aab getCodeTab.requestFocus
    tabOwner.setSelectedIndex(tabIndex)
  }

  def setPanelsSelectedComponent(tab: Component): Unit = {
    val (tabOwner, tabIndex) = getTabManager.ownerAndIndexOfTab(tab)
    getTabManager.printAllTabs
    App.printSwingObject(tab, "setPanelsSelectedComponent, tab: ")
    println("tabIndex, " + tabIndex)
    App.printSwingObject(tabOwner, "setPanelsSelectedComponent, tabOwner: ")
    // aab getCodeTab.requestFocus
    tabOwner.setSelectedComponent(tab)
  }

  def getIndexOfCodeTab(tab: CodeTab): Int = {
    val indx = (0 until getTabCount).find(n => getComponentAt(n) == tab).get
    indx
  }


    // def getIndexOfComponent(tab: Component): Int =
    //   (0 until getTabCount).find(n => getComponentAt(n) == tab).get
//  def forAllCodeTabs = tabManager.forAllCodeTabs(_)


}
