// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Color, Component }
import java.awt.event.{ ActionEvent, MouseEvent }
import java.awt.print.PrinterAbortException
import javax.swing.event.{ ChangeEvent, ChangeListener }
import javax.swing.plaf.ComponentUI
import javax.swing.{ AbstractAction, Action, BoxLayout, JLabel, JPanel, JScrollPane, JTabbedPane, JTable, SwingConstants }
import javax.swing.table.DefaultTableModel

import org.nlogo.api.Exceptions
import org.nlogo.app.codetab.{ CodeTab, MainCodeTab, TemporaryCodeTab }
import org.nlogo.app.common.{ Actions, ExceptionCatchingAction, MenuTab, TabsInterface, Events => AppEvents },
  Actions.Ellipsis, TabsInterface.Filename
import org.nlogo.app.infotab.InfoTab
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.app.tools.AgentMonitorManager
import org.nlogo.awt.{ EventQueue, UserCancelException }
import org.nlogo.core.I18N
import org.nlogo.swing.Implicits._
import org.nlogo.swing.{ OptionDialog, Printable, PrinterManager, TabsMenu, UserAction }, UserAction.MenuAction
import org.nlogo.window.Event.LinkParent
import org.nlogo.window.Events._
import org.nlogo.window.{ EditDialogFactoryInterface, Event, ExternalFileInterface, GUIWorkspace, JobWidget, MonitorWidget }

