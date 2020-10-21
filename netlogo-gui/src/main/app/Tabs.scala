// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Color, Component }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent, WindowAdapter, WindowEvent }
import java.awt.print.PrinterAbortException
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.{ AbstractAction, Action, JTabbedPane }

import scala.collection.mutable

import org.nlogo.api.Exceptions
import org.nlogo.app.codetab.{ CodeTab, ExternalFileManager, MainCodeTab, TemporaryCodeTab }
import org.nlogo.app.common.{ ExceptionCatchingAction, MenuTab, TabsInterface, Events => AppEvents },
  TabsInterface.Filename
import org.nlogo.app.infotab.InfoTab
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.I18N
import org.nlogo.swing.{ Printable, PrinterManager, TabsMenu, UserAction }, UserAction.MenuAction
import org.nlogo.window.Event.LinkParent
import org.nlogo.window.Events._
import org.nlogo.window.{ Event, ExternalFileInterface, GUIWorkspace, JobWidget, MonitorWidget }

// This used to be the only class whose instance 'owned' the InterfaceTab, the InfoTab, the MainCodeTab and the included files tabs.
// Now when there is a separate code window, that work is shared by CodeTabsPanel for the CodeTabs.
// As a result, functionality common to Tabs and CodeTabsPanel is now in the class AbstractTabsPanel.
// The role of Tabs would be better served by the name AppTabsPanel. AAB 10/2020

