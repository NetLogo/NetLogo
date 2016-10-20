// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.Graphics
import java.awt.event.ActionEvent
import javax.swing.{ AbstractAction, Action, JTabbedPane, UIManager }
import javax.swing.plaf.ComponentUI

import org.nlogo.app.codetab.{ CodeTab, MainCodeTab, TemporaryCodeTab }
import org.nlogo.app.common.{ Events => AppEvents, TabsInterface }
import org.nlogo.app.infotab.InfoTab
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.app.tools.AgentMonitorManager
import org.nlogo.core.I18N
import org.nlogo.swing.{ RichAction, UserAction, TabsMenu }, UserAction.{ ActionCategoryKey, ActionRankKey , TabsCategory }
import org.nlogo.swing.Implicits._
import org.nlogo.window.{EditDialogFactoryInterface, GUIWorkspace}
import org.nlogo.window.Events._

class Tabs(val workspace:  GUIWorkspace,
           monitorManager: AgentMonitorManager,
           dialogFactory:  EditDialogFactoryInterface) extends JTabbedPane(javax.swing.SwingConstants.TOP)
  with TabsInterface with javax.swing.event.ChangeListener with org.nlogo.window.Event.LinkParent
  with org.nlogo.window.LinkRoot
  with LoadBeginEvent.Handler
  with RuntimeErrorEvent.Handler
  with CompiledEvent.Handler
  with ModelSavedEvent.Handler
  with AfterLoadEvent.Handler {

  locally{
    setOpaque(false)
    setFocusable(false)
    addChangeListener(this)
    if (System.getProperty("os.name").startsWith("Mac")) {
      try {
        val ui = Class.forName("org.nlogo.app.MacTabbedPaneUI").newInstance.asInstanceOf[ComponentUI]
        setUI(ui)
      } catch {
        case e: ClassNotFoundException =>
      }
    }
  }

  // This might make more sense as a constructor parameter - RG 10/27/16
  var menu: UserAction.Menu = null

  var tabActions: Seq[Action] = TabsMenu.tabActions(this)

  def menuActions = tabActions ++ codeTab.menuActions ++ interfaceTab.menuActions :+ printAction

  val interfaceTab = new InterfaceTab(workspace, monitorManager, dialogFactory)
  val infoTab = new InfoTab(workspace.attachModelDir(_))
  val codeTab = new MainCodeTab(workspace, this)

  var previousTab: java.awt.Component = interfaceTab
  var currentTab: java.awt.Component = interfaceTab

  def init(moreTabs: (String, java.awt.Component) *) {
    addTab(I18N.gui.get("tabs.run"), interfaceTab)
    addTab(I18N.gui.get("tabs.info"), infoTab)
    addTab(I18N.gui.get("tabs.code"), codeTab)
    for((name, tab) <- moreTabs)
      addTab(name, tab)

    tabActions = TabsMenu.tabActions(this)
  }

  def stateChanged(e: javax.swing.event.ChangeEvent) {
    previousTab = currentTab
    currentTab = getSelectedComponent
    currentTab.requestFocus()
    new AppEvents.SwitchedTabsEvent(previousTab, currentTab).raise(this)
  }

  override def requestFocus() { currentTab.requestFocus() }
  def handle(e: LoadBeginEvent) { setSelectedComponent(interfaceTab) }
  def handle(e: RuntimeErrorEvent) {
    if(!e.jobOwner.isInstanceOf[org.nlogo.window.MonitorWidget])
      if(e.sourceOwner == codeTab)
        highlightRuntimeError(codeTab, e)
      else if(e.sourceOwner.isInstanceOf[org.nlogo.window.ExternalFileInterface]) {
        val filename = e.sourceOwner.asInstanceOf[org.nlogo.window.ExternalFileInterface].getFileName
        val tab = getTabWithFilename(filename).getOrElse{
          openTemporaryFile(filename, true)
          getTabWithFilename(filename).get
        }
        highlightRuntimeError(tab, e)
      }
  }

  def highlightRuntimeError(tab: CodeTab, e: RuntimeErrorEvent) {
    setSelectedComponent(tab)
    // the use of invokeLater here is a desperate attempt to work around the Mac bug where sometimes
    // the selection happens and sometime it doesn't - ST 8/28/04
    org.nlogo.awt.EventQueue.invokeLater( () => tab.select(e.pos, e.pos + e.length) )
  }

  val errorColor = java.awt.Color.RED

  def handle(e: CompiledEvent) {
    def clearErrors() {
      for(i <- 0 until getTabCount)
        if(getComponentAt(i).isInstanceOf[CodeTab])
          setForegroundAt(i, null)
    }
    def recolorTab(component: java.awt.Component, hasError: Boolean) {
      setForegroundAt(indexOfComponent(component), if(hasError) errorColor else null)
    }

    // recolor tabs
    if(e.sourceOwner.isInstanceOf[CodeTab]) {
      val tab = e.sourceOwner.asInstanceOf[CodeTab]
      if(e.error != null) setSelectedComponent(tab)
      // on null error, clear all errors, as we only get one event for all the files
      if(e.error == null) clearErrors() else recolorTab(tab, e.error != null)
      // I don't really know why this is necessary when you delete a slider (by using the menu
      // item *not* the button) which causes an error in the Code tab the focus gets lost,
      // so request the focus by a known component 7/18/07
      requestFocus()
    }
    if(e.sourceOwner.isInstanceOf[org.nlogo.window.ExternalFileInterface]) {
      val filename = e.sourceOwner.asInstanceOf[org.nlogo.window.ExternalFileInterface].getFileName
      var tab = getTabWithFilename(filename)
      if(! tab.isDefined && e.error != null) {
        openTemporaryFile(filename, true)
        tab = getTabWithFilename(filename)
      }
      if(e.error != null) setSelectedComponent(tab.get)
      recolorTab(tab.get, e.error != null)
      requestFocus()
    }
    if((e.sourceOwner.isInstanceOf[org.nlogo.window.JobWidget] &&
        !e.sourceOwner.asInstanceOf[org.nlogo.window.JobWidget].isCommandCenter
        || e.sourceOwner == null // i'm assuming this is only true when
        // we've deleted that last widget. not a great sol'n - AZS 5/16/05
      )) {
      if(e.error != null) setSelectedIndex(0)
      recolorTab(interfaceTab, e.error != null)
    }
  }

  def openTemporaryFile(filename: String, fileMustExist: Boolean) {
    getTabWithFilename(filename) match {
      case Some(tab) => setSelectedComponent(tab)
      case _ => addNewTab(filename, fileMustExist)
    }
  }

  def getSource(filename: String): String = getTabWithFilename(filename).map(_.innerSource).orNull

  private def getTabWithFilename(name: String): Option[TemporaryCodeTab] =
    // start at 3 because 0, 1, and 2 are the permanent tabs
    (3 until getTabCount)
      .map(getComponentAt)
      .collect{case tab: TemporaryCodeTab => tab}
      .find(_.filename == name)

  def newTemporaryFile() { addNewTab(TemporaryCodeTab.NewFile, false) }

  def addNewTab(name: String, fileMustExist: Boolean) {
    val tab = new TemporaryCodeTab(workspace, this, name, fileMustExist, codeTab.smartTabbingEnabled)
    addTab(stripPath(name), tab)
    addMenuItem(getTabCount() - 1, stripPath(name))
    org.nlogo.window.Event.rehash()
    tab.includesMenu.updateVisibility()
    setSelectedComponent(tab)
    // if I just call requestFocus the tab never gets the focus request because it's not yet
    // visible.  There might be a more swing appropriate way to do this but I can't figure it out
    // (if you know it feel free to fix) ev 7/24/07
    org.nlogo.awt.EventQueue.invokeLater( () => requestFocus() )
  }

  def saveExternalFiles() {
    (3 until getTabCount)
      .map(getComponentAt)
      .collect{case tab: TemporaryCodeTab => tab}
      .foreach(_.doSave())
  }

  def saveTemporaryFile(filename: String) {
    getTabWithFilename(filename) foreach { tab =>
      val index = getIndexOfComponent(tab)
      setTitleAt(index, stripPath(filename))
      tabActions(index).putValue(Action.NAME, filename)
    }
  }

  def getIndexOfComponent(tab: CodeTab): Int =
    (0 until getTabCount).find(n => getComponentAt(n) == tab).get

  def closeTemporaryFile(filename: String) {
    getTabWithFilename(filename) foreach { tab =>
      val index = getIndexOfComponent(tab)
      remove(tab)
      removeMenuItem(index)
    }
  }

  def forAllCodeTabs(fn: CodeTab => Unit) =
    getComponents collect { case tab: CodeTab => tab } foreach (fn)

  def lineNumbersVisible = codeTab.lineNumbersVisible
  def lineNumbersVisible_=(visible: Boolean) = forAllCodeTabs(_.lineNumbersVisible = visible)

  def removeMenuItem(index: Int) {
    tabActions.foreach(menu.revokeAction(_))
    tabActions = TabsMenu.tabActions(this)
    tabActions.foreach(menu.offerAction(_))
  }

  def addMenuItem(i: Int, name: String) {
    val newAction = TabsMenu.tabAction(this, i)
    tabActions = tabActions :+ newAction
    menu.offerAction(newAction)
  }

  private def stripPath(filename: String): String =
    filename.substring(filename.lastIndexOf(System.getProperty("file.separator")) + 1, filename.length)

  val printAction = {
    val action = RichAction("print-current-tab") { _ =>
      currentTab match {
        case printable: org.nlogo.swing.Printable =>
          try org.nlogo.swing.PrinterManager.print(printable, workspace.modelNameForDisplay)
          catch {
            case abortEx: java.awt.print.PrinterAbortException => org.nlogo.api.Exceptions.ignore(abortEx)
          }
      }
    }

    action.putValue(Action.NAME,                  I18N.gui.get("menu.file.print"))
    action.putValue(UserAction.ActionCategoryKey, UserAction.FileCategory)
    action.putValue(UserAction.ActionGroupKey,    "org.nlogo.app.Tabs.Print")
    action.putValue(Action.ACCELERATOR_KEY,       UserAction.KeyBindings.keystroke('P', withMenu = true))
    action
  }

  override def processMouseMotionEvent(e: java.awt.event.MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }

  def handle(e: ModelSavedEvent) {
    saveExternalFiles()
  }

  def handle(e: AfterLoadEvent) {
    requestFocus()
  }
}
