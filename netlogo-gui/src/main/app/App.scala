// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ Dimension, EventQueue, Frame, GraphicsEnvironment, KeyboardFocusManager, Toolkit, BorderLayout}
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.{ DropTarget, DropTargetDragEvent, DropTargetDropEvent, DropTargetEvent, DropTargetListener }
import java.awt.event.ActionEvent
import java.io.File
import java.util.{ List => JList }
import javax.swing.{ JFrame, JMenu }

import scala.concurrent.ExecutionContext

import org.nlogo.agent.{ Agent, World2D, World3D }
import org.nlogo.analytics.Analytics
import org.nlogo.api._
import org.nlogo.app.codetab.{ ExternalFileManager, TemporaryCodeTab }
import org.nlogo.app.common.{ CodeToHtml, Events => AppEvents, FileActions, FindDialog }
import org.nlogo.app.interfacetab.{ CommandCenter, InterfaceTab, InterfaceWidgetControls, WidgetPanel }
import org.nlogo.app.tools.{ AgentMonitorManager, GraphicsPreview, LibraryManagerErrorDialog, PreviewCommandsEditor }
import org.nlogo.awt.UserCancelException
import org.nlogo.core.{ AgentKind, CompilerException, ExternalResource, I18N, Model, ModelSettings, NetLogoPreferences,
  Shape, Widget => CoreWidget }, Shape.{ LinkShape, VectorShape }
import org.nlogo.log.{ JsonFileLogger, LogEvents, LogManager }
import org.nlogo.nvm.{ PresentationCompilerInterface, Workspace }
import org.nlogo.shape.{ LinkShapesManagerInterface, ShapesManagerInterface, TurtleShapesManagerInterface }
import org.nlogo.swing.{ DropdownOptionPane, InputOptionPane, OptionPane, SetSystemLookAndFeel, UserAction, Utils },
  UserAction.{ ActionCategoryKey, EditCategory, FileCategory, HelpCategory, MenuAction, ToolsCategory }
import org.nlogo.theme.{ ClassicTheme, DarkTheme, InterfaceColors, LightTheme, ThemeSync }
import org.nlogo.util.{ NullAppHandler, Pico }
import org.nlogo.window._
import org.nlogo.window.Events._
import org.nlogo.workspace.{ AbstractWorkspace, AbstractWorkspaceScala, Controllable, HubNetManagerFactory, WorkspaceFactory }

import org.picocontainer.parameters.{ ComponentParameter, ConstantParameter }
import org.picocontainer.Parameter

import scala.jdk.CollectionConverters.ListHasAsScala

/**
 * The main class for the complete NetLogo application.
 *
 * <p>All methods in this class, including the constructor,
 * <strong>must</strong> be called from
 * the AWT event queue thread, unless otherwise specified.
 *
 * <p>See the "Controlling" section of the NetLogo User Manual
 * for example code.
 */
object App {
  private val pico = new Pico()
  // all these guys are assigned in main. yuck
  var app: App = null
  private var commandLineModelIsLaunch = false
  private var commandLineModel: String = null
  private var commandLineMagic: String = null
  private var commandLineURL: String = null
  private var logEvents: String = null
  private var logDirectory: String = null
  private var popOutCodeTab = false
  private var colorTheme: String = null
  /**
   * Should be called once at startup to create the application and
   * start it running.  May not be called more than once.  Once
   * this method has called, the singleton instance of this class
   * is stored in <code>app</code>.
   *
   * <p>This method must <strong>not</strong> be called from the AWT event
   * queue thread.
   *
   * @param args Should be empty. (Passing non-empty arguments
   *             is not currently documented.)
   */
  def main(args: Array[String]): Unit = {
    mainWithAppHandler(args, NullAppHandler)
  }

  def mainWithAppHandler(args: Array[String], appHandler: Object): Unit = {
    val lowerArgs = args.map(_.trim.toLowerCase)
    if (lowerArgs.contains("--headless") || lowerArgs.contains("--help")) {
      org.nlogo.headless.Main.main(args)
      return
    }

    try {
      val scalePref = NetLogoPreferences.getDouble("uiScale", 1.0)

      if (scalePref > 1.0) {
        System.setProperty("sun.java2d.uiScale", scalePref.toString)

        Utils.setUIScale(scalePref)
      } else {
        val devices = GraphicsEnvironment.getLocalGraphicsEnvironment.getScreenDevices
        val scale = devices(0).getDefaultConfiguration.getDefaultTransform.getScaleX

        Utils.setUIScale(scale)
      }

      // this call is reflective to avoid complicating dependencies
      appHandler.getClass.getDeclaredMethod("init").invoke(appHandler)

      AbstractWorkspace.isApp(true)
      org.nlogo.window.VMCheck.detectBadJVMs()
      processCommandLineArguments(args)
      Splash.beginSplash() // also initializes AWT
      pico.add("org.nlogo.compile.Compiler")
      if (Version.is3D)
        pico.addScalaObject("org.nlogo.api.NetLogoThreeDDialect")
      else
        pico.addScalaObject("org.nlogo.api.NetLogoLegacyDialect")

      if (Version.systemDynamicsAvailable) {
        pico.add("org.nlogo.sdm.gui.NLogoGuiSDMFormat")
        pico.add("org.nlogo.sdm.gui.NLogoThreeDGuiSDMFormat")
      }
      pico.addScalaObject("org.nlogo.sdm.gui.SDMGuiAutoConvertable")

      pico.addAdapter(new Adapters.AnyModelLoaderComponent())

      pico.addAdapter(new Adapters.ModelConverterComponent())

      pico.addComponent(classOf[CodeToHtml])
      pico.addComponent(classOf[App])
      pico.addComponent(classOf[ModelSaver])
      pico.add("org.nlogo.gl.view.ViewManager")
      // Anything that needs a parent Frame, we need to use ComponentParameter
      // and specify classOf[AppFrame], otherwise PicoContainer won't know which
      // Frame to use - ST 6/16/09
      pico.add(classOf[TurtleShapesManagerInterface],
            "org.nlogo.shape.editor.TurtleShapeManagerDialog",
            Array[Parameter] (
              new ComponentParameter(classOf[AppFrame]),
              new ComponentParameter(), new ComponentParameter()))
      pico.add(classOf[LinkShapesManagerInterface],
            "org.nlogo.shape.editor.LinkShapeManagerDialog",
            Array[Parameter] (
              new ComponentParameter(classOf[AppFrame]),
              new ComponentParameter(), new ComponentParameter()))
      pico.add("org.nlogo.lab.gui.LabManager")
      pico.addComponent(classOf[EditDialogFactory])
      // we need to make HeadlessWorkspace objects for BehaviorSpace to use.
      // HeadlessWorkspace uses picocontainer too, but it could get confusing
      // to use the same container in both places, so I'm going to keep the
      // containers separate and just use Plain Old Java Reflection to
      // call HeadlessWorkspace's newInstance() method. - ST 3/11/09
      // And we'll conveniently reuse it for the preview commands editor! - NP 2015-11-18
      val factory = new WorkspaceFactory {
        def newInstance: AbstractWorkspaceScala =
          Class.forName("org.nlogo.headless.HeadlessWorkspace")
            .getMethod("newInstance").invoke(null).asInstanceOf[AbstractWorkspaceScala]
        def openCurrentModelIn(w: Workspace): Unit = {
          w.setModelPath(app.workspace.getModelPath)
          w.openModel(pico.getComponent(classOf[ModelSaver]).currentModelInCurrentVersion)
        }
      }

      pico.addComponent(classOf[WorkspaceFactory], factory)
      pico.addComponent(classOf[GraphicsPreview])
      pico.addComponent(classOf[ExternalFileManager])
      pico.add(
        classOf[PreviewCommandsEditorInterface],
        "org.nlogo.app.tools.PreviewCommandsEditor",
        new ComponentParameter(classOf[AppFrame]),
        new ComponentParameter(), new ComponentParameter())
      pico.add(classOf[MainMenuBar], "org.nlogo.app.MainMenuBar",
        new ConstantParameter(AbstractWorkspace.isApp))
      pico.add(classOf[CommandCenter], "org.nlogo.app.interfacetab.CommandCenter", new ComponentParameter(),
               new ConstantParameter(true))
      pico.add("org.nlogo.app.interfacetab.InterfaceTab")
      pico.addComponent(classOf[AgentMonitorManager])

      System.setProperty("flatlaf.menuBarEmbedded", "false")
      System.setProperty("sun.awt.noerasebackground", "true") // stops view2.5d and 3d windows from blanking to white until next interaction

      app = pico.getComponent(classOf[App])

      pico.add(classOf[AggregateManagerInterface],
        "org.nlogo.sdm.gui.GUIAggregateManager",
        Array[Parameter] (
          new ComponentParameter(classOf[AppFrame]),
          new ComponentParameter(), new ComponentParameter(),
          new ComponentParameter(), new ComponentParameter(),
          new ConstantParameter(app.workspace.getExtensionManager)))

      // It's pretty silly, but in order for the splash screen to show up
      // for more than a fraction of a second, we want to initialize as
      // much stuff as we can from main() before handing off to the event
      // thread.  So what happens in the App constructor and what happens
      // in finishStartup() is pretty arbitrary -- it's whatever makes
      // the splash screen come up early without getting a bunch of Java
      // exceptions because we're doing too much on the main thread.
          // Hey, it's important to make a good first impression.
      //   - ST 8/19/03
      org.nlogo.awt.EventQueue.invokeAndWait(() => app.finishStartup(appHandler))
    }
    catch {
      case ex: java.lang.Throwable =>
        StartupError.report(ex)
        return
    }

  }

