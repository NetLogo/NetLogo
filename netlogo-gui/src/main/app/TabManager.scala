// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Color, Component, FlowLayout, Graphics, KeyboardFocusManager }
import java.awt.event.{ ActionEvent, KeyEvent, MouseAdapter, MouseEvent, WindowAdapter, WindowEvent,
                        WindowFocusListener }
import java.awt.print.PrinterAbortException
import java.nio.file.{ Path, Paths }
import java.util.prefs.Preferences
import javax.swing.{ AbstractAction, Action, Box, JComponent, JFrame, JLabel, JPanel }

import org.nlogo.api.Exceptions
import org.nlogo.app.codetab.{ CodeTab, ExternalFileManager, MainCodeTab, TemporaryCodeTab }
import org.nlogo.app.common.{ ExceptionCatchingAction, MenuTab, TabsInterface }
import org.nlogo.app.common.Events.SwitchedTabsEvent
import org.nlogo.app.common.TabsInterface.Filename
import org.nlogo.app.infotab.InfoTab
import org.nlogo.app.interfacetab.InterfaceTab
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.{ Printable, PrinterManager, UserAction, Utils }
import org.nlogo.window.Events.{ AboutToCloseFilesEvent, AboutToSaveModelEvent, CompileAllEvent, CompiledEvent,
                                 ExternalFileSavedEvent, LoadBeginEvent, LoadErrorEvent, LoadModelEvent,
                                 ModelSavedEvent, RuntimeErrorEvent }
import org.nlogo.window.{ ExternalFileInterface, GUIWorkspace, JobWidget, MonitorWidget }

