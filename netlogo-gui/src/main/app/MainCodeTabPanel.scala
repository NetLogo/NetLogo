// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Color, Component }
import java.awt.event.{ MouseAdapter, MouseEvent }
import javax.swing.{ JTabbedPane }
import javax.swing.event.{ ChangeEvent, ChangeListener }

import org.nlogo.app.codetab.{ CodeTab, ExternalFileManager, MainCodeTab, TemporaryCodeTab }

import org.nlogo.app.common.{ TabsInterface }, TabsInterface.Filename
import org.nlogo.core.I18N
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.app.infotab.InfoTab
import org.nlogo.window.Events._
import org.nlogo.window.{ GUIWorkspace }
import org.nlogo.window.Event.LinkParent

class MainCodeTabPanel(workspace:           GUIWorkspace,
                       interfaceTab:        InterfaceTab,
                       externalFileManager: ExternalFileManager,
                       private var menu:    MenuBar)
  extends AbstractTabs(workspace, interfaceTab, externalFileManager)
  with TabsInterface
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

  val codeTab = new MainCodeTab(workspace, this, menu)

  def getCodeTab = codeTab

  currentTab = codeTab

  var externalFileTabs = Set.empty[TemporaryCodeTab]

  def init(manager: FileManager, monitor: DirtyMonitor, moreTabs: (String, Component) *) {
    addTab(I18N.gui.get("tabs.code"), codeTab)
    initManagerMonitor(manager, monitor)
  }

  def forAllCodeTabs(fn: CodeTab => Unit) =
    (externalFileTabs.asInstanceOf[Set[CodeTab]] + codeTab) foreach fn

  def handle(e: CompiledEvent) = {
    val errorColor = Color.RED
    def clearErrors() = {
      def clearForeground(tab: Component) = {
        tabManager.getTabOwner(tab).setForegroundAt(
          tabManager.getTabOwner(tab).indexOfComponent(tab), null)
        }
        forAllCodeTabs(clearForeground)
      }

      def recolorTab(component: Component, hasError: Boolean): Unit = {
        tabManager.getTabOwner(component).setForegroundAt(
          tabManager.getTabOwner(component).indexOfComponent(component),
          if(hasError) errorColor else null)
      }

      // recolor tabs
      e.sourceOwner match {
        case `codeTab` =>
          // on null error, clear all errors, as we only get one event for all the files
          if (e.error == null) {
            clearErrors()
          }
          else {
            tabManager.setSelectedCodeTab(codeTab)
            recolorTab(codeTab, true)
          }
          // I don't really know why this is necessary when you delete a slider (by using the menu
          // item *not* the button) which causes an error in the Code tab the focus gets lost,
          // so request the focus by a known component 7/18/07
          requestFocus()
        case _ =>
      }
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

    // for trait TabsInterface
    val infoTab = new InfoTab(workspace.attachModelDir(_))

    def lineNumbersVisible = codeTab.lineNumbersVisible
    def lineNumbersVisible_=(visible: Boolean) = forAllCodeTabs(_.lineNumbersVisible = visible)

    def newExternalFile() = tabManager.getAppsTab.newExternalFile()
    def openExternalFile(filename: String) = tabManager.getAppsTab.openExternalFile(filename: String)
    def closeExternalFile(filename: Filename) = tabManager.getAppsTab.closeExternalFile(filename: Filename)
}