  private def processCommandLineArguments(args: Array[String]): Unit = {
    def printAndExit(s: String): Unit = {
      println(s)
      sys.exit(0)
    }

    // note: this method is static so that it can be called from main()
    // before App is instantiated, which means we can use the --version
    // flags without the AWT ever being initialized, which is handy when
    // we're at the command line and need the version but can't do GUI stuff
    // (e.g. if we're on Linux but don't have the DISPLAY environment variable
    // set) - ST 4/1/02
    var i = 0
    def nextToken() = { val t = args(i); i += 1; t }
    def moreTokens = i < args.length
    while(moreTokens){
      val token = nextToken()
      if (token == "--events") org.nlogo.window.Event.logEvents = true;
      else if (token == "--open" || token == "--launch") {
        commandLineModelIsLaunch = token == "--launch"
        require(commandLineModel == null &&
                commandLineMagic == null &&
                commandLineURL == null,
          "Error parsing command line arguments: you can only specify one model to open at startup.")
        val fileToken = nextToken()
        val modelFile = new java.io.File(fileToken)
        // Best to check if the file exists here, because after the GUI thread has started,
        // NetLogo just hangs with the splash screen showing if file doesn't exist. ~Forrest (2/12/2009)
        if (!modelFile.exists) throw new IllegalStateException(I18N.errors.getN("fileformat.notFound", fileToken))
        commandLineModel = modelFile.getAbsolutePath()
      }
      else if (token == "--magic") {
        require(commandLineModel == null &&
                commandLineMagic == null &&
                commandLineURL == null)
        commandLineMagic = nextToken()
      }
      else if (token == "--url") {
        require(commandLineModel == null &&
                commandLineMagic == null &&
                commandLineURL == null)
        commandLineURL = nextToken()
      }
      else if (token == "--color-theme") {
        colorTheme = nextToken()

        colorTheme match {
          case "classic" =>
          case "light" =>
          case _ => throw new IllegalArgumentException(I18N.errors.getN("themes.unknown", colorTheme))
        }
      }
      else if (token == "--version") printAndExit(Version.version)
      else if (token == "--extension-api-version") printAndExit(APIVersion.version)
      else if (token == "--builddate") printAndExit(Version.buildDate)
      else if (token == "--log-events") logEvents = nextToken()
      else if (token == "--log-directory") logDirectory = nextToken()
      else if (token == "--codetab-window") popOutCodeTab = true
      else if (token == "--3d") System.setProperty("org.nlogo.is3d", "true")
      else if (token.startsWith("--")) {
        //TODO: Decide: should we do System.exit() here?
        // Previously we've just ignored unknown parameters, but that seems wrong to me.  ~Forrest (2/12/2009)
        System.err.println("Error: Unknown command line argument: " + token)
      }
      else { // we assume it's a filename to "launch"
        commandLineModelIsLaunch = true
        require(commandLineModel == null &&
                commandLineMagic == null &&
                commandLineURL == null,
          "Error parsing command line arguments: you can only specify one model to open at startup.")
        val modelFile = new java.io.File(token)
        // Best to check if the file exists here, because after the GUI thread has started,
        // NetLogo just hangs with the splash screen showing if file doesn't exist. ~Forrest (2/12/2009)
        if (!modelFile.exists())
          throw new IllegalStateException(I18N.errors.getN("fileformat.notFound", token))
        commandLineModel = modelFile.getAbsolutePath()
      }
    }
  }
}