class Tabs(workspace:           GUIWorkspace,
           interfaceTab:        InterfaceTab,
           externalFileManager: ExternalFileManager,
           private var menu:    MenuBar)
  extends AbstractTabsPanel(workspace, interfaceTab, externalFileManager)
  with TabsInterface with ChangeListener with LinkParent
  with org.nlogo.window.LinkRoot
  with AboutToCloseFilesEvent.Handler
  with LoadBeginEvent.Handler
  with RuntimeErrorEvent.Handler
  with CompiledEvent.Handler
  with AfterLoadEvent.Handler
  with ExternalFileSavedEvent.Handler {

  addChangeListener(this)

  def getTabs = { this }

  def setMenu(newMenu: MenuBar): Unit = {
    val menuItems = permanentMenuActions ++ (currentTab match {
      case mt: MenuTab => mt.activeMenuActions
      case _ => Seq()
    })
    menuItems.foreach(action => menu.revokeAction(action))
    menuItems.foreach(newMenu.offerAction)
    menu = newMenu
  }

  val mainCodeTab = new MainCodeTab(workspace, this, menu)

  def permanentMenuActions = {
    tabActions ++ mainCodeTab.permanentMenuActions ++ interfaceTab.permanentMenuActions :+ PrintAction
  }

  var tabActions: Seq[Action] = Seq.empty[Action]
  lazy val saveModelActions = fileManager.saveModelActions(this)
  val infoTab = new InfoTab(workspace.attachModelDir(_))
  val stableCodeTab = mainCodeTab
  val externalFileTabs = mutable.Set.empty[TemporaryCodeTab]

  override def getMainCodeTab(): MainCodeTab = { mainCodeTab }

  // The value of popOutCodeTab is set in the init method
  var popOutCodeTab : Boolean = _

  // Because of the order in which elements of the NetLogo application come into being
  // Tabs cannot be fully built when it is first instantiated.
  // These steps are complete by the init method. . AAB 10/2020

  // the moreTabs argument was there for PlugIns, and remains only
  // to allow easier testing of scenerios with novel tabs. . AAB 10/2020
  def init(manager: FileManager, monitor: DirtyMonitor, moreTabs: (String, Component) *): Unit =  {
    addTab(I18N.gui.get("tabs.run"), interfaceTab)
    addTab(I18N.gui.get("tabs.info"), infoTab)

    // If there is not separate code tab, the MainCodeTab belongs to Tabs. AAB 10/2020
    if (tabManager.getCodeTabsOwner.equals(this)) {
      popOutCodeTab = false
      addTab(I18N.gui.get("tabs.code"), mainCodeTab)
    } else {
      popOutCodeTab = true
    }

    for((name, tab) <- moreTabs) {
      addTab(name, tab)
    }

    tabActions = TabsMenu.tabActions(tabManager)
    fileManager = manager
    dirtyMonitor = monitor
    assert(fileManager != null && dirtyMonitor != null)

    saveModelActions foreach menu.offerAction

    // Currently Ctrl-OPEN_BRACKET = Ctrl-[ creates a separate code window. AAB 10/2020
    tabManager.setAppCodeTabBindings
  }

  jframe.addWindowFocusListener(new WindowAdapter() {
    override def windowGainedFocus(e: WindowEvent) {
      val currentTab = getTabs.getSelectedComponent
      tabManager.setCurrentTab(currentTab)
      if (tabManager.getMainCodeTab.dirty) {
        // The SwitchedTabsEvent can lead to compilation. AAB 10/2020
         new AppEvents.SwitchedTabsEvent(tabManager.getMainCodeTab, currentTab).raise(getTabs)
      }
    }
    })

  def stateChanged(e: ChangeEvent) = {
    // Because there can be a separate code tab window, it is
    // sometime necessary to deselect a tab by setting the selected
    // tab index of the parent JTabbedPane to -1
    // In that case do nothing. The correct action will happen when
    // the selected index is reset. AAB 10/2020
    if (tabManager.getSelectedAppTabIndex != -1) {
      val previousTab = tabManager.getCurrentTab
      currentTab = getSelectedComponent
      tabManager.setCurrentTab(currentTab)

      previousTab match {
        case mt: MenuTab => mt.activeMenuActions foreach menu.revokeAction
        case _ =>
      }
      currentTab match {
        case mt: MenuTab => mt.activeMenuActions foreach menu.offerAction
        case _ =>
      }
      // TODO: The correctness of this logic needs investigation - AAB 10/2020
      (previousTab.isInstanceOf[TemporaryCodeTab], currentTab.isInstanceOf[TemporaryCodeTab]) match {
        case (true, false) => saveModelActions foreach menu.offerAction
        case (false, true) => saveModelActions foreach menu.revokeAction
        case _             =>
      }
      currentTab.requestFocus()
      new AppEvents.SwitchedTabsEvent(previousTab, currentTab).raise(this)
    }
  }

  this.addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent): Unit =  {
      // A single mouse control-click on the MainCodeTab in a separate window
      // opens the code window, and takes care of the bookkeeping. AAB 10/2020
      if (me.getClickCount() == 1 && me.isControlDown) {
        val currentTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        if (currentTab.isInstanceOf[MainCodeTab]) {
          tabManager.switchToSeparateCodeWindow
        }
      }
    }
  })

  def handle(e: AboutToCloseFilesEvent) = {
    OfferSaveExternalsDialog.offer( collection.immutable.Set( externalFileTabs.toSeq: _*).asInstanceOf[Set[TemporaryCodeTab]]
    filter (_.saveNeeded) , this)
  }

  def handle(e: LoadBeginEvent) = {
    setSelectedComponent(interfaceTab)
    externalFileTabs foreach { tab =>
      externalFileManager.remove(tab)
      closeExternalFile(tab.filename)
    }
  }

  def handle(e: RuntimeErrorEvent) = {
   if(!e.jobOwner.isInstanceOf[MonitorWidget])
    e.sourceOwner match {
      case file: ExternalFileInterface =>
        val filename = file.getFileName
        val tab = getTabWithFilename(Right(filename)).getOrElse {
          openExternalFile(filename)
          getTabWithFilename(Right(filename)).get
        }
        highlightRuntimeError(tab, e)
      case _ =>
    }
  }

  def highlightRuntimeError(tab: CodeTab, e: RuntimeErrorEvent) = {
    setSelectedComponent(tab)
    // the use of invokeLater here is a desperate attempt to work around the Mac bug where sometimes
    // the selection happens and sometime it doesn't - ST 8/28/04
    EventQueue.invokeLater(() => tab.select(e.pos, e.pos + e.length) )
  }

  def handle(e: CompiledEvent) = {
    val errorColor = Color.RED

    def clearErrors(): Unit = {
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

    def recolorInterfaceTab(): Unit = {
      if (e.error != null) setSelectedIndex(0)
      recolorTab(interfaceTab, e.error != null)
    }

    // recolor tabs
    e.sourceOwner match {
      case `stableCodeTab` =>
      // on null error, clear all errors, as we only get one event for all the files. AAB 10/2020
      if (e.error == null) {
        clearErrors()
      }
      else {
        tabManager.setSelectedCodeTab(mainCodeTab)
        recolorTab(mainCodeTab, true)
      }
      // I don't really know why this is necessary when you delete a slider (by using the menu
      // item *not* the button) which causes an error in the Code tab the focus gets lost,
      // so request the focus by a known component 7/18/07
      requestFocus()
      case file: ExternalFileInterface =>
        val filename = file.getFileName
        var tab = getTabWithFilename(Right(filename))
        if (!tab.isDefined && e.error != null) {
          openExternalFile(filename)
          tab = getTabWithFilename(Right(filename))
          tab.get.handle(e) // it was late to the party, let it handle the event too
        }
        if (e.error != null) setSelectedComponent(tab.get)
        recolorTab(tab.get, e.error != null)
        requestFocus()
      case null => // i'm assuming this is only true when we've deleted that last widget. not a great sol'n - AZS 5/16/05
        recolorInterfaceTab()
      case jobWidget: JobWidget if !jobWidget.isCommandCenter =>
        recolorInterfaceTab()
      case _ =>
    }
  }

  def handle(e: ExternalFileSavedEvent) = {
    getTabWithFilename(Right(e.path)) foreach { tab =>
      val index = tabManager.getTabOwner(tab).indexOfComponent(tab)
      setTitleAt(index, tab.filenameForDisplay)
      tabActions(index).putValue(Action.NAME, e.path)
    }
  }

  def getSource(filename: String): String = { getTabWithFilename(Right(filename)).map(_.innerSource).orNull }

  def getTabWithFilename(filename: Filename): Option[TemporaryCodeTab] = {
    externalFileTabs find (_.filename == filename)
  }

  private var _externalFileNum = 1
  private def externalFileNum() = {
    _externalFileNum += 1
    _externalFileNum - 1
  }

  def newExternalFile() = { addNewTab(Left(I18N.gui.getN("tabs.external.new", externalFileNum(): Integer))) }

  def openExternalFile(filename: String) = {
    getTabWithFilename(Right(filename)) match {
      case Some(tab) => setSelectedComponent(tab)
      case _ => addNewTab(Right(filename))
    }
  }

  def addNewTab(name: Filename) = {
    val tab = new TemporaryCodeTab(workspace, this, name, externalFileManager, fileManager.convertTabAction _, mainCodeTab.smartTabbingEnabled)
    if (externalFileTabs.isEmpty) menu.offerAction(SaveAllAction)
    externalFileTabs += tab
    getCodeTabsOwner.addTab(tab.filenameForDisplay, tab)
    addMenuItem(tabManager.getCombinedTabCount - 1, tab.filenameForDisplay)
    Event.rehash()

    setPanelsSelectedComponent(tab)
    // if I just call requestFocus the tab never gets the focus request because it's not yet
    // visible.  There might be a more swing appropriate way to do this but I can't figure it out
    // (if you know it feel free to fix) ev 7/24/07
    EventQueue.invokeLater( () => requestFocus() )
  }

  def closeExternalFile(filename: Filename): Unit = {
    getTabWithFilename(filename) foreach { tab =>
      val index = getIndexOfCodeTab(tab)
      removeTab(tab)
      removeMenuItem(index)
      externalFileTabs -= tab
      if (externalFileTabs.isEmpty) menu.revokeAction(SaveAllAction)
    }
  }

  def forAllCodeTabs(fn: CodeTab => Unit) = {
    (externalFileTabs.asInstanceOf[mutable.Set[CodeTab]] + mainCodeTab) foreach fn
  }
  def lineNumbersVisible = { mainCodeTab.lineNumbersVisible }
  def lineNumbersVisible_=(visible: Boolean) = { forAllCodeTabs(_.lineNumbersVisible = visible) }

  def removeMenuItem(index: Int) {
    tabActions.foreach(action => menu.revokeAction(action))
    tabActions = TabsMenu.tabActions(tabManager)
    tabActions.foreach(action => menu.offerAction(action))
  }

  def addMenuItem(i: Int, name: String) {
    val newAction = TabsMenu.tabAction(tabManager, i)
    tabActions = tabActions :+ newAction
    menu.offerAction(newAction)
  }

  override def processMouseMotionEvent(e: MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }

  def handle(e: AfterLoadEvent) = {
    mainCodeTab.getPoppingCheckBox.setSelected(popOutCodeTab)
    requestFocus()
  }


  object SaveAllAction extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAll"), this)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileSaveGroup
    rank        = 1
    accelerator = UserAction.KeyBindings.keystroke('S', withMenu = true, withAlt = true)

    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      fileManager.saveModel(false)
      externalFileTabs foreach (_.save(false))
    }
  }

  object PrintAction extends AbstractAction(I18N.gui.get("menu.file.print")) with UserAction.MenuAction {
    category = UserAction.FileCategory
    group = "org.nlogo.app.Tabs.Print"
    accelerator = UserAction.KeyBindings.keystroke('P', withMenu = true)

    def actionPerformed(e: ActionEvent) = currentTab match {
      case printable: Printable =>
        try PrinterManager.print(printable, workspace.modelNameForDisplay)
        catch {
          case abortEx: PrinterAbortException => Exceptions.ignore(abortEx)
        }
    }
  }
}