class TabManager(val workspace: GUIWorkspace, val interfaceTab: InterfaceTab,
                 val externalFileManager: ExternalFileManager)
  extends TabsInterface with AboutToCloseFilesEvent.Handler with AboutToSaveModelEvent.Handler
  with CompiledEvent.Handler with ExternalFileSavedEvent.Handler with LoadBeginEvent.Handler
  with LoadErrorEvent.Handler with LoadModelEvent.Handler with ModelSavedEvent.Handler with RuntimeErrorEvent.Handler {
  
  private val closeIconLight = Utils.iconScaled("/images/close-light.png", 8, 8)
  private val closeIconDark = Utils.iconScaled("/images/close-dark.png", 8, 8)
  
  private class TabLabel(val text: String, tab: Component) extends JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
    val textLabel = new JLabel(text)
    var closeLabel: JLabel = null

    setOpaque(false)

    add(textLabel)

    tab match {
      case tempTab: TemporaryCodeTab =>
        closeLabel = new JLabel(closeIconLight)

        closeLabel.addMouseListener(new MouseAdapter {
          override def mouseClicked(e: MouseEvent) {
            if (e.getButton == MouseEvent.BUTTON1) {
              try {
                tempTab.prepareForClose()
                closeExternalTab(tempTab)
              }

              catch {
                case e: UserCancelException =>
              }
            }
          }
        })

        add(Box.createHorizontalStrut(10))
        add(closeLabel)
      
      case _ =>
    }
    
    override def paintComponent(g: Graphics) {
      if (tab == mainTabs.getSelectedComponent || tab == separateTabs.getSelectedComponent) {
        textLabel.setForeground(Color.WHITE)

        if (closeLabel != null)
          closeLabel.setIcon(closeIconLight)
      }

      else {
        textLabel.setForeground(Color.BLACK)

        if (closeLabel != null)
          closeLabel.setIcon(closeIconDark)
      }

      super.paintComponent(g)
    }
  }

  private val prefs = Preferences.userRoot.node("/org/nlogo/NetLogo")

  private val focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager

  val infoTab = new InfoTab(workspace.attachModelDir(_))
  val mainCodeTab = new MainCodeTab(workspace, this, null)

  val mainTabs = new TabsPanel(this)
  val separateTabs = new TabsPanel(this)

  val separateTabsWindow = new CodeTabsWindow(workspace.getFrame, separateTabs)

  private var previousTab: Component = interfaceTab

  var fileManager: FileManager = null
  var dirtyMonitor: DirtyMonitor = null
  var menuBar: MenuBar = null

  private var smartTabbing = true

  smartTabbingEnabled = prefs.getBoolean("indentAutomatically", true)

  private var watcherThread: FileWatcherThread = null

  private var tabActions: Seq[Action] = TabsMenu.tabActions(this)

  private var newFileNumber = 1

  private var reloading = false
  private var movingTabs = true

  addTabWithLabel(mainTabs, I18N.gui.get("tabs.run"), interfaceTab)
  addTabWithLabel(mainTabs, I18N.gui.get("tabs.info"), infoTab)
  addTabWithLabel(mainTabs, I18N.gui.get("tabs.code"), mainCodeTab)

  movingTabs = false

  workspace.getFrame.addWindowFocusListener(new WindowFocusListener {
    def windowGainedFocus(e: WindowEvent) {
      if (separateTabs.getSelectedComponent != null) {
        mainTabs.focusSelected

        setMenuActions(separateTabs.getSelectedComponent, mainTabs.getSelectedComponent)

        switchedTabs(mainTabs.getSelectedComponent)
      }
    }
    
    def windowLostFocus(e: WindowEvent) {}
  })

  private val appComponent = workspace.getFrame.asInstanceOf[JFrame].getContentPane.asInstanceOf[JComponent]

  appComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    .put(UserAction.KeyBindings.keystroke(KeyEvent.VK_W, withMenu = true, withShift = true), "openSeparateCodeTab")
  appComponent.getActionMap.put("openSeparateCodeTab", new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      switchWindow(true)
    }
  })

  appComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    .put(UserAction.KeyBindings.keystroke(KeyEvent.VK_OPEN_BRACKET, withMenu = true, withShift = true), "previousTab")
  appComponent.getActionMap.put("previousTab", new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      previousTab(mainTabs)
    }
  })

  appComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    .put(UserAction.KeyBindings.keystroke(KeyEvent.VK_CLOSE_BRACKET, withMenu = true, withShift = true), "nextTab")
  appComponent.getActionMap.put("nextTab", new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      nextTab(mainTabs)
    }
  })

  separateTabsWindow.addWindowListener(new WindowAdapter {
    override def windowClosing(e: WindowEvent) {
      switchWindow(false)
    }
  })

  separateTabsWindow.addWindowFocusListener(new WindowFocusListener {
    def windowGainedFocus(e: WindowEvent) {
      separateTabs.focusSelected

      setMenuActions(mainTabs.getSelectedComponent, separateTabs.getSelectedComponent)

      switchedTabs(separateTabs.getSelectedComponent)
    }
    
    def windowLostFocus(e: WindowEvent) {}
  })

  private val separateComponent = separateTabsWindow.getContentPane.asInstanceOf[JComponent]

  separateComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    .put(UserAction.KeyBindings.keystroke(KeyEvent.VK_W, withMenu = true), "closeSeparateCodeTab")
  separateComponent.getActionMap.put("closeSeparateCodeTab", new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      switchWindow(false)
    }
  })

  separateComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    .put(UserAction.KeyBindings.keystroke(KeyEvent.VK_OPEN_BRACKET, withMenu = true, withShift = true), "previousTab")
  separateComponent.getActionMap.put("previousTab", new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      previousTab(separateTabs)
    }
  })

  separateComponent.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
    .put(UserAction.KeyBindings.keystroke(KeyEvent.VK_CLOSE_BRACKET, withMenu = true, withShift = true), "nextTab")
  separateComponent.getActionMap.put("nextTab", new AbstractAction {
    def actionPerformed(e: ActionEvent) {
      nextTab(separateTabs)
    }
  })

  def init(fileManager: FileManager, dirtyMonitor: DirtyMonitor, menuBar: MenuBar, actions: Seq[Action]) {
    this.fileManager = fileManager
    this.dirtyMonitor = dirtyMonitor
    this.menuBar = menuBar

    actions.foreach(separateTabsWindow.menuBar.offerAction)
    permanentMenuActions.foreach(offerAction)

    updateTabActions()
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

  def stopWatcherThread() {
    if (watcherThread != null) {
      watcherThread.interrupt
      watcherThread = null
    }
  }

  def setWatchingFiles(value: Boolean, modelPath: String = workspace.getModelPath) {
    if (value)
      startWatcherThread(modelPath)
    else
      stopWatcherThread()
  }

  private def handleFileChange(): Boolean = {
    // We stop the file watcher thread after the dialog is shown, so we need to
    // start it back up in these callbacks.

    def cancelCallback() =
      startWatcherThread()

    def okCallback() {
      reload()
      startWatcherThread()
    }

    val dirty = dirtyMonitor.modelDirty

    val dirtyDialog = new DirtyNotificationDialog(workspace.getFrame, okCallback, cancelCallback)

    if (dirty)
      dirtyDialog.setVisible(true)
    else
      reload()
  

    // Return 'dirty' to stop the file watcher thread if file is dirty. This is
    // to prevent the file dirty dialog from being shown back-to-back.
    dirty
  }

  object SaveAllAction extends ExceptionCatchingAction(I18N.gui.get("menu.file.saveAll"), workspace.getFrame)
                       with UserAction.MenuAction {
    category    = UserAction.FileCategory
    group       = UserAction.FileSaveGroup
    rank        = 1
    accelerator = UserAction.KeyBindings.keystroke('S', withMenu = true, withAlt = true)

    @throws(classOf[UserCancelException])
    override def action(): Unit = {
      fileManager.saveModel(false)
      getExternalFileTabs.foreach(_.save(false))
    }
  }

  object PrintAction extends AbstractAction(I18N.gui.get("menu.file.print")) with UserAction.MenuAction {
    category = UserAction.FileCategory
    group = "org.nlogo.app.Tabs.Print"
    accelerator = UserAction.KeyBindings.keystroke('P', withMenu = true)

    def actionPerformed(e: ActionEvent) =
      getSelectedTab match {
        case printable: Printable =>
          try PrinterManager.print(printable, workspace.modelNameForDisplay)
          catch {
            case abortEx: PrinterAbortException => Exceptions.ignore(abortEx)
          }
      }
  }

  def permanentMenuActions =
    mainCodeTab.permanentMenuActions ++ interfaceTab.permanentMenuActions ++ interfaceTab.activeMenuActions ++
      fileManager.saveModelActions(workspace.getFrame) :+ PrintAction

  def setMenuActions(oldTab: Component, newTab: Component) {
    oldTab match {
      case mt: MenuTab => mt.activeMenuActions.foreach(revokeAction)
    }

    newTab match {
      case mt: MenuTab => mt.activeMenuActions.foreach(offerAction)
    }
  }

  def updateTabActions() {
    tabActions.foreach(revokeAction)

    tabActions = TabsMenu.tabActions(this)

    tabActions.foreach(offerAction)
  }

  def offerAction(action: Action) {
    menuBar.offerAction(action)
    separateTabsWindow.menuBar.offerAction(action)
  }

  def revokeAction(action: Action) {
    menuBar.revokeAction(action)
    separateTabsWindow.menuBar.revokeAction(action)
  }

  def smartTabbingEnabled = smartTabbing
  def smartTabbingEnabled_=(enabled: Boolean) = {
    smartTabbing = enabled
    prefs.putBoolean("indentAutomatically", enabled)

    mainCodeTab.setIndenter(enabled)
    getExternalFileTabs.foreach(_.setIndenter(enabled))
  }

  def lineNumbersVisible = mainCodeTab.lineNumbersVisible
  def lineNumbersVisible_=(visible: Boolean) = {
    mainCodeTab.lineNumbersVisible = visible
    getExternalFileTabs.foreach(_.lineNumbersVisible = visible)
  }

  def watchingFiles = watcherThread != null
  def watchingFiles_=(value: Boolean) = setWatchingFiles(value)

  watchingFiles = getAutoReload

  def getAutoReload = prefs.get("reloadOnExternalChanges", "false").toBoolean

  def focusOnError = prefs.getBoolean("focusOnError", true)

  def getTotalTabCount =
    mainTabs.getTabCount + separateTabs.getTabCount

  def getTabTitle(index: Int): String = {
    if (index >= mainTabs.getTabCount)
      separateTabs.getTabComponentAt(index - mainTabs.getTabCount).asInstanceOf[TabLabel].text
    else
      mainTabs.getTabComponentAt(index).asInstanceOf[TabLabel].text
  }

  def getTabWithFilename(filename: Filename): Option[TemporaryCodeTab] =
    getExternalFileTabs.find(_.filename == filename)

  def getSelectedTab: Component = {
    if (separateTabsWindow.isAncestorOf(focusManager.getFocusOwner))
      separateTabs.getSelectedComponent
    else
      mainTabs.getSelectedComponent
  }

  def getExternalFileTabs: Seq[TemporaryCodeTab] = {
    if (separateTabsWindow.isVisible)
      for (i <- 1 until separateTabs.getTabCount) yield separateTabs.getComponentAt(i).asInstanceOf[TemporaryCodeTab]
    else
      for (i <- 3 until mainTabs.getTabCount) yield mainTabs.getComponentAt(i).asInstanceOf[TemporaryCodeTab]
  }

  private def addTabWithLabel(tabsPanel: TabsPanel, title: String, tab: Component) {
    tabsPanel.addTab(null, tab)
    tabsPanel.setTabComponentAt(tabsPanel.getTabCount - 1, new TabLabel(title, tab))
  }

  def setSelectedIndex(index: Int) {
    if (index >= mainTabs.getTabCount) {
      separateTabs.setSelectedIndex(index - mainTabs.getTabCount)
      separateTabs.focusSelected
    }

    else {
      mainTabs.setSelectedIndex(index)
      mainTabs.focusSelected
    }
  }

  def setSelectedTab(tab: Component) {
    if (mainTabs.indexOfComponent(tab) == -1) {
      separateTabs.setSelectedComponent(tab)
      separateTabs.focusSelected
    }

    else {
      mainTabs.setSelectedComponent(tab)
      mainTabs.focusSelected
    }
  }

  def previousTab(tabsPanel: TabsPanel) {
    if (tabsPanel.getSelectedIndex > 0)
      tabsPanel.setSelectedIndex(tabsPanel.getSelectedIndex - 1)
  }

  def nextTab(tabsPanel: TabsPanel) {
    if (tabsPanel.getSelectedIndex < tabsPanel.getTabCount - 1)
      tabsPanel.setSelectedIndex(tabsPanel.getSelectedIndex + 1)
  }

  def switchedTabs(tab: Component) {
    if (!movingTabs && tab != previousTab) {
      tab.requestFocus

      new SwitchedTabsEvent(previousTab, tab).raise(workspace.getFrame)

      setMenuActions(previousTab, tab)

      previousTab = tab
    }
  }

  private def addExternalFile(name: Filename) {
    if (getExternalFileTabs.isEmpty)
      offerAction(SaveAllAction)

    val tab = new TemporaryCodeTab(workspace, this, name, externalFileManager, fileManager.convertTabAction(_),
                                   smartTabbing, separateTabsWindow.isVisible)

    if (separateTabsWindow.isVisible) {
      addTabWithLabel(separateTabs, tab.filenameForDisplay, tab)
      separateTabs.setSelectedComponent(tab)
    }

    else {
      addTabWithLabel(mainTabs, tab.filenameForDisplay, tab)
      mainTabs.setSelectedComponent(tab)
    }

    updateTabActions()
  }

  def newExternalFile {
    addExternalFile(Left(I18N.gui.getN("tabs.external.new", newFileNumber: Integer)))

    newFileNumber += 1
  }

  def addTab(tab: Component, name: String) {
    if (separateTabsWindow.isVisible) {
      addTabWithLabel(separateTabs, name, tab)
      separateTabs.setSelectedComponent(tab)
    }

    else {
      addTabWithLabel(mainTabs, name, tab)
      mainTabs.setSelectedComponent(tab)
    }
  }

  def openExternalFile(filename: String) {
    getTabWithFilename(Right(filename)) match {
      case Some(tab) =>
        if (separateTabsWindow.isVisible)
          separateTabs.setSelectedComponent(tab)
        else
          mainTabs.setSelectedComponent(tab)
      case _ =>
        addExternalFile(Right(filename))

        new CompileAllEvent().raise(mainCodeTab)
    }
  }

  def replaceTab(oldTab: Component, newTab: Component) {
    val index = mainTabs.indexOfComponent(oldTab)

    if (index == -1)
      separateTabs.setComponentAt(separateTabs.indexOfComponent(oldTab), newTab)
    else
      mainTabs.setComponentAt(index, newTab)
  }

  def closeExternalFile(filename: Filename) {
    getTabWithFilename(filename) match {
      case Some(tab) =>
        closeExternalTab(tab)

      case _ =>
    }
  }

  def closeExternalTab(tab: TemporaryCodeTab) {
    if (separateTabsWindow.isVisible)
      separateTabs.remove(tab)
    else
      mainTabs.remove(tab)
    
    externalFileManager.remove(tab)

    if (getExternalFileTabs.isEmpty)
      revokeAction(SaveAllAction)
    
    updateTabActions()
  }

  def removeTab(tab: Component) {
    if (mainTabs.indexOfComponent(tab) == -1)
      separateTabs.remove(tab)
    else
      mainTabs.remove(tab)
  }

  def switchWindow(separate: Boolean, preserveSelected: Boolean = false) {
    movingTabs = true

    if (separate) {
      val selected = mainTabs.getSelectedComponent

      while (mainTabs.getTabCount > 2) {
        val tabLabel = mainTabs.getTabComponentAt(2)

        separateTabs.addTab(mainTabs.getTitleAt(2), mainTabs.getComponentAt(2))
        separateTabs.setTabComponentAt(separateTabs.getTabCount() - 1, tabLabel)
      }

      separateTabsWindow.open()

      mainCodeTab.setSeparate(true)
      getExternalFileTabs.foreach(_.setSeparate(true))

      if (selected.isInstanceOf[CodeTab]) {
        mainTabs.setSelectedIndex(0)
        separateTabs.setSelectedComponent(selected)
        selected.requestFocus
      }

      else {
        mainTabs.setSelectedComponent(selected)
        separateTabs.setSelectedIndex(0)
        mainCodeTab.requestFocus
      }
    }

    else {
      val selected = separateTabs.getSelectedComponent

      while (separateTabs.getTabCount > 0) {
        val tabLabel = separateTabs.getTabComponentAt(0)

        mainTabs.addTab(separateTabs.getTitleAt(0), separateTabs.getComponentAt(0))
        mainTabs.setTabComponentAt(mainTabs.getTabCount() - 1, tabLabel)
      }

      separateTabsWindow.setVisible(false)

      mainCodeTab.setSeparate(false)
      getExternalFileTabs.foreach(_.setSeparate(false))

      if (preserveSelected)
        mainTabs.setSelectedComponent(selected)

      mainTabs.getSelectedComponent.requestFocus
    }

    movingTabs = false

    new CompileAllEvent().raise(mainCodeTab)

    App.app.setWindowTitles()
  }

  def reload() {
    if (!reloading) {
      reloading = true
      workspace.reload
    }
  }

  def handle(e: LoadBeginEvent) {
    if (!reloading) {
      getExternalFileTabs.foreach(closeExternalTab)
      mainTabs.setSelectedComponent(interfaceTab)
    }
  }

  def handle(e: LoadModelEvent) {
    // We need to restart the watcher thread every load because the list of
    // included files may have changed.

    stopWatcherThread()

    if (getAutoReload) {
      startWatcherThread()

      getExternalFileTabs.foreach(_.reload)
    }

    reloading = false
  }

  def handle(e: LoadErrorEvent) {
    reloading = false
  }
  
  def handle(e: RuntimeErrorEvent) {
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

  def highlightRuntimeError(tab: CodeTab, e: RuntimeErrorEvent) {
    setSelectedTab(tab)

    tab.select(e.pos, e.pos + e.length)
  }

  def handle(e: CompiledEvent) {
    def clearErrors() {
      if (separateTabsWindow.isVisible) {
        for (i <- 0 until separateTabs.getTabCount)
          separateTabs.setForegroundAt(i, null)
      }

      else {
        for (i <- 2 until mainTabs.getTabCount)
          mainTabs.setForegroundAt(i, null)
      }
    }

    def recolorTab(tab: Component, hasError: Boolean): Unit = {
      val color = if (hasError) Color.red else null

      if (separateTabsWindow.isVisible && tab.isInstanceOf[CodeTab])
        separateTabs.setForegroundAt(separateTabs.indexOfComponent(tab), color)
      else
        mainTabs.setForegroundAt(mainTabs.indexOfComponent(tab), color)
      
      if (hasError && focusOnError) {
        setSelectedTab(tab)
      }
    }

    e.sourceOwner match {
      case `mainCodeTab` => {
        // on null error, clear all errors, as we only get one event for all the files. AAB 10/2020
        if (e.error == null)
          clearErrors()
        else
          recolorTab(mainCodeTab, true)
      }
      case file: ExternalFileInterface => {
        val filename = file.getFileName
        var tab = getTabWithFilename(Right(filename))
        if (!tab.isDefined && e.error != null) {
          openExternalFile(filename)
          tab = getTabWithFilename(Right(filename))
          tab.get.handle(e) // it was late to the party, let it handle the event too
        }
        recolorTab(tab.get, e.error != null)
      }
      case null => // i'm assuming this is only true when we've deleted that last widget. not a great sol'n - AZS 5/16/05
        recolorTab(interfaceTab, e.error != null)
      case jobWidget: JobWidget if !jobWidget.isCommandCenter =>
        recolorTab(interfaceTab, e.error != null)
      case _ =>
    }
  }

  def handle(e: AboutToSaveModelEvent) {
    // Stop watching here so that the watcher thread doesn't detect our own
    // write.
    stopWatcherThread()
  }

  def handle(e: ModelSavedEvent) =
    setWatchingFiles(getAutoReload, e.modelPath)

  def handle(e: ExternalFileSavedEvent) = {
    getTabWithFilename(Right(e.path)).foreach(tab => {
      if (separateTabsWindow.isVisible)
        separateTabs.setTitleAt(separateTabs.indexOfComponent(tab), tab.filenameForDisplay)
      else
        mainTabs.setTitleAt(mainTabs.indexOfComponent(tab), tab.filenameForDisplay)
    })
  }

  def handle(e: AboutToCloseFilesEvent) =
    OfferSaveExternalsDialog.offer(getExternalFileTabs.filter(_.saveNeeded).toSet, workspace.getFrame)
}