class Tabs(val workspace:    GUIWorkspace,
           monitorManager:   AgentMonitorManager,
           dialogFactory:    EditDialogFactoryInterface,
           private var menu: MenuBar)
  extends JTabbedPane(SwingConstants.TOP)
  with TabsInterface with ChangeListener with LinkParent
  with org.nlogo.window.LinkRoot
  with AboutToCloseFilesEvent.Handler
  with LoadBeginEvent.Handler
  with RuntimeErrorEvent.Handler
  with CompiledEvent.Handler
  with AfterLoadEvent.Handler {

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
    tabActions ++ codeTab.permanentMenuActions ++ interfaceTab.permanentMenuActions ++ Seq(SaveAction, SaveAsAction, SaveAllAction, PrintAction)

  var tabActions: Seq[Action] = TabsMenu.tabActions(this)

  var fileManager: FileManager = null
  var dirtyMonitor: DirtyMonitor = null

  val interfaceTab = new InterfaceTab(workspace, monitorManager, dialogFactory)
  val infoTab = new InfoTab(workspace.attachModelDir(_))
  val codeTab = new MainCodeTab(workspace, this, menu)
  var externalFileTabs = Set.empty[TemporaryCodeTab]

  var previousTab: Component = interfaceTab
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
  }

  def stateChanged(e: ChangeEvent) {
    previousTab = currentTab
    previousTab match {
      case mt: MenuTab => mt.activeMenuActions.foreach(action => menu.revokeAction(action))
      case _ =>
    }
    currentTab = getSelectedComponent
    currentTab match {
      case mt: MenuTab => mt.activeMenuActions.foreach(action => menu.offerAction(action))
      case _ =>
    }
    currentTab.requestFocus()
    new AppEvents.SwitchedTabsEvent(previousTab, currentTab).raise(this)
  }

  override def requestFocus(): Unit = currentTab.requestFocus()

  def handle(e: AboutToCloseFilesEvent): Unit = {
    offerSaveModel()
    offerSaveExternalFiles()
  }

  def handle(e: LoadBeginEvent): Unit = setSelectedComponent(interfaceTab)

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
        }

  def highlightRuntimeError(tab: CodeTab, e: RuntimeErrorEvent) {
    setSelectedComponent(tab)
    // the use of invokeLater here is a desperate attempt to work around the Mac bug where sometimes
    // the selection happens and sometime it doesn't - ST 8/28/04
    EventQueue.invokeLater(() => tab.select(e.pos, e.pos + e.length) )
  }

  def handle(e: CompiledEvent) {
    val errorColor = Color.RED
    def clearErrors() = forAllCodeTabs(tab => setForegroundAt(indexOfComponent(tab), null))
    def recolorTab(component: Component, hasError: Boolean): Unit =
      setForegroundAt(indexOfComponent(component), if(hasError) errorColor else null)

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
        }
        if (e.error != null) setSelectedComponent(tab.get)
        recolorTab(tab.get, e.error != null)
        requestFocus()
      case null => // i'm assuming this is only true when we've deleted that last widget. not a great sol'n - AZS 5/16/05
        if(e.error != null) setSelectedIndex(0)
        recolorTab(interfaceTab, e.error != null)
      case jobWidget: JobWidget if !jobWidget.isCommandCenter =>
        if(e.error != null) setSelectedIndex(0)
        recolorTab(interfaceTab, e.error != null)
    }
  }

  @throws(classOf[UserCancelException])
  def offerSaveModel(): Unit = {
    if (dirtyMonitor.dirty && fileManager.userWantsToSaveFirst()) {
      fileManager.saveModel(false)
    }
  }

  private def offerSaveExternalFiles(): Unit = {
    implicit val i18nPrefix = I18N.Prefix("file.save")
    val dirtyExternalFiles = externalFileTabs filter (_.dirty)
    if (dirtyExternalFiles.nonEmpty) {
      val panel = new JPanel
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS))
      panel.add(new JLabel(I18N.gui("filesChanged")))
      val table = new JTable(SaveTableModel)
      table.setShowGrid(false)
      table.getTableHeader.setReorderingAllowed(false)
      table.getTableHeader.setResizingAllowed(false)
      table.setRowSelectionAllowed(false)
      table.getColumnModel.getColumn(0).setMaxWidth(getFontMetrics(getFont).stringWidth(I18N.gui("shouldSave")) * 2)
      table.validate()
      panel.add(new JScrollPane(table))
      val options = Array(I18N.gui("saveSelected"), I18N.gui("discardAll"), I18N.gui.get("common.buttons.cancel"))
      OptionDialog.showCustom(this, "NetLogo", panel, options) match {
        case 0 => SaveTableModel.files filter (_ (0).asInstanceOf[Boolean]) foreach (row => getTabWithFilename(row(1).asInstanceOf[Filename]) foreach (_.save(false)))
        case 1 =>
        case _ => throw new UserCancelException
      }
    }
  }

  def getSource(filename: String): String = getTabWithFilename(Right(filename)).map(_.innerSource).orNull

  def getTabWithFilename(filename: Filename): Option[TemporaryCodeTab] =
    externalFileTabs find (_.filename == Right(filename))

  def newExternalFile() = addNewTab(Left("New File" + " " + (externalFileTabs.size + 1)))

  def openExternalFile(filename: String) =
    getTabWithFilename(Right(filename)) match {
      case Some(tab) => setSelectedComponent(tab)
      case _ => addNewTab(Right(filename))
    }

  def addNewTab(name: Filename) = {
    val tab = new TemporaryCodeTab(workspace, this, name, codeTab.smartTabbingEnabled)
    externalFileTabs += tab
    val title = (name.right map stripPath).merge
    addTab(title, tab)
    addMenuItem(getTabCount - 1, title)
    Event.rehash()
    setSelectedComponent(tab)
    // if I just call requestFocus the tab never gets the focus request because it's not yet
    // visible.  There might be a more swing appropriate way to do this but I can't figure it out
    // (if you know it feel free to fix) ev 7/24/07
    EventQueue.invokeLater( () => requestFocus() )
  }

  def saveExternalFiles() = externalFileTabs foreach (_.doSave)

  def saveExternalFile(filename: Filename) =
    getTabWithFilename(filename) foreach { tab =>
      val index = indexOfComponent(tab)
      setTitleAt(index, filename.right.map(stripPath).merge)
      tabActions(index).putValue(Action.NAME, filename.merge)
    }

  def getIndexOfComponent(tab: CodeTab): Int =
    (0 until getTabCount).find(n => getComponentAt(n) == tab).get

  def closeExternalFile(filename: Filename): Unit =
    getTabWithFilename(filename) foreach { tab =>
      val index = getIndexOfComponent(tab)
      remove(tab)
      removeMenuItem(index)
      externalFileTabs -= tab
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

  private def stripPath(filename: String): String =
    filename.substring(filename.lastIndexOf(System.getProperty("file.separator")) + 1, filename.length)

  object SaveAction extends ExceptionCatchingAction(I18N.gui.get("menu.file.save"), this)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileSaveGroup
    accelerator = UserAction.KeyBindings.keystroke('S', withMenu = true)

    @throws(classOf[UserCancelException])
    override def action(): Unit = currentTab match {
      case externalFileTab: TemporaryCodeTab =>
        externalFileTab.save(true)
        saveExternalFile(externalFileTab.filename)
      case _                                 => fileManager.saveModel(false)
    }
  }

  object SaveAsAction extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAs") + Ellipsis, this)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileSaveGroup
    accelerator = UserAction.KeyBindings.keystroke('S', withMenu = true, withShift = true)

    @throws(classOf[UserCancelException])
    override def action(): Unit = currentTab match {
      case externalFileTab: TemporaryCodeTab =>
        externalFileTab.save(true)
        saveExternalFile(externalFileTab.filename)
      case _                                 => fileManager.saveModel(true)
    }
  }

  object SaveAllAction extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAll"), this)
  with MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileSaveGroup

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

  override def processMouseMotionEvent(e: MouseEvent) {
    // do nothing.  mouse moves are for some reason causing doLayout to be called in the tabbed
    // components on windows and linux (but not Mac) in java 6 it never did this before and I don't
    // see any reason why it needs to. It's causing flickering in the info tabs on the affected
    // platforms ev 2/2/09
  }

  def handle(e: AfterLoadEvent) {
    requestFocus()
  }

  private object SaveTableModel extends DefaultTableModel {
    implicit val i18nPrefix = I18N.Prefix("file.save")
    def dirtyExternalFiles = externalFileTabs filter (_.dirty)
    val files = (dirtyExternalFiles map (tab => Array(true: java.lang.Boolean, tab.filename))).toArray
    override def getValueAt(row: Int, col: Int) = col match {
      case 0 => files(row)(col)
      case 1 => files(row)(col).asInstanceOf[Filename].merge
    }
    override def setValueAt(value: AnyRef, row: Int, col: Int) = files(row)(col) = value
    override def getRowCount: Int = dirtyExternalFiles.size
    override def getColumnCount: Int = 2
    override def getColumnName(i: Int): String = i match {
      case 0 => I18N.gui("shouldSave")
      case 1 => I18N.gui("filename")
    }
    override def getColumnClass(i: Int): Class[_] = i match {
      case 0 => classOf[java.lang.Boolean]
      case 1 => classOf[String]
    }
    override def isCellEditable(row: Int, col: Int) = col == 0
  }
}
