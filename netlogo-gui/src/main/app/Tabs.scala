// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Color, Component }
import java.awt.event.{ ActionEvent, MouseEvent }
import java.awt.print.PrinterAbortException
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.plaf.ComponentUI
import javax.swing.{ AbstractAction, Action, JTabbedPane, SwingConstants }

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

class Tabs(val workspace:       GUIWorkspace,
           val interfaceTab:    InterfaceTab,
           private var menu:    MenuBar,
           externalFileManager: ExternalFileManager)
  extends JTabbedPane(SwingConstants.TOP)
  with TabsInterface with ChangeListener with LinkParent
  with org.nlogo.window.LinkRoot
  with AboutToCloseFilesEvent.Handler
  with LoadBeginEvent.Handler
  with RuntimeErrorEvent.Handler
  with CompiledEvent.Handler
  with AfterLoadEvent.Handler
  with ExternalFileSavedEvent.Handler {

  locally {
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

  def setMenu(newMenu: MenuBar): Unit = {
    val menuItems = permanentMenuActions ++ (currentTab match {
      case mt: MenuTab => mt.activeMenuActions
      case _ => Seq()
    })
    menuItems.foreach(action => menu.revokeAction(action))
    menuItems.foreach(newMenu.offerAction)
    menu = newMenu
  }

  def permanentMenuActions =
    tabActions ++ codeTab.permanentMenuActions ++ interfaceTab.permanentMenuActions :+ PrintAction

  var tabActions: Seq[Action] = TabsMenu.tabActions(this)
  lazy val saveModelActions = fileManager.saveModelActions(this)

  var fileManager: FileManager = null
  var dirtyMonitor: DirtyMonitor = null

  val infoTab = new InfoTab(workspace.attachModelDir(_), workspace.compiler.dialect)
  val codeTab = new MainCodeTab(workspace, this, menu)
  var externalFileTabs = Set.empty[TemporaryCodeTab]

  var currentTab: Component = interfaceTab

  def init(manager: FileManager, monitor: DirtyMonitor, moreTabs: (String, Component) *) {
    addTab(I18N.gui.get("tabs.run"), interfaceTab)
    addTab(I18N.gui.get("tabs.info"), infoTab)
    addTab(I18N.gui.get("tabs.code"), codeTab)
    for((name, tab) <- moreTabs)
      addTab(name, tab)

    tabActions = TabsMenu.tabActions(this)
    fileManager = manager
    dirtyMonitor = monitor
    assert(fileManager != null && dirtyMonitor != null)

    saveModelActions foreach menu.offerAction
  }

  def stateChanged(e: ChangeEvent) = {
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

    currentTab.requestFocus()
    new AppEvents.SwitchedTabsEvent(previousTab, currentTab).raise(this)
  }

  override def requestFocus() = currentTab.requestFocus()

  def handle(e: AboutToCloseFilesEvent) =
    OfferSaveExternalsDialog.offer(externalFileTabs filter (_.saveNeeded), this)

  def handle(e: LoadBeginEvent) = {
    setSelectedComponent(interfaceTab)
    externalFileTabs foreach { tab =>
      externalFileManager.remove(tab)
      closeExternalFile(tab.filename)
    }
  }

  def handle(e: RuntimeErrorEvent) =
     if(!e.jobOwner.isInstanceOf[MonitorWidget])
        e.sourceOwner match {
          case `codeTab` =>
            highlightRuntimeError(codeTab, e)
          case file: ExternalFileInterface =>
            val filename = file.getFileName
            val tab = getTabWithFilename(Right(filename)).getOrElse {
              openExternalFile(filename)
              getTabWithFilename(Right(filename)).get
            }
            highlightRuntimeError(tab, e)
          case _ =>
        }

  def highlightRuntimeError(tab: CodeTab, e: RuntimeErrorEvent) {
    setSelectedComponent(tab)
    // the use of invokeLater here is a desperate attempt to work around the Mac bug where sometimes
    // the selection happens and sometime it doesn't - ST 8/28/04
    EventQueue.invokeLater(() => tab.select(e.pos, e.pos + e.length) )
  }

  def handle(e: CompiledEvent) = {
    val errorColor = Color.RED
    def clearErrors() = forAllCodeTabs(tab => setForegroundAt(indexOfComponent(tab), null))
    def recolorTab(component: Component, hasError: Boolean): Unit =
      setForegroundAt(indexOfComponent(component), if(hasError) errorColor else null)
    def recolorInterfaceTab() = {
      if (e.error != null) setSelectedIndex(0)
      recolorTab(interfaceTab, e.error != null)
    }

    // recolor tabs
    e.sourceOwner match {
      case `codeTab` =>
        // on null error, clear all errors, as we only get one event for all the files
        if (e.error == null)
          clearErrors()
        else {
          setSelectedComponent(codeTab)
          recolorTab(codeTab, true)
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

  def handle(e: ExternalFileSavedEvent) =
    getTabWithFilename(Right(e.path)) foreach { tab =>
      val index = indexOfComponent(tab)
      setTitleAt(index, tab.filenameForDisplay)
      tabActions(index).putValue(Action.NAME, e.path)
    }

  def getSource(filename: String): String = getTabWithFilename(Right(filename)).map(_.innerSource).orNull

  def getTabWithFilename(filename: Filename): Option[TemporaryCodeTab] =
    externalFileTabs find (_.filename == filename)

  private var _externalFileNum = 1
  private def externalFileNum() = {
    _externalFileNum += 1
    _externalFileNum - 1
  }
  def newExternalFile() = addNewTab(Left(I18N.gui.getN("tabs.external.new", externalFileNum(): Integer)))

  def openExternalFile(filename: String) =
    getTabWithFilename(Right(filename)) match {
      case Some(tab) => setSelectedComponent(tab)
      case _ => addNewTab(Right(filename))
    }

  def addNewTab(name: Filename) = {
    val tab = new TemporaryCodeTab(workspace, this, name, externalFileManager, fileManager.convertTabAction _, codeTab.smartTabbingEnabled)
    if (externalFileTabs.isEmpty) menu.offerAction(SaveAllAction)
    externalFileTabs += tab
    addTab(tab.filenameForDisplay, tab)
    addMenuItem(getTabCount - 1, tab.filenameForDisplay)
    Event.rehash()
    setSelectedComponent(tab)
    // if I just call requestFocus the tab never gets the focus request because it's not yet
    // visible.  There might be a more swing appropriate way to do this but I can't figure it out
    // (if you know it feel free to fix) ev 7/24/07
    EventQueue.invokeLater( () => requestFocus() )
  }

  def getIndexOfComponent(tab: CodeTab): Int =
    (0 until getTabCount).find(n => getComponentAt(n) == tab).get

  def closeExternalFile(filename: Filename): Unit =
    getTabWithFilename(filename) foreach { tab =>
      val index = getIndexOfComponent(tab)
      remove(tab)
      removeMenuItem(index)
      externalFileTabs -= tab
      if (externalFileTabs.isEmpty) menu.revokeAction(SaveAllAction)
    }

  def forAllCodeTabs(fn: CodeTab => Unit) =
    (externalFileTabs.asInstanceOf[Set[CodeTab]] + codeTab) foreach fn

  def lineNumbersVisible = codeTab.lineNumbersVisible
  def lineNumbersVisible_=(visible: Boolean) = forAllCodeTabs(_.lineNumbersVisible = visible)

  def removeMenuItem(index: Int) {
    tabActions.foreach(action => menu.revokeAction(action))
    tabActions = TabsMenu.tabActions(this)
    tabActions.foreach(action => menu.offerAction(action))
  }

  def addMenuItem(i: Int, name: String) {
    val newAction = TabsMenu.tabAction(this, i)
    tabActions = tabActions :+ newAction
    menu.offerAction(newAction)
  }

  override def processMouseMotionEvent(e: MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }

  def handle(e: AfterLoadEvent) = requestFocus()


  object SaveAllAction extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAll"), this)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileSaveGroup
    rank        = 1

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