class App extends org.nlogo.window.Event.LinkChild
  with org.nlogo.api.Exceptions.Handler
  with AppEvent.Handler
  with BeforeLoadEvent.Handler
  with LoadBeginEvent.Handler
  with LoadEndEvent.Handler
  with LoadModelEvent.Handler
  with ModelSavedEvent.Handler
  with ModelSections
  with AppEvents.SwitchedTabsEvent.Handler
  with AppEvents.OpenLibrariesDialogEvent.Handler
  with AboutToQuitEvent.Handler
  with ZoomedEvent.Handler
  with Controllable {

  import App.{ pico, commandLineMagic, commandLineModel, commandLineURL, commandLineModelIsLaunch, logDirectory, logEvents, popOutCodeTab }
  val frame = new AppFrame
  def getFrame = frame

  // all these guys get set in the locally block
  private var _workspace: GUIWorkspace = null
  def workspace = _workspace
  lazy val owner = new SimpleJobOwner("App", workspace.world.mainRNG, AgentKind.Observer)
  private var _tabManager: TabManager = null
  def tabManager = _tabManager
  var menuBar: MainMenuBar = null
  var _fileManager: FileManager = null
  var monitorManager: AgentMonitorManager = null
  def getMonitorManager = monitorManager
  var aggregateManager: AggregateManagerInterface = null
  var dirtyMonitor: DirtyMonitor = null
  var labManager: LabManagerInterface = null
  var recentFilesMenu: RecentFilesMenu = null
  private var errorDialogManager: ErrorDialogManager = null
  private val listenerManager = new NetLogoListenerManager
  private var syncComponents = Set[ThemeSync]()
  private val runningInMacWrapper = Option(System.getProperty("org.nlogo.mac.appClassName")).nonEmpty
  private val ImportWorldURLProp = "netlogo.world_state_url"
  private val ImportRawWorldURLProp = "netlogo.raw_world_state_url"

  val isMac = System.getProperty("os.name").startsWith("Mac")

  private val analyticsConsent = NetLogoPreferences.get("sendAnalytics", "$$$") == "$$$"

  /**
   * Quits NetLogo by exiting the JVM.  Asks user for confirmation first
   * if they have unsaved changes. If the user confirms, calls System.exit(0).
   */
  // part of controlling API; used by e.g. the Mathematica-NetLogo link
  // - ST 8/21/07
  @throws(classOf[UserCancelException])
  def quit(): Unit ={ fileManager.quit() }

  locally {
    frame.addLinkComponent(this)
    pico.addComponent(frame)

    if (App.colorTheme == null)
      App.colorTheme = NetLogoPreferences.get("colorTheme", "light")

    SetSystemLookAndFeel.setSystemLookAndFeel()

    InterfaceColors.setTheme(App.colorTheme match {
      case "classic" => ClassicTheme
      case "light" => LightTheme
      case "dark" => DarkTheme
    })

    errorDialogManager = new ErrorDialogManager(frame,
      Map(classOf[MetadataLoadingException] -> new LibraryManagerErrorDialog(frame)))

    org.nlogo.api.Exceptions.setHandler(this)
    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      def uncaughtException(t: Thread, e: Throwable): Unit = { org.nlogo.api.Exceptions.handle(e) }
    })

    val interfaceFactory = new InterfaceFactory() {
      def widgetPanel(workspace: GUIWorkspace): AbstractWidgetPanel =
        new WidgetPanel(workspace)
      def widgetControls(wp: AbstractWidgetPanel, workspace: GUIWorkspace, buttons: List[WidgetInfo], frame: Frame) = {
        new InterfaceWidgetControls(wp.asInstanceOf[WidgetPanel], workspace, buttons, frame,
          pico.getComponent(classOf[EditDialogFactory]))
      }
    }
    pico.add(classOf[HubNetManagerFactory], "org.nlogo.hubnet.server.gui.HubNetManagerFactory",
          Array[Parameter] (
            new ComponentParameter(classOf[AppFrame]), new ComponentParameter(), new ComponentParameter()))
    pico.addComponent(interfaceFactory)
    pico.addComponent(new MenuBarFactory())

    val controlSet = new AppControlSet()

    val world = if(Version.is3D) new World3D() else new World2D()

    pico.addComponent(world)
    _workspace = new GUIWorkspace(world,
      GUIWorkspace.KioskLevel.None,
      frame,
      frame,
      pico.getComponent(classOf[HubNetManagerFactory]),
      pico.getComponent(classOf[ExternalFileManager]),
      listenerManager,
      errorDialogManager,
      controlSet) {
      val compiler = pico.getComponent(classOf[PresentationCompilerInterface])
      // lazy to avoid initialization order snafu - ST 3/1/11
      lazy val updateManager = new UpdateManager {
        override def defaultFrameRate = _workspace.frameRate
        override def ticks = _workspace.world.tickCounter.ticks
        override def updateMode = _workspace.updateMode
      }

      def aggregateManager: AggregateManagerInterface = App.this.aggregateManager
      def inspectAgent(agent: org.nlogo.api.Agent, radius: Double): Unit = {
        val a = agent.asInstanceOf[org.nlogo.agent.Agent]
        monitorManager.inspect(a.kind, a, radius)
      }
      override def inspectAgent(agentClass: AgentKind, agent: Agent, radius: Double): Unit = {
        monitorManager.inspect(agentClass, agent, radius)
      }
      override def stopInspectingAgent(agent: Agent): Unit = {
        monitorManager.stopInspecting(agent)
      }
      override def stopInspectingDeadAgents(): Unit = {
        monitorManager.stopInspectingDeadAgents()
      }
      override def closeAgentMonitors(): Unit = { monitorManager.closeAll() }
      override def newRenderer: RendererInterface = {
        // yikes, it's really ugly that we do this stuff
        // way inside here (should be top level). not sure
        // what to do about it - ST 3/2/09, 3/4/09
        if (pico.getComponent(classOf[GUIWorkspace]) == null) {
          pico.addComponent(this)
          pico.add("org.nlogo.render.Renderer")
        }
        pico.getComponent(classOf[RendererInterface])
      }

      def updateModel(model: Model): Model = {
        model.withOptionalSection("org.nlogo.modelsection.modelsettings", Some(ModelSettings(snapOn)), Some(ModelSettings(false)))
      }

      override def dispose(): Unit = super.dispose()
    }

    ShapeChangeListener.listen(_workspace, world)

    pico.addComponent(new EditorColorizer(workspace))

    frame.addLinkComponent(workspace)

    frame.addLinkComponent({
      val libMan = workspace.getLibraryManager
      new ExtensionAssistant(
        frame
      , libMan.getExtensionInfos.map(_.codeName).toSet
      , (name) => libMan.lookupExtension(name, "").fold("N/A")(_.version)
      , { (name, version) => libMan.lookupExtension(name, version).foreach(libMan.installExtension); compileLater() }
      )
    })

    monitorManager = pico.getComponent(classOf[AgentMonitorManager])
    frame.addLinkComponent(monitorManager)

    _tabManager = new TabManager(workspace, pico.getComponent(classOf[InterfaceTab]),
                                 pico.getComponent(classOf[ExternalFileManager]))

    frame.addLinkComponent(_tabManager)
    controlSet.interfaceTab = Some(_tabManager.interfaceTab)

    pico.addComponent(_tabManager.interfaceTab.getInterfacePanel)
    frame.getContentPane.add(_tabManager.mainTabs, BorderLayout.CENTER)

    frame.addLinkComponent(new CompilerManager(workspace, world, _tabManager.mainCodeTab))
    frame.addLinkComponent(listenerManager)

    def switchOrPref(switchValue: String, prefName: String, default: String) = {
      if (switchValue != null && !switchValue.trim().equals("")) {
        switchValue
      } else {
        NetLogoPreferences.get(prefName, default)
      }
    }

    if (logDirectory != null || logEvents != null || NetLogoPreferences.get("loggingEnabled", "false").toBoolean) {
      val finalLogDirectory = new File(switchOrPref(logDirectory, "logDirectory", System.getProperty("user.home")))
      val eventsString      = switchOrPref(logEvents, "logEvents", "")
      val events            = LogEvents.parseEvents(eventsString)
      val studentName       = askForName()
      val addListener       = (l) => listenerManager.addListener(l)
      val loggerFactory     = (p) => new JsonFileLogger(p)
      LogManager.start(addListener, loggerFactory, finalLogDirectory, events, studentName, () =>
        new OptionPane(frame, I18N.gui.get("common.messages.warning"), I18N.gui.get("error.dialog.logDirectory"),
                       OptionPane.Options.Ok, OptionPane.Icons.Warning))
    }

  }

  private def finishStartup(appHandler: Object): Unit = {
    try {
      val app = pico.getComponent(classOf[App])

      aggregateManager = pico.getComponent(classOf[AggregateManagerInterface])
      frame.addLinkComponent(aggregateManager)

      labManager = pico.getComponent(classOf[LabManagerInterface])
      frame.addLinkComponent(labManager)

      val titler = (file: Option[String]) => { file map externalFileTitle getOrElse modelTitle() }
      pico.add(classOf[DirtyMonitor], "org.nlogo.app.DirtyMonitor",
        new ComponentParameter, new ComponentParameter, new ComponentParameter, new ComponentParameter,
        new ConstantParameter(titler), new ConstantParameter(_tabManager.separateTabsWindow))
      dirtyMonitor = pico.getComponent(classOf[DirtyMonitor])
      frame.addLinkComponent(dirtyMonitor)

      val menuBar = pico.getComponent(classOf[MainMenuBar])

      pico.add(classOf[FileManager],
        "org.nlogo.app.FileManager",
        new ComponentParameter(), new ComponentParameter(), new ComponentParameter(),
        new ComponentParameter(), new ComponentParameter(),
        new ConstantParameter(menuBar), new ConstantParameter(frame), new ConstantParameter(_tabManager))
      setFileManager(pico.getComponent(classOf[FileManager]))

      val viewManager = pico.getComponent(classOf[GLViewManagerInterface])
      workspace.init(viewManager)
      frame.addLinkComponent(viewManager)

      app.setMenuBar(menuBar)
      frame.setJMenuBar(menuBar)

      _tabManager.init(fileManager, dirtyMonitor, menuBar, allActions)

      FindDialog.init(frame, _tabManager.separateTabsWindow)

      // OK, this is a little kludgy.  First we pack so everything
      // is realized, and all addNotify() methods are called.  But
      // the actual size we get won't be right yet, because the
      // default model hasn't been loaded.  So load it, then pack
      // again.  The first pack is needed because until everything
      // has been realized, the NetLogo event system won't work.
      //  - ST 8/16/03
      frame.pack()

      loadDefaultModel()

      _tabManager.interfaceTab.packSplitPane()

      smartPack(frame.getPreferredSize, true)

      _tabManager.interfaceTab.resetSplitPane()

      if (! isMac) { org.nlogo.awt.Positioning.center(frame, null) }

      new DropTarget(frame.getContentPane, new DropTargetListener {
        def dragEnter(e: DropTargetDragEvent): Unit = {}
        def dragExit(e: DropTargetEvent): Unit = {}
        def dragOver(e: DropTargetDragEvent): Unit = {}

        def drop(e: DropTargetDropEvent): Unit = {
          if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            e.acceptDrop(e.getDropAction)

            val files = e.getTransferable.getTransferData(DataFlavor.javaFileListFlavor)
                         .asInstanceOf[JList[File]].asScala

            if (files.size == 1) {
              val path = files(0).toString

              if (path.endsWith("." + ModelReader.modelSuffix)) {
                open(path)

                e.dropComplete(true)
              } else {
                e.dropComplete(false)
              }
            } else {
              e.dropComplete(false)
            }
          } else {
            e.rejectDrop()
          }
        }

        def dropActionChanged(e: DropTargetDragEvent): Unit = {}
      })

      Splash.endSplash()
      frame.setVisible(true)
      if (isMac) {
        appHandler.getClass.getDeclaredMethod("ready", classOf[AnyRef]).invoke(appHandler, this)
      }

      frame.addLinkComponent(_tabManager.separateTabsWindow)

      if (popOutCodeTab || NetLogoPreferences.getBoolean("startSeparateCodeTab", false)) {
        _tabManager.switchWindow(true)
      }

      import ExecutionContext.Implicits.global
      AnnouncementsInfoDownloader.fetch().foreach(_tabManager.interfaceTab.appendAnnouncements)

      _tabManager.interfaceTab.requestFocus()

      syncWindowThemes()

      if (analyticsConsent) {
        NetLogoPreferences.putBoolean("sendAnalytics",
          new OptionPane(frame, I18N.gui.get("dialog.analyticsConsent"),
                         I18N.gui.get("dialog.analyticsConsent.message"), OptionPane.Options.YesNo,
                         OptionPane.Icons.Info).getSelectedIndex == 0)
      }

      Analytics.refreshPreference()
      Analytics.appStart(Version.is3D)
    }
    catch {
      case ex: java.lang.Throwable =>
        StartupError.report(ex)
        return
    }

  }

  // This is for other windows to get their own copy of the menu
  // bar.  It's needed especially for OS X since the screen menu bar
  // doesn't get shared across windows.  -- AZS 6/17/2005
  private class MenuBarFactory extends org.nlogo.window.MenuBarFactory {
    def actions = allActions ++ _tabManager.permanentMenuActions

    def createMenu(newMenu: org.nlogo.swing.Menu, category: String): JMenu = {
      actions.filter(_.getValue(ActionCategoryKey) == category).foreach(newMenu.offerAction)
      newMenu
    }

    def createEditMenu:  JMenu = createMenu(new EditMenu,  EditCategory)
    def createFileMenu:  JMenu = createMenu(new FileMenu,  FileCategory)
    def createHelpMenu:  JMenu = createMenu(new HelpMenu,  HelpCategory)
    def createToolsMenu: JMenu = createMenu(new ToolsMenu, ToolsCategory)
    def createZoomMenu:  JMenu = new ZoomMenu
  }

  private def loadDefaultModel(): Unit = {
    if (commandLineModel != null) {
      if (commandLineModelIsLaunch) { // --launch through InstallAnywhere?
        // open up the blank model first so in case
        // the magic open fails for some reason
        // there's still a model loaded ev 3/7/06
        fileManager.newModel()
        open(commandLineModel)
      }
      else {
        libraryOpen(commandLineModel) // --open from command line
      }

    } else if (commandLineMagic != null) {
      workspace.magicOpen(commandLineMagic)

    } else if (commandLineURL != null) {
      try {
        fileManager.openFromURI(new java.net.URI(commandLineURL), ModelType.Library)

        import org.nlogo.awt.EventQueue

        Option(System.getProperty(ImportRawWorldURLProp)) map {
          url => // `io.Source.fromURL(url).bufferedReader` steps up to bat and... manages to fail gloriously here! --JAB (8/22/12)
            import java.io.{ BufferedReader, InputStreamReader }, java.net.URL
            EventQueue.invokeLater {
              () =>
                workspace.importWorld(new BufferedReader(new InputStreamReader(new URL(url).openStream())))
                workspace.view.dirty()
                workspace.view.repaint()
            }
        } orElse (Option(System.getProperty(ImportWorldURLProp)) map {
          url =>

            import java.util.zip.GZIPInputStream, java.io.{ ByteArrayInputStream, InputStreamReader }, scala.io.{ Codec, Source }

            val source = Source.fromURL(url)(using Codec.ISO8859)
            val bytes  = source.map(_.toByte).toArray
            val bais   = new ByteArrayInputStream(bytes)
            val gis    = new GZIPInputStream(bais)
            val reader = new InputStreamReader(gis)

            EventQueue.invokeLater {
              () => {
                workspace.importWorld(reader)
                workspace.view.dirty()
                workspace.view.repaint()
                source.close()
                bais.close()
                gis.close()
                reader.close()
              }
            }

        })
      }
      catch {
        case ex: java.net.ConnectException =>
          fileManager.newModel()
          new OptionPane(frame, I18N.gui.get("file.open.error.unloadable.title"),
                         I18N.gui.getN("file.open.error.unloadable.message", commandLineURL),
                         OptionPane.Options.OkCancel, OptionPane.Icons.Warning)
      }

    } else if (NetLogoPreferences.get("loadLastOnStartup", "false").toBoolean) {
      // if recent list is empty we need the new model, or if loading the recent model
      // fails then we'll fall back on it.  -Jeremy B June 2021
      fileManager.newModel()
      val recentFiles = (new RecentFiles).models
      if (!recentFiles.isEmpty) {
        val modelEntry = recentFiles.head
        fileManager.openFromPath(modelEntry.path, modelEntry.modelType)
      }
    } else {
      fileManager.newModel()
    }
  }

  /// zooming

  def handle(e: ZoomedEvent): Unit = {
    smartPack(frame.getPreferredSize, false)
  }

  def resetZoom(): Unit = {
    new ZoomedEvent(0).raise(this)
  }

  lazy val openPreferencesDialog = new ShowPreferencesDialog(frame, _tabManager, _tabManager.interfaceTab.iP)

  lazy val openAboutDialog = new ShowAboutWindow(frame)

  lazy val openRGBAColorDialog = new OpenRGBAColorDialog(frame)

  lazy val openLibrariesDialog = {
    val updateSource =
      (transform: (String) => String) =>
        _tabManager.mainCodeTab.innerSource = transform(_tabManager.mainCodeTab.innerSource)
    new OpenLibrariesDialog( frame, workspace.getLibraryManager, () => compile()
                           , updateSource, () => workspace.getExtensionPathMappings())
  }

  lazy val allActions: Seq[MenuAction] = {
    // If we're running in the mac wrapper, it takes care of displaying these
    // items for us - RG 2/26/18
    val osSpecificActions =
      if (runningInMacWrapper) Seq() else Seq(openPreferencesDialog, openAboutDialog)

    val workspaceActions = WorkspaceActions(workspace)

    val generalActions: Seq[MenuAction] = Seq(
      openLibrariesDialog,
      openRGBAColorDialog,
      new ShowShapeManager("turtleShapesEditor", turtleShapesManager),
      new ShowShapeManager("linkShapesEditor",   linkShapesManager),
      new ShowSystemDynamicsModeler(aggregateManager),
      new OpenHubNetClientEditor(workspace, frame),
      workspace.hubNetControlCenterAction,
      new PreviewCommandsEditor.EditPreviewCommands(
        pico.getComponent(classOf[PreviewCommandsEditorInterface]),
        workspace,
        () => pico.getComponent(classOf[ModelSaver]).asInstanceOf[ModelSaver].currentModel),
      FindDialog.FIND_ACTION,
      FindDialog.FIND_NEXT_ACTION,
      new ConvertWidgetSizes(frame, _tabManager.interfaceTab.iP)
    ) ++
    HelpActions.apply ++
    FileActions(workspace, menuBar.fileMenu) ++
    workspaceActions ++
    labManager.actions ++
    fileManager.actions

    osSpecificActions ++ generalActions
  }

  def setMenuBar(menuBar: MainMenuBar): Unit = {
    if (menuBar != this.menuBar) {
      this.menuBar = menuBar
      allActions.foreach(menuBar.offerAction)
      Option(recentFilesMenu).foreach(_.setMenu(menuBar))
    }
  }

  def fileManager: FileManager = _fileManager

  def setFileManager(manager: FileManager): Unit = {
    if (manager != _fileManager) {
      _fileManager = manager
      frame.addLinkComponent(manager)
      recentFilesMenu = new RecentFilesMenu(frame, manager)
      frame.addLinkComponent(recentFilesMenu)
    }
  }

  // used by external tools to make GUI automation smoother (Isaac B 3/13/25)
  def setIgnorePopups(ignore: Boolean): Unit = {
    tabManager.setIgnoreChanges(ignore)
  }

  // AppEvent stuff (kludgy)
  /**
   * Internal use only.
   */
  def handle(e: AppEvent): Unit = {
    import AppEventType._
    e.`type` match {
      case RELOAD => reload()
      case MAGIC_OPEN => magicOpen(e.args(0).toString)
    }
  }

  private def reload(): Unit = {
    val modelType = workspace.getModelType
    val path = workspace.getModelPath
    if (modelType != ModelType.New && path != null) tryOpenFromSource(path, modelType)
    else commandLater("print \"can't, new model\"")
  }

  private def tryOpenFromSource(path: String, modelType: ModelType): Unit = {
    val source = FileIO.fileToString(path)
    // sometimes the filesystem does weird things and the file is empty for a moment,
    // so we keep trying until the file contents are resolved (Isaac B 4/22/25)
    if (source.isEmpty) {
      EventQueue.invokeLater(() => tryOpenFromSource(path, modelType))
    } else {
      openFromSource(source, path, modelType)
    }
  }

  private def magicOpen(name: String): Unit = {
    val matches = org.nlogo.workspace.ModelsLibrary.findModelsBySubstring(name)
    if (matches.isEmpty) commandLater("print \"no models matching \\\"" + name + "\\\" found\"")
    else {
      val fullName =
        if (matches.size == 1) {
          Some(matches(0))
        } else {
          new DropdownOptionPane(frame, I18N.gui.get("tools.magicModelMatcher"),
                                 I18N.gui.get("tools.magicModelMatcher.mustChoose"),
                                 matches.map(_.replace(".nlogox3d", "")
                                              .replace(".nlogox", "")))
            .getSelectedChoice
        }
      fullName.foreach(name => {
        org.nlogo.workspace.ModelsLibrary.getModelPath(name).foreach { path =>
          val source = FileIO.fileToString(path)
          org.nlogo.awt.EventQueue.invokeLater(() => openFromSource(source, path, ModelType.Library))
        }
      })
    }
  }

  ///

  def setWindowTitles(): Unit = {
    if (tabManager.separateTabsWindow.isVisible) {
      frame.setTitle(modelTitle())
      tabManager.separateTabsWindow.setTitle(
        tabManager.separateTabs.getSelectedComponent match {
          case tempTab: TemporaryCodeTab => externalFileTitle(tempTab.filename.merge)
          case _ => modelTitle()
        }
      )
    } else {
      frame.setTitle(
        tabManager.mainTabs.getSelectedComponent match {
          case tempTab: TemporaryCodeTab => externalFileTitle(tempTab.filename.merge)
          case _ => modelTitle()
        }
      )
    }
  }

  /// ThemeSync stuff

  def addSyncComponent(ts: ThemeSync): Unit = {
    syncComponents = syncComponents + ts
  }

  def removeSyncComponent(ts: ThemeSync): Unit = {
    syncComponents = syncComponents - ts
  }

  def syncWindowThemes(): Unit = {
    FindDialog.syncTheme()

    menuBar.syncTheme()

    tabManager.syncTheme()
    frame.repaint()
    tabManager.separateTabsWindow.repaint()

    workspace.glView.syncTheme()
    workspace.glView.repaint()

    monitorManager.syncTheme()

    _turtleShapesManager match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }

    _linkShapesManager match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }

    labManager.syncTheme()

    openPreferencesDialog.syncTheme()
    openAboutDialog.syncTheme()
    openRGBAColorDialog.syncTheme()
    openLibrariesDialog.syncTheme()

    workspace.hubNetManager match {
      case Some(manager: ThemeSync) => manager.syncTheme()
      case _ =>
    }

    aggregateManager match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }

    errorDialogManager.syncTheme()

    // this allows external objects like extension GUIs to sync with the theme (Isaac B 1/12/25)
    syncComponents.foreach(_.syncTheme())
  }

  /**
   * Internal use only.
   */
  final def handle(e: AppEvents.SwitchedTabsEvent): Unit = {
    if (e.newTab == _tabManager.interfaceTab) {
      monitorManager.showAll()
      frame.toFront()
    } else if (e.oldTab == _tabManager.interfaceTab) {
      monitorManager.hideAll()
    }
    setWindowTitles()
  }

  /**
   * Internal use only.
   */
  def handle(e: AppEvents.OpenLibrariesDialogEvent): Unit =
    openLibrariesDialog.actionPerformed(null)

  /**
   * Internal use only.
   */
  def handle(e: ModelSavedEvent): Unit = {
    workspace.modelSaved(e.modelPath)
    errorDialogManager.setModelName(workspace.modelNameForDisplay)
    if (AbstractWorkspace.isApp) {
      setWindowTitles()
      workspace.hubNetManager.foreach { manager =>
        manager.setTitle(workspace.modelNameForDisplay, workspace.getModelDir, workspace.getModelType)
      }
    }
  }

  /**
   * Internal use only.
   */
  def handle(e: LoadBeginEvent): Unit = {
    val modelName = workspace.modelNameForDisplay
    errorDialogManager.setModelName(modelName)
    if (AbstractWorkspace.isApp)
      setWindowTitles()
    workspace.hubNetManager.foreach(_.closeClientEditor())
    errorDialogManager.closeAllDialogs()
  }

  private var wasAtPreferredSizeBeforeLoadBegan = false
  private var preferredSizeAtLoadEndTime: java.awt.Dimension = null

  /**
   * Internal use only.
   */
  def handle(e: BeforeLoadEvent): Unit = {
    wasAtPreferredSizeBeforeLoadBegan =
            preferredSizeAtLoadEndTime == null ||
            frame.getSize == preferredSizeAtLoadEndTime ||
            frame.getSize == frame.getPreferredSize
  }

  private lazy val _turtleShapesManager: ShapesManagerInterface = {
    pico.getComponent(classOf[TurtleShapesManagerInterface])
  }
  def turtleShapesManager: ShapesManagerInterface = _turtleShapesManager

  private lazy val _linkShapesManager: ShapesManagerInterface = {
    pico.getComponent(classOf[LinkShapesManagerInterface])
  }
  def linkShapesManager: ShapesManagerInterface = _linkShapesManager

  /**
   * Internal use only.
   */
  def handle(e: LoadEndEvent): Unit = {
    turtleShapesManager.reset()
    linkShapesManager.reset()
    workspace.view.repaint()

    if(AbstractWorkspace.isApp){
      // if we don't call revalidate() here we don't get up-to-date
      // preferred size information - ST 11/4/03
      _tabManager.interfaceTab.packSplitPane()
      if(wasAtPreferredSizeBeforeLoadBegan) smartPack(frame.getPreferredSize, true)
      else{
        val currentSize = frame.getSize
        val preferredSize = frame.getPreferredSize
        var newWidth = currentSize.width
        if(preferredSize.width > newWidth) newWidth = preferredSize.width
        var newHeight = currentSize.height
        if(preferredSize.height > newHeight) newHeight = preferredSize.height
        if(newWidth != currentSize.width || newHeight != currentSize.height) smartPack(new Dimension(newWidth, newHeight), true)
      }
      _tabManager.interfaceTab.resetSplitPane()
      preferredSizeAtLoadEndTime = frame.getPreferredSize()
    }

    if (KeyboardFocusManager.getCurrentKeyboardFocusManager.getActiveWindow != null) {
      frame.toFront()
      _tabManager.interfaceTab.requestFocus()
    }

    syncWindowThemes()
  }

  def handle(e: LoadModelEvent): Unit = {
    workspace.getResourceManager.setResources(e.model.resources)
  }

  /**
   * Internal use only.
   */
  def handle(e: AboutToQuitEvent): Unit = {
    LogManager.stop()
  }

  private def frameTitle(filename: String, dirty: Boolean) = {
    val title =
      // on OS X, use standard window title format. otherwise use Windows convention
      if(! System.getProperty("os.name").startsWith("Mac")) s"$filename - NetLogo"
      // 8212 is the unicode value for an em dash. we use the number since
      // we don't want non-ASCII characters in the source files -- AZS 6/14/2005
      else s"NetLogo ${8212.toChar} $filename"

    if (dirty) s"* $title" else title
  }

  private def modelTitle(allowDirtyMarker: Boolean = true) = {
    if (workspace.getModelFileName == null) "NetLogo"
    else {
      val title = frameTitle(workspace.modelNameForDisplay, allowDirtyMarker && dirtyMonitor.modelDirty)
      // OS X UI guidelines prohibit paths in title bars, but oh well...
      if (workspace.getModelType == ModelType.Normal) s"$title {${workspace.getModelDir}}" else title
    }
  }

  private def externalFileTitle(path: String) = {
    val filename = TemporaryCodeTab.stripPath(path)
    (_tabManager.getTabWithFilename(Right(path)) orElse _tabManager.getTabWithFilename(Left(path))).
      map (tab => frameTitle(filename, tab.saveNeeded)).
      getOrElse(frameTitle(filename, false))
  }

  /**
   * Internal use only.
   */
  def handle(t: Throwable): Unit = {
    try {
      val logo = t.isInstanceOf[LogoException]
      if (! logo) {
        t.printStackTrace(System.err)
        if (errorDialogManager.safeToIgnore(t)) {
          return
        }
      }
      // The PeriodicUpdate thread may throw errors over and
      // over if a dynamic slider or other code snippet
      // generates errors.  For this reason we don't want to
      // pop up the error dialog if it is already up.
      //  -- CLB 8/15/2006
      // else we ignore it, craig originally wanted to print to stdout
      // though, the code he wrote really printed to stderr.
      // Because we redirect stderr to a file in releases this was a
      // problem.  We could write our own stack trace printer (easily)
      // that actually prints to stdout but I'm not really sure that's
      // important. ev 2/25/08
      if (!errorDialogManager.alreadyVisible)
        org.nlogo.awt.EventQueue.invokeLater(() =>
          errorDialogManager.show(null, null, Thread.currentThread, t))
    } catch {
      case e2: RuntimeException => e2.printStackTrace(System.err)
    }
  }

  /// public methods for controlling (ModelCruncher uses them too)

  /**
   * Opens a model stored in a file.
   * @param path the path (absolute or relative) of the NetLogo model to open.
   */
  @throws(classOf[java.io.IOException])
  override def open(path: String, shouldAutoInstallLibs: Boolean = false): Unit = {
    dispatchThreadOrBust(fileManager.openFromPath(path, ModelType.Normal, shouldAutoInstallLibs))
  }

  /**
   * Saves the currently open model.
   * Should only be used by ModelResaver.
   */
  @throws(classOf[java.io.IOException])
  private[nlogo] def saveOpenModel(): Unit = {
    dispatchThreadOrBust(fileManager.saveModel(false))
  }

  /**
   * This is called reflectively by the mac app wrapper with the full path.
   * This will only be called after appHandler.ready has been called.
   * @param path the path (absolute) to the NetLogo model to open.
   */
  def handleOpenPath(path: String) = {
    try {
      dispatchThreadOrBust {
        fileManager.aboutToCloseFiles()
        open(path)
      }
    } catch {
      case ex: UserCancelException => org.nlogo.api.Exceptions.ignore(ex)
      case ex: java.io.IOException =>
        new OptionPane(frame, I18N.gui.get("common.messages.error"), ex.getMessage, OptionPane.Options.Ok,
                       OptionPane.Icons.Error)
    }
  }

  /**
   * This is called reflectively by the mac app wrapper.
   */
  def handleQuit(): Unit = {
    fileManager.quit()
  }

  /**
   * This is called reflectively by the mac app wrapper.
   */
  def handleShowAbout(): Unit = {
    showAboutWindow()
  }

  /**
   * This is called reflectively by the mac app wrapper.
   */
  def handleShowPreferences(): Unit = {
    showPreferencesDialog()
  }

  @throws(classOf[java.io.IOException])
  def libraryOpen(path: String): Unit = {
    dispatchThreadOrBust(fileManager.openFromPath(path, ModelType.Library))
  }

  /**
   * Opens a model stored in a string.
   * @param name Model name (will appear in the main window's title bar)
   * @param source The complete model, including widgets and so forth,
   *               in the same format as it would be stored in a file.
   */
  def openFromSource(name:String, source:String): Unit ={
    // I'm not positive that NORMAL is right here.
    openFromSource(source, name, ModelType.Normal)
  }

  def openFromSource(source:String, path:String, modelType:ModelType): Unit ={
    import java.nio.file.Paths
    dispatchThreadOrBust(
      try fileManager.openFromSource(Paths.get(path).toUri, source, modelType)
      catch { case ex:UserCancelException => org.nlogo.api.Exceptions.ignore(ex) })
  }

  /**
   * Runs NetLogo commands and waits for them to complete.
   * <p>This method must <strong>not</strong> be called from the AWT event
   * queue thread or while that thread is blocked.
   * It is an error to do so.
   * @param source The command or commands to run
   * @throws org.nlogo.core.CompilerException if the code fails to compile
   * @throws IllegalStateException if called from the AWT event queue thread
   * @see #commandLater
   */
  @throws(classOf[CompilerException])
  def command(source: String): Unit = {
    org.nlogo.awt.EventQueue.cantBeEventDispatchThread()
    workspace.evaluateCommands(owner, source)
  }

  /**
   * Runs NetLogo commands in the background.  Returns immediately,
   * without waiting for the commands to finish.
   * <p>This method may be called from <em>any</em> thread.
   * @param source The command or commands to run
   * @throws org.nlogo.core.CompilerException if the code fails to compile
   * @see #command
   */
  @throws(classOf[CompilerException])
  def commandLater(source: String): Unit ={
    workspace.evaluateCommands(owner, source, false)
  }

  /**
   * Runs a NetLogo reporter.
   * <p>This method must <strong>not</strong> be called from the AWT event
   * queue thread or while that thread is blocked.
   * It is an error to do so.
   * @param source The reporter to run
   * @return the result reported; may be of type java.lang.Integer, java.lang.Double,
   *         java.lang.Boolean, java.lang.String, {@link org.nlogo.core.LogoList},
   *         {@link org.nlogo.api.Agent}, AgentSet, or Nobody
   * @throws org.nlogo.core.CompilerException if the code fails to compile
   * @throws IllegalStateException if called from the AWT event queue thread
   */
  @throws(classOf[CompilerException])
  def report(source: String): Object = {
    org.nlogo.awt.EventQueue.cantBeEventDispatchThread()
    workspace.evaluateReporter(owner, source, workspace.world.observer)
  }

  /**
   * Returns the contents of the Code tab.
   * @return contents of Code tab
   */
  def getProcedures: String = dispatchThreadOrBust(_tabManager.mainCodeTab.innerSource)

  /**
   * Replaces the contents of the Code tab.
   * Does not recompile the model.
   * @param source new contents
   * @see #compile
   */
  def setProcedures(source:String): Unit = { dispatchThreadOrBust(_tabManager.mainCodeTab.innerSource = source) }

  /**
   * Recompiles the model.  Useful after calling
   * <code>setProcedures()</code>.
   * @see #setProcedures
   */
  def compile(): Unit ={ dispatchThreadOrBust(new CompileAllEvent().raise(this)) }

  /**
   * Recompiles the model after any other events in progress have finished.  Useful if you interrupt
   * a failed compile to ask the user about a workaround to try, like with a missing extension installation
   * from the library.
   * @see #compile
   */
  def compileLater(): Unit ={ dispatchThreadOrBust(new CompileAllEvent().raiseLater(this)) }

  /**
   * Not currently supported.  For now, use <code>command</code>
   * or <code>commandLater()</code> instead.
   * @param name the button to press
   * @see #command
   * @see #commandLater
   */
  def pressButton(name:String): Unit = {
    if (java.awt.EventQueue.isDispatchThread()) throw new IllegalStateException("can't call on event thread")
    val button = findButton(name)
    if (button.forever) {
      button.foreverOn = !button.foreverOn
      org.nlogo.awt.EventQueue.invokeAndWait(() => {
        button.buttonUp = !button.foreverOn
        button.action()
      })
    }
    else {
      org.nlogo.awt.EventQueue.invokeAndWait(() => {
        button.buttonUp = false
        button.action()
      })
      while (button.running) {
        try Thread.sleep(100)
        catch { case ex: InterruptedException => org.nlogo.api.Exceptions.ignore(ex) }
      }
    }
  }

  /// helpers for controlling methods

  private def findButton(name:String): ButtonWidget =
    _tabManager.interfaceTab.getInterfacePanel.getComponents
      .collect{case bw: ButtonWidget => bw}
      .find(_.displayName == name)
      .getOrElse{throw new IllegalArgumentException(
        "button '" + name + "' not found")}

  def smartPack(targetSize: Dimension, allowShrink: Boolean): Unit = {
    if (frame.getExtendedState != Frame.MAXIMIZED_BOTH) {
      val gc = frame.getGraphicsConfiguration
      val maxBounds = gc.getBounds
      val insets = Toolkit.getDefaultToolkit.getScreenInsets(gc)
      val maxWidth = maxBounds.width - insets.left - insets.right
      val maxHeight = maxBounds.height - insets.top - insets.bottom
      val maxBoundsX = maxBounds.x + insets.left
      val maxBoundsY = maxBounds.y + insets.top
      val maxX = maxBoundsX + maxWidth
      val maxY = maxBoundsY + maxHeight

      val (currentWidth, currentHeight) = (frame.getWidth, frame.getHeight)

      // Maybe grow the window, but never shrink it
      var newWidth  = targetSize.width.max(tabManager.interfaceTab.getMinimumWidth).min(maxWidth)
      var newHeight = targetSize.height.min(maxHeight)
      if (!allowShrink) {
        newWidth = newWidth.max(currentWidth)
        newHeight = newHeight.max(currentHeight)
      }

      // move up/left to get more room if possible and necessary
      val moveLeft = 0.max(frame.getLocation().x + newWidth  - maxX)
      val moveUp   = 0.max(frame.getLocation().y + newHeight - maxY)

      // now we can compute our new position
      val newX = maxBoundsX.max(frame.getLocation().x - moveLeft)
      val newY = maxBoundsY.max(frame.getLocation().y - moveUp)

      // and now that we know our position, we can compute our new size
      newWidth  = newWidth.min(maxX - newX)
      newHeight = newHeight.min(maxY - newY)

      // now do it!
      frame.setBounds(newX, newY, newWidth, newHeight)
      frame.validate()

      frame.setMinimumSize(new Dimension(_tabManager.interfaceTab.getMinimumWidth / 2, 300))

      // not sure why this is sometimes necessary - ST 11/24/03
      _tabManager.mainTabs.requestFocus()
    }
  }

  /**
   * Internal use only.
   */
  // used both from HelpMenu and MacHandlers - ST 2/2/09
  def showAboutWindow(): Unit = {
    openAboutDialog.actionPerformed(
      new ActionEvent(frame, ActionEvent.ACTION_PERFORMED, null))
  }

  /**
   * Internal use only.
   */
  def showPreferencesDialog(): Unit = {
    openPreferencesDialog.actionPerformed(
      new ActionEvent(frame, ActionEvent.ACTION_PERFORMED, null))
  }

  def showPreferencesDialogAt(index: Int): Unit = {
    openPreferencesDialog.openToTab(index)
  }

  /// AppFrame
  def getLinkParent: AppFrame = frame // for Event.LinkChild

  private def dispatchThreadOrBust[T](f: => T) = {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    f
  }

  def procedureSource:  String =
    _tabManager.mainCodeTab.innerSource
  def widgets:          Seq[CoreWidget] = {
    _tabManager.interfaceTab.iP.getWidgetsForSaving
  }
  def info:             String =
    _tabManager.infoTab.info
  def turtleShapes:     Seq[VectorShape] =
    workspace.world.turtleShapeList.shapes.collect { case s: VectorShape => s }
  def linkShapes:       Seq[LinkShape] =
    workspace.world.linkShapeList.shapes.collect { case s: LinkShape => s }
  def additionalSections: Seq[ModelSections.ModelSaveable] = {
    val sections =
      Seq[ModelSections.ModelSaveable](workspace.previewCommands,
        labManager,
        aggregateManager,
        workspace)
    workspace.hubNetManager.map(_ +: sections).getOrElse(sections)
  }
  def resources:        Seq[ExternalResource] =
    workspace.getResourceManager.getResources

  def askForName() = {
    val frame = new JFrame()
    frame.setAlwaysOnTop(true)
    val prompt = I18N.gui.get("tools.loggingMode.enterName")
    val name   = new InputOptionPane(frame, "", prompt).getInput
    if (name == null) { "unknown" } else { name.toString.trim() }
  }

}
