// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Color, Component }
import java.awt.event.{ ActionEvent, MouseAdapter, MouseEvent, WindowAdapter, WindowEvent }
import java.awt.print.PrinterAbortException
import java.nio.file.{ Path, Paths }
import java.util.prefs.Preferences
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
import org.nlogo.swing.{ Printable, PrinterManager, UserAction }, UserAction.MenuAction
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
  with LoadModelEvent.Handler
  with AboutToSaveModelEvent.Handler
  with ModelSavedEvent.Handler
  with ExternalFileSavedEvent.Handler {

  addChangeListener(this)

  private var watcherThread: FileWatcherThread = null
  private val prefs = Preferences.userRoot.node("/org/nlogo/NetLogo")

  def stopWatcherThread() {
    if (watcherThread != null) {
      watcherThread.interrupt
      watcherThread = null
    }
  }

  def startWatcherThread(modelPath: String = workspace.getModelPath) {
    // Stop the current thread if there's one. This ensures that there can be
    // at most one thread.
    stopWatcherThread()

    if (modelPath != null) {
      def f(x: Map[String, String]): List[Path] = x.values.map(Paths.get(_)).toList
      val includes: List[Path] = mainCodeTab.getIncludesTable.map(f).getOrElse(List.empty)

      watcherThread = new FileWatcherThread(Paths.get(modelPath) :: includes, handleFileChange)
      watcherThread.start
    }
  }

  def setWatchingFiles(value: Boolean, modelPath: String = workspace.getModelPath) {
    if (value) {
      startWatcherThread(modelPath)
    } else {
      stopWatcherThread()
    }
  }

  def watchingFiles = watcherThread != null
  def watchingFiles_=(value: Boolean) {
    setWatchingFiles(value)
  }

  def getAutoReload = prefs.get("reloadOnExternalChanges", "false").toBoolean

  def reload() {
    if (!reloading) {
      reloading = true
      workspace.reload
    }
  }

  private def handleFileChange(): Boolean = {
    // We stop the file watcher thread after the dialog is shown, so we need to
    // start it back up in these callbacks.

    def cancelCallback() {
      startWatcherThread()
    }

    def okCallback() {
      reload()
      startWatcherThread()
    }

    val dirty = tabManager.getDirtyMonitor.modelDirty

    val dirtyDialog = new DirtyNotificationDialog(workspace.getFrame, okCallback, cancelCallback)

    if (dirty) {
      dirtyDialog.setVisible(true)
    } else {
      reload()
    }

    // Return 'dirty' to stop the file watcher thread if file is dirty. This is
    // to prevent the file dirty dialog from being shown back-to-back.
    dirty
  }

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

  // Because of the order in which elements of the NetLogo application come into being
  // Tabs cannot be fully built when it is first instantiated.
  // These steps are complete by the init method. AAB 10/2020

  // the moreTabs argument was there for PlugIns, and remains only
  // to allow easier testing of scenerios with novel tabs. AAB 10/2020
  def init(manager: FileManager, monitor: DirtyMonitor, moreTabs: (String, Component) *): Unit =  {
    addTab(I18N.gui.get("tabs.run"), interfaceTab)
    addTab(I18N.gui.get("tabs.info"), infoTab)
    addTab(I18N.gui.get("tabs.code"), mainCodeTab)
    // If there is not separate code tab, the MainCodeTab belongs to Tabs.
    // Otherwise it will get transferred by CodeTabsPanel.init AAB 10/2020

    for((name, tab) <- moreTabs) {
      addTab(name, tab)
    }

    tabActions = TabsMenu.tabActions(tabManager)
    initManagerMonitor(manager, monitor)

    saveModelActions foreach menu.offerAction

    tabManager.setDirtyMonitor(monitor)

    // Set hotkey to create a separate code window. AAB 12/2020
    tabManager.setAppCodeTabBindings
  }

    // When there is a separate code tab window, there will be a selected tab in
    // both windows. This means that the selected tab in the non-active window will
    // not be selectable. A WindowFocusListener is used to detect when the user
    // changes focus to the main GUI window from the Code Tab window, so that
    // a dirty code tab will be compiled.
  jframe.addWindowFocusListener(new WindowAdapter() {
    override def windowGainedFocus(e: WindowEvent) {
      val currentTab = getTabs.getSelectedComponent
      // The main GUI window can gain focus if you click outside of the NetLogo application
      // and then click main GUI window. This should never lead to compilation if there
      // is no separate code tab because a state change event will take care of things.
      // The SwitchedTabsEvent can lead to compilation. AAB 10/2020
      if (tabManager.isCodeTabSeparate && tabManager.getMainCodeTab.dirty) {
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
      val previousTab = currentTab
      currentTab = getSelectedComponent

      previousTab match {
        case mt: MenuTab => mt.activeMenuActions foreach menu.revokeAction
        case _ =>
      }
      currentTab match {
        case mt: MenuTab => mt.activeMenuActions foreach menu.offerAction
        case _ =>
      }

      (previousTab.isInstanceOf[TemporaryCodeTab], currentTab.isInstanceOf[TemporaryCodeTab]) match {
        case (true, false) => saveModelActions foreach menu.offerAction
        case (false, true) => saveModelActions foreach menu.revokeAction
        case _             =>
      }
      new AppEvents.SwitchedTabsEvent(previousTab, currentTab).raise(this)
    }
  }

  this.addMouseListener(new MouseAdapter() {
    override def mouseClicked(me: MouseEvent): Unit =  {
      // A single mouse control-click on the MainCodeTab in a separate window
      // opens the code window, and takes care of the bookkeeping. AAB 10/2020
      if (me.getClickCount() == 1 && me.isControlDown) {
        val clickedTab = me.getSource.asInstanceOf[JTabbedPane].getSelectedComponent
        if (clickedTab.isInstanceOf[MainCodeTab]) {
          tabManager.switchToSeparateCodeWindow
        }
      }
    }
  })

  def handle(e: AboutToCloseFilesEvent) = {
    OfferSaveExternalsDialog.offer( collection.immutable.Set( externalFileTabs.toSeq: _*).asInstanceOf[Set[TemporaryCodeTab]]
    filter (_.saveNeeded) , this)
  }

  var reloading: Boolean = false

  def handle(e: LoadBeginEvent) = {
    if (!reloading) {
      setSelectedComponent(interfaceTab)
      externalFileTabs foreach { tab =>
        externalFileManager.remove(tab)
        closeExternalFile(tab.filename)
      }
    }
  }

  def handle(e: RuntimeErrorEvent) = {
    if (!e.jobOwner.isInstanceOf[MonitorWidget]) {
      e.sourceOwner match {
        case `mainCodeTab` =>
          highlightRuntimeError(mainCodeTab, e)

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
  }

  def highlightRuntimeError(tab: CodeTab, e: RuntimeErrorEvent) = {
    tabManager.setPanelsSelectedComponent(tab)
    // the use of invokeLater here is a desperate attempt to work around the Mac bug where sometimes
    // the selection happens and sometime it doesn't - ST 8/28/04
    EventQueue.invokeLater(() => tab.select(e.pos, e.pos + e.length) )
  }

  def handle(e: CompiledEvent) = {
    val errorColor = Color.RED

    def clearErrors(): Unit = {
      def clearForeground(tab: Component) = {
        // In the case where the code tab has not been compiled,
        // is in a separate window, if one exits the separate window,
        // if the code compiles correctly setting the foreground throws
        // an exception because of a problem getting tab's bounds.
        // Perhaps this is related to the fact that the tab has just
        // moved from the CodeTabsPanel to Tabs. AAB 10/2020
        try {
          tabManager.getTabOwner(tab).setForegroundAt(
            tabManager.getTabOwner(tab).indexOfComponent(tab), null)
        } catch {
          case indexEx: java.lang.ArrayIndexOutOfBoundsException => Exceptions.ignore(indexEx)
        }
      }
      forAllCodeTabs(clearForeground)
    }

    def recolorTab(component: Component, hasError: Boolean): Unit = {
      // Use try as in clearErrors, just in case AAB 10/2020
      try {
        tabManager.getTabOwner(component).setForegroundAt(
          tabManager.getTabOwner(component).indexOfComponent(component),
          if(hasError) errorColor else null)
      } catch {
        case indexEx: java.lang.ArrayIndexOutOfBoundsException => Exceptions.ignore(indexEx)
      }
    }

    def recolorInterfaceTab(): Unit = {
      if (e.error != null) setSelectedIndex(0)
      recolorTab(interfaceTab, e.error != null)
    }

    // recolor tabs
    e.sourceOwner match {
      case `stableCodeTab` => {
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
      }
      case file: ExternalFileInterface => {
        val filename = file.getFileName
        var tab = getTabWithFilename(Right(filename))
        if (!tab.isDefined && e.error != null) {
          openExternalFile(filename)
          tab = getTabWithFilename(Right(filename))
          tab.get.handle(e) // it was late to the party, let it handle the event too
        }
        if (e.error != null) {
          tabManager.setPanelsSelectedComponent(tab.get)
        }
        recolorTab(tab.get, e.error != null)
        requestFocus()
      }
      case null => { // i'm assuming this is only true when we've deleted that last widget. not a great sol'n - AZS 5/16/05
        recolorInterfaceTab()
      }
      case jobWidget: JobWidget if !jobWidget.isCommandCenter => {
        recolorInterfaceTab()
      }
      case _ =>
    }
  }

  def handle(e: ExternalFileSavedEvent) = {
    getTabWithFilename(Right(e.path)) foreach { tab =>
      val (tabowner, index) = tabManager.ownerAndIndexOfTab(tab)
      tabowner.setTitleAt(index, tab.filenameForDisplay)
      val combinedTabIndex = tabManager.combinedIndexFromOwnerAndIndex(tabowner, index)
      tabActions(combinedTabIndex).putValue(Action.NAME, e.path)
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

  def newExternalFile() = {
    addNewExternalFileTab(Left(I18N.gui.getN("tabs.external.new", externalFileNum(): Integer)))
  }

  def openExternalFile(filename: String) = {
    getTabWithFilename(Right(filename)) match {
      case Some(tab) => tabManager.setPanelsSelectedComponent(tab)
      case _ => addNewExternalFileTab(Right(filename))
    }
  }

  def addNewExternalFileTab(name: Filename) = {
    val tab = new TemporaryCodeTab(workspace, this, name, externalFileManager, fileManager.convertTabAction _, mainCodeTab.smartTabbingEnabled)
    if (externalFileTabs.isEmpty) menu.offerAction(SaveAllAction)
    externalFileTabs += tab
    tabManager.addNewTab(tab, tab.filenameForDisplay)
    Event.rehash()

    tabManager.setPanelsSelectedComponent(tab)
    // if I just call requestFocus the tab never gets the focus request because it's not yet
    // visible.  There might be a more swing appropriate way to do this but I can't figure it out
    // (if you know it feel free to fix) ev 7/24/07
    EventQueue.invokeLater( () => requestFocus() )
  }

  def closeExternalFile(filename: Filename): Unit = {
    getTabWithFilename(filename) foreach { tab =>
      tabManager.removeTab(tab)
      externalFileTabs -= tab
      if (externalFileTabs.isEmpty) {
        menu.revokeAction(SaveAllAction)
        // Could change to remove and copy only FileMenu accelerators - AAB Nov 2020
        tabManager.removeCodeTabContainerAccelerators
        tabManager.copyMenuBarAccelerators
      }
    }
  }

  def forAllCodeTabs(fn: CodeTab => Unit) = {
    (externalFileTabs.asInstanceOf[mutable.Set[CodeTab]] + mainCodeTab) foreach fn
  }
  def lineNumbersVisible = { mainCodeTab.lineNumbersVisible }
  def lineNumbersVisible_=(visible: Boolean) = { forAllCodeTabs(_.lineNumbersVisible = visible) }

  // Renamed to reflect what it actually does now.
  // This method removes all tab entries in the TabsMenu because they come from a list of tabActions
  // that could be outdated. It then updates the tabActions with the tabs that
  // currently exist, and regenerates the TabsMenu.
  // Removed argument index which was not being used - AAB Nov 2020
  def updateTabsMenu() {
    tabActions.foreach(action => menu.revokeAction(action))
    tabActions = TabsMenu.tabActions(tabManager)
    tabActions.foreach(action => menu.offerAction(action))
    // Could change to remove and copy only TabsMenu accelerators - AAB Nov 2020
    tabManager.removeCodeTabContainerAccelerators
    tabManager.copyMenuBarAccelerators
  }

  def addMenuItem(i: Int, name: String) {
    val newAction = TabsMenu.tabAction(tabManager, i)
    tabActions = tabActions :+ newAction
    menu.offerAction(newAction)
    // This may not be necessary AAB 08/21
    tabManager.copyMenuAcceleratorsByName(I18N.gui.get("menu.tabs"))
  }

  override def processMouseMotionEvent(e: MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }

  def handle(e: AfterLoadEvent) = {
    mainCodeTab.getPoppingCheckBox.setSelected(tabManager.isCodeTabSeparate)
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

  def setDirtyMonitorCodeWindow(): Unit = {
    getTabManager.setDirtyMonitorCodeWindow
  }
  def switchToSpecifiedCodeWindowState(state: Boolean): Unit = {
    getTabManager.switchToSpecifiedCodeWindowState(state)
  }

  def handle(e: LoadModelEvent) {
    // We need to restart the watcher thread every load because the list of
    // included files may have changed.

    stopWatcherThread()

    if(getAutoReload) {
      startWatcherThread()

      externalFileTabs foreach { tab =>
        tab.reload
      }
    }

    reloading = false
  }

  def handle(e: AboutToSaveModelEvent) {
    // Stop watching here so that the watcher thread doesn't detect our own
    // write.
    stopWatcherThread()
  }

  def handle(e: ModelSavedEvent) {
    setWatchingFiles(getAutoReload, e.modelPath)
  }

  watchingFiles = getAutoReload
}
