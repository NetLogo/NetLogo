// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import com.jthemedetecor.OsThemeDetector

import java.awt.{ Dimension, EventQueue, Frame, GraphicsEnvironment, KeyboardFocusManager, Toolkit, BorderLayout}
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.{ DropTarget, DropTargetDragEvent, DropTargetDropEvent, DropTargetEvent, DropTargetListener }
import java.awt.event.ActionEvent
import java.io.File
import java.lang.ProcessHandle
import java.net.{ ConnectException, URI, URL }
import java.util.{ List => JList }
import javax.swing.{ JFrame, JMenu }

import scala.concurrent.ExecutionContext
import scala.io.{ Codec, Source }
import scala.sys.process.Process

import org.nlogo.agent.{ Agent, World2D, World3D }
import org.nlogo.analytics.Analytics
import org.nlogo.agent.{ CompilationManagement, World }
import org.nlogo.api.{ Agent => ApiAgent, AggregateManagerInterface, AnnouncementsInfoDownloader, APIVersion,
                       Exceptions, FileIO, LogoException, MetadataLoadingException, ModelSections, ModelType,
                       NetLogoLegacyDialect, NetLogoThreeDDialect, RendererInterface, SimpleJobOwner, Version }
import org.nlogo.app.codetab.{ ExternalFileManager, TemporaryCodeTab }
import org.nlogo.app.common.{ Events => AppEvents, FileActions, FindDialog }
import org.nlogo.app.interfacetab.{ CommandCenter, InterfaceTab, InterfaceWidgetControls, WidgetPanel }
import org.nlogo.app.tools.{ AgentMonitorManager, GraphicsPreview, LibraryManagerErrorDialog, PreviewCommandsEditor }
import org.nlogo.awt.UserCancelException
import org.nlogo.compile.Compiler
import org.nlogo.core.{ AgentKind, CompilerException, ExternalResource, I18N, Model, ModelSettings, NetLogoPreferences,
                        Shape, Widget => CoreWidget },
  Shape.{ LinkShape, VectorShape }
import org.nlogo.fileformat.FileFormat
import org.nlogo.gl.view.ViewManager
import org.nlogo.headless.{ HeadlessWorkspace, Main }
import org.nlogo.hubnet.server.gui.HubNetManagerFactory
import org.nlogo.lab.gui.LabManager
import org.nlogo.log.{ JsonFileLogger, LogEvents, LogManager }
import org.nlogo.nvm.{ PresentationCompilerInterface, Workspace }
import org.nlogo.render.Renderer
import org.nlogo.sdm.gui.{ GUIAggregateManager, NLogoGuiSDMFormat, NLogoThreeDGuiSDMFormat, SDMGuiAutoConvertable }
import org.nlogo.shape.editor.{ LinkShapeManagerDialog, TurtleShapeManagerDialog }
import org.nlogo.swing.{ DropdownOptionPane, InputOptionPane, Menu, OptionPane, Positioning, SetSystemLookAndFeel,
                         UserAction, Utils },
  UserAction.{ ActionCategoryKey, EditCategory, FileCategory, HelpCategory, MenuAction, ToolsCategory }
import org.nlogo.theme.{ ClassicTheme, DarkTheme, InterfaceColors, LightTheme, ThemeSync }
import org.nlogo.util.AppHandler
import org.nlogo.window._
import org.nlogo.window.{ MenuBarFactory => WindowMenuBarFactory }
import org.nlogo.window.Event.LinkChild
import org.nlogo.window.Events._
import org.nlogo.workspace.{ AbstractWorkspace, AbstractWorkspaceScala, Controllable, ModelsLibrary, WorkspaceFactory }

import sttp.client4.pekkohttp.PekkoHttpBackend
import sttp.client4.quick.{ quickRequest, UriContext }

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
  sealed abstract trait ModelArgType

  object ModelArgType {
    case class Model(file: String) extends ModelArgType
    case class Magic(name: String) extends ModelArgType
    case class Url(url: String) extends ModelArgType
  }

  case class CommandLineArgs(
    launch: Boolean = false,
    model: Option[ModelArgType] = None,
    logEvents: Option[String] = None,
    logDirectory: Option[String] = None,
    popOutCodeTab: Boolean = false,
    colorTheme: String = NetLogoPreferences.get("colorTheme", {
      if (OsThemeDetector.getDetector.isDark) {
        "dark"
      } else {
        "light"
      }
    })
  )

  // ideally this would be an Option[App], but that would break all the code that uses App.app,
  // so it's not worth changing at this point (Isaac B 8/24/25)
  var app: App = null

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
    if (System.getProperty("os.name").toLowerCase.startsWith("mac")) {
      System.setProperty("apple.awt.graphics.UseQuartz", "false")
      System.setProperty("apple.awt.showGrowBox", "true")
      System.setProperty("apple.laf.useScreenMenuBar", "true")

      mainWithAppHandler(args, new MacHandler)
    } else {
      mainWithAppHandler(args, new AppHandler)
    }
  }

  def mainWithAppHandler(args: Array[String], appHandler: AppHandler): Unit = {
    val lowerArgs = args.map(_.trim.toLowerCase)

    if (lowerArgs.contains("--headless") || lowerArgs.contains("--help")) {
      Main.main(args)

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

      appHandler.init()

      AbstractWorkspace.isApp(true)
      VMCheck.detectBadJVMs()

      val cmdArgs = processCommandLineArguments(args)

      SetSystemLookAndFeel.setSystemLookAndFeel()

      InterfaceColors.setTheme(cmdArgs.colorTheme match {
        case "classic" => ClassicTheme
        case "light" => LightTheme
        case "dark" => DarkTheme
      })

      System.setProperty("flatlaf.menuBarEmbedded", "false")
      System.setProperty("sun.awt.noerasebackground", "true") // stops view2.5d and 3d windows from blanking to white until next interaction

      Splash.beginSplash() // also initializes AWT

      app = new App(cmdArgs)

      // It's pretty silly, but in order for the splash screen to show up
      // for more than a fraction of a second, we want to initialize as
      // much stuff as we can from main() before handing off to the event
      // thread.  So what happens in the App constructor and what happens
      // in finishStartup() is pretty arbitrary -- it's whatever makes
      // the splash screen come up early without getting a bunch of Java
      // exceptions because we're doing too much on the main thread.
          // Hey, it's important to make a good first impression.
      //   - ST 8/19/03
      EventQueue.invokeAndWait(() => app.finishStartup(appHandler))
    } catch {
      case ex: Throwable =>
        StartupError.report(ex)
    }
  }

  // note: this method is static so that it can be called from main()
  // before App is instantiated, which means we can use the --version
  // flags without the AWT ever being initialized, which is handy when
  // we're at the command line and need the version but can't do GUI stuff
  // (e.g. if we're on Linux but don't have the DISPLAY environment variable
  // set) - ST 4/1/02
  private def processCommandLineArguments(args: Array[String]): CommandLineArgs = {
    def printAndExit(s: String): Unit = {
      println(s)
      sys.exit(0)
    }

    val iter = args.iterator

    def requireAndGet(message: String): String = {
      require(iter.hasNext, message)
      iter.next
    }

    var current = CommandLineArgs()

    while (iter.hasNext) {
      iter.next match {
        case "--version" => printAndExit(Version.version)
        case "--extension-api-version" => printAndExit(APIVersion.version)
        case "--builddate" => printAndExit(Version.buildDate)
        case "--events" => Event.logEvents = true

        case token @ ("--open" | "--launch") =>
          require(current.model.isEmpty, I18N.errors.get("args.oneModel"))

          val next = requireAndGet(I18N.errors.getN("args.noFile", token))
          val file = new File(next)

          // Best to check if the file exists here, because after the GUI thread has started,
          // NetLogo just hangs with the splash screen showing if file doesn't exist. ~Forrest (2/12/2009)
          require(file.exists, I18N.errors.getN("fileformat.notFound", next))

          current = current.copy(launch = token == "--launch", model = Some(ModelArgType.Model(file.getAbsolutePath)))

        case "--magic" =>
          require(current.model.isEmpty, I18N.errors.get("args.oneModel"))

          current = current.copy(model = Some(ModelArgType.Magic(requireAndGet(I18N.errors.get("args.noName")))))

        case "--url" =>
          require(current.model.isEmpty, I18N.errors.get("args.oneModel"))

          current = current.copy(model = Some(ModelArgType.Url(requireAndGet(I18N.errors.get("args.noUrl")))))

        case "--color-theme" =>
          val next = requireAndGet(I18N.errors.get("args.noTheme"))

          require(next == "classic" || next == "light" || next == "dark", I18N.errors.getN("themes.unknown", next))

          current = current.copy(colorTheme = next)

        case "--log-events" =>
          current = current.copy(logEvents = Option(requireAndGet(I18N.errors.get("args.noEvents"))))

        case "--log-directory" =>
          current = current.copy(logDirectory = Option(requireAndGet(I18N.errors.get("args.noDirectory"))))

        case "--codetab-window" =>
          current = current.copy(popOutCodeTab = true)

        case "--3d" =>
          Version.set3D(true)

        case token if token.startsWith("--") =>
          // TODO: Decide: should we do System.exit() here?
          // Previously we've just ignored unknown parameters, but that seems wrong to me.  ~Forrest (2/12/2009)
          System.err.println("Error: Unknown command line argument: " + token)

        case token => // we assume it's a filename to "launch"
          require(current.model.isEmpty, I18N.errors.get("args.oneModel"))

          val file = new File(token)

          // Best to check if the file exists here, because after the GUI thread has started,
          // NetLogo just hangs with the splash screen showing if file doesn't exist. ~Forrest (2/12/2009)
          require(file.exists, I18N.errors.getN("fileformat.notFound", token))

          current = current.copy(launch = true, model = Some(ModelArgType.Model(file.getAbsolutePath)))
      }
    }

    current
  }
}

class App(args: App.CommandLineArgs) extends LinkChild with Exceptions.Handler with AppEvent.Handler
  with BeforeLoadEvent.Handler with LoadBeginEvent.Handler with LoadEndEvent.Handler with LoadModelEvent.Handler
  with ModelSavedEvent.Handler with ModelSections with AppEvents.SwitchedTabsEvent.Handler
  with AppEvents.OpenLibrariesDialogEvent.Handler with AppEvents.RestartEvent.Handler with AboutToQuitEvent.Handler
  with ZoomedEvent.Handler with Controllable {

  val frame = new AppFrame

  def getFrame = frame

  private val world: World & CompilationManagement = {
    if (Version.is3D) {
      new World3D
    } else {
      new World2D
    }
  }

  private val editDialogFactory = new EditDialogFactory

  private val interfaceFactory = new InterfaceFactory {
    def widgetPanel(workspace: GUIWorkspace): AbstractWidgetPanel =
      new WidgetPanel(workspace)

    def widgetControls(wp: AbstractWidgetPanel, workspace: GUIWorkspace, buttons: List[WidgetInfo], frame: Frame) =
      new InterfaceWidgetControls(wp.asInstanceOf[WidgetPanel], workspace, buttons, frame, editDialogFactory)
  }

  private val menuBarFactory = new MenuBarFactory
  private val externalFileManager = new ExternalFileManager
  private val listenerManager = new NetLogoListenerManager
  private val themeSyncManager = new ThemeSyncManager

  private val errorDialogManager =
    new ErrorDialogManager(frame, Map(classOf[MetadataLoadingException] -> new LibraryManagerErrorDialog(frame)))

  private val controlSet = new AppControlSet

  val workspace: GUIWorkspace = new GUIWorkspace(world, GUIWorkspace.KioskLevel.None, frame, frame,
                                                 new HubNetManagerFactory(frame, interfaceFactory, menuBarFactory),
                                                 externalFileManager, listenerManager, errorDialogManager,
                                                 controlSet) {
    val compiler = new Compiler(
      if (Version.is3D) {
        NetLogoThreeDDialect
      } else {
        NetLogoLegacyDialect
      }
    )

    // lazy to avoid initialization order snafu - ST 3/1/11
    lazy val updateManager = new UpdateManager {
      override def defaultFrameRate = frameRate
      override def ticks = workspace.world.tickCounter.ticks
      override def updateMode = workspace.updateMode
    }

    def aggregateManager: AggregateManagerInterface =
      new GUIAggregateManager(frame, menuBarFactory, this, colorizer, editDialogFactory, extensionManager)

    def inspectAgent(agent: ApiAgent, radius: Double): Unit = {
      agent match {
        case a: Agent =>
          monitorManager.inspect(a.kind, a, radius)

        case _ =>
      }
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

    override def closeAgentMonitors(): Unit = {
      monitorManager.closeAll()
    }

    override def newRenderer: RendererInterface =
      new Renderer(this.world)

    def updateModel(model: Model): Model =
      model.withOptionalSection("org.nlogo.modelsection.modelsettings", Some(ModelSettings(snapOn)),
                                Some(ModelSettings(false)))
  }

  private val monitorManager = new AgentMonitorManager(workspace)
  private val colorizer = new EditorColorizer(workspace)

  private val interfaceTab = new InterfaceTab(workspace, monitorManager, editDialogFactory, colorizer,
                                              new CommandCenter(workspace, true))

  private val modelLoader = FileFormat.standardAnyLoader(false, workspace, true)
  private val modelSaver = new ModelSaver(this, modelLoader)

  val tabManager = new TabManager(workspace, interfaceTab, externalFileManager)

  private val dirtyMonitor = new DirtyMonitor(frame, modelSaver, modelLoader, workspace,
                                              _.fold(modelTitle())(externalFileTitle), tabManager.separateTabsWindow)

  private val converter = FileFormat.converter(workspace.getExtensionManager, workspace.getLibraryManager,
                                               workspace.getCompilationEnvironment, workspace,
                                               FileFormat.defaultAutoConvertables :+ SDMGuiAutoConvertable)
                                              (workspace.dialect)

  private val mainMenuBar = new MainMenuBar(AbstractWorkspace.isApp)

  private val workspaceFactory = new WorkspaceFactory {
    def newInstance: AbstractWorkspaceScala = HeadlessWorkspace.newInstance

    def openCurrentModelIn(w: Workspace): Unit = {
      w.setModelPath(workspace.getModelPath)
      w.openModel(modelSaver.currentModelInCurrentVersion)
    }
  }

  val fileManager = new FileManager(workspace, modelLoader, converter, dirtyMonitor, modelSaver, mainMenuBar,
                                    frame, tabManager, workspaceFactory)

  private val recentFilesMenu = new RecentFilesMenu(frame, fileManager)

  private val labManager = new LabManager(workspace, editDialogFactory, colorizer, menuBarFactory, workspaceFactory,
                                          modelLoader)

  private val turtleShapesManager = new TurtleShapeManagerDialog(frame, world, modelLoader)
  private val linkShapesManager = new LinkShapeManagerDialog(frame, world, modelLoader)

  private lazy val owner = new SimpleJobOwner("App", world.mainRNG, AgentKind.Observer)

  private val runningInMacWrapper = Option(System.getProperty("org.nlogo.mac.appClassName")).nonEmpty

  private val analyticsConsent = NetLogoPreferences.get("sendAnalytics", "$$$") == "$$$"

  /**
   * Quits NetLogo by exiting the JVM.  Asks user for confirmation first
   * if they have unsaved changes. If the user confirms, calls System.exit(0).
   */
  // part of controlling API; used by e.g. the Mathematica-NetLogo link
  // - ST 8/21/07
  @throws(classOf[UserCancelException])
  def quit(): Unit = {
    fileManager.quit()
  }

  locally {
    frame.addLinkComponent(this)
    frame.addLinkComponent(workspace)
    frame.addLinkComponent(monitorManager)
    frame.addLinkComponent(tabManager)
    frame.addLinkComponent(new CompilerManager(workspace, world, tabManager.mainCodeTab))
    frame.addLinkComponent(listenerManager)
    frame.addLinkComponent(workspace.aggregateManager)
    frame.addLinkComponent(labManager)
    frame.addLinkComponent(dirtyMonitor)
    frame.addLinkComponent(fileManager)
    frame.addLinkComponent(recentFilesMenu)
    frame.addLinkComponent(tabManager.separateTabsWindow)

    frame.addLinkComponent({
      val libMan = workspace.getLibraryManager
      new ExtensionAssistant(
        frame
      , (name)            => libMan.lookupExtension(name, "").isDefined
      , (name)            => libMan.lookupExtension(name, "").fold("N/A")(_.version)
      , { (name, version) => libMan.lookupExtension(name, version).foreach(libMan.installExtension); compileLater() }
      )
    })

    val viewManager = new ViewManager(workspace, frame, interfaceTab.iP)

    frame.addLinkComponent(viewManager)

    workspace.init(viewManager)

    if (Version.systemDynamicsAvailable) {
      new NLogoGuiSDMFormat().addToLoader(modelLoader)
      new NLogoThreeDGuiSDMFormat().addToLoader(modelLoader)
    }

    Exceptions.setHandler(this)

    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
      def uncaughtException(t: Thread, e: Throwable): Unit = {
        Exceptions.handle(e)
      }
    })

    ShapeChangeListener.listen(workspace, world)

    controlSet.interfaceTab = Some(interfaceTab)

    def switchOrPref(switchValue: Option[String], prefName: String, default: String): String =
      switchValue.filter(_.trim.nonEmpty).getOrElse(NetLogoPreferences.get(prefName, default))

    if (args.logDirectory.isDefined || args.logEvents.isDefined ||
        NetLogoPreferences.get("loggingEnabled", "false").toBoolean) {
      val finalLogDirectory = new File(switchOrPref(args.logDirectory, "logDirectory", System.getProperty("user.home")))
      val eventsString      = switchOrPref(args.logEvents, "logEvents", "")
      val events            = LogEvents.parseEvents(eventsString)
      val studentName       = askForName()
      val addListener       = (l) => listenerManager.addListener(l)
      val loggerFactory     = (p) => new JsonFileLogger(p)
      LogManager.start(addListener, loggerFactory, finalLogDirectory, events, studentName, () =>
        new OptionPane(frame, I18N.gui.get("common.messages.warning"), I18N.gui.get("error.dialog.logDirectory"),
                       OptionPane.Options.Ok, OptionPane.Icons.Warning))
    }
  }

  private def finishStartup(appHandler: AppHandler): Unit = {
    try {
      frame.getContentPane.add(tabManager.mainTabs, BorderLayout.CENTER)

      allActions.foreach(mainMenuBar.offerAction)

      recentFilesMenu.setMenu(mainMenuBar)
      frame.setJMenuBar(mainMenuBar)

      tabManager.init(fileManager, dirtyMonitor, mainMenuBar, allActions)

      FindDialog.init(frame, tabManager.separateTabsWindow)

      // OK, this is a little kludgy.  First we pack so everything
      // is realized, and all addNotify() methods are called.  But
      // the actual size we get won't be right yet, because the
      // default model hasn't been loaded.  So load it, then pack
      // again.  The first pack is needed because until everything
      // has been realized, the NetLogo event system won't work.
      //  - ST 8/16/03
      frame.pack()

      loadDefaultModel()

      interfaceTab.packSplitPane()

      smartPack(frame.getPreferredSize, true)

      interfaceTab.resetSplitPane()

      Positioning.center(frame, null)

      new DropTarget(frame.getContentPane, new DropTargetListener {
        def dragEnter(e: DropTargetDragEvent): Unit = {}
        def dragExit(e: DropTargetEvent): Unit = {}
        def dragOver(e: DropTargetDragEvent): Unit = {}

        def drop(e: DropTargetDropEvent): Unit = {

          import scala.jdk.CollectionConverters.ListHasAsScala

          if (e.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            e.acceptDrop(e.getDropAction)

            val files = e.getTransferable.getTransferData(DataFlavor.javaFileListFlavor)
                         .asInstanceOf[JList[File]].asScala

            if (files.size == 1) {
              val path = files(0).toString

              if ("\\.nlogox?(3d)?$".r.findFirstIn(path).isDefined) {
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

      appHandler.ready(this)

      if (args.popOutCodeTab || NetLogoPreferences.getBoolean("startSeparateCodeTab", false))
        tabManager.switchWindow(true)

      import ExecutionContext.Implicits.global

      AnnouncementsInfoDownloader.fetch().foreach(interfaceTab.appendAnnouncements)

      interfaceTab.requestFocus()

      syncWindowThemes()

      if (analyticsConsent) {
        val sendAnalytics = new OptionPane(frame, I18N.gui.get("dialog.analyticsConsent"),
                                           I18N.gui.get("dialog.analyticsConsent.message"), OptionPane.Options.YesNo,
                                           OptionPane.Icons.Info).getSelectedIndex == 0

        NetLogoPreferences.putBoolean("sendAnalytics", sendAnalytics)

        val request = quickRequest.post(uri"https://backend.netlogo.org/items/NetLogo_Desktop_Analytics")
                                  .body(s"""{"enabled": $sendAnalytics}""")
                                  .contentType("application/json")

        val backend = PekkoHttpBackend()

        request.send(backend).onComplete { _ =>
          backend.close()
        }
      }

      Analytics.refreshPreference()
      Analytics.appStart(Version.versionNumberNo3D, Version.is3D)
    } catch {
      case ex: Throwable => StartupError.report(ex)
    }
  }

  // used by preferences that require a restart if the user chooses the "Restart Now" option (Isaac B 7/23/25)
  def handle(e: AppEvents.RestartEvent): Unit = {

    val processFile = new File(ProcessHandle.current.info.command.get)

    if (System.getProperty("os.name").toLowerCase.startsWith("mac")) {

      // this looks a bit strange but the reported process is actually the binary file nested deep in the .app file,
      // and in order for the OS to register the app properly, the .app file needs to be run (Isaac B 7/24/25)
      Process(Seq("open", "-n", "-a", processFile.getParentFile.getParentFile.getParentFile.toString)).run()

    } else if (System.getProperty("os.name").toLowerCase.startsWith("linux")) {

      // It's very important that we remove `_JPACKAGE_LAUNCHER` from the environment.  The Linux launcher will fail
      // to relaunch itself if that value is set.  See: https://bugs.java.com/bugdatabase/view_bug?bug_id=8289201
      // --Jason B. (8/29/25)
      val cmdSequence = Source.fromFile("/proc/self/cmdline").getLines().toSeq.head.split('\u0000').toArray
      val cwd         = new File("/proc/self/cwd").getCanonicalFile()
      val pb          = ProcessBuilder(cmdSequence*).directory(cwd).inheritIO()
      pb.environment.remove("_JPACKAGE_LAUNCHER")
      pb.start()

    } else {
      Process(Seq(processFile.toString)).run()
    }

    System.exit(0)

  }

  // This is for other windows to get their own copy of the menu
  // bar.  It's needed especially for OS X since the screen menu bar
  // doesn't get shared across windows.  -- AZS 6/17/2005
  private class MenuBarFactory extends WindowMenuBarFactory {
    def actions = allActions ++ tabManager.permanentMenuActions

    def createMenu(newMenu: Menu, category: String): JMenu = {
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
    args.model match {
      case Some(App.ModelArgType.Model(file)) =>
        if (args.launch) { // --launch through InstallAnywhere?
          // open up the blank model first so in case
          // the magic open fails for some reason
          // there's still a model loaded ev 3/7/06
          fileManager.newModel()
          open(file)
        } else { // --open from command line
          libraryOpen(file)
        }

      case Some(App.ModelArgType.Magic(name)) =>
        workspace.magicOpen(name)

      case Some(App.ModelArgType.Url(url)) =>
        try {
          fileManager.openFromURI(new URI(url), ModelType.Library)

          Option(System.getProperty("netlogo.raw_world_state_url")).map { url =>
            EventQueue.invokeLater(() => {
              // `io.Source.fromURL(url).bufferedReader` steps up to bat and...
              // manages to fail gloriously here! --JAB (8/22/12)
              import java.io.{ BufferedReader, InputStreamReader }

              workspace.importWorld(new BufferedReader(new InputStreamReader(new URL(url).openStream())))
              workspace.view.dirty()
              workspace.view.repaint()
            })
          }.orElse(Option(System.getProperty("netlogo.world_state_url")).map { url =>
            import java.io.{ ByteArrayInputStream, InputStreamReader }
            import java.util.zip.GZIPInputStream
            import scala.io.{ Codec, Source }

            val source = Source.fromURL(url)(using Codec.ISO8859)
            val bytes  = source.map(_.toByte).toArray
            val bais   = new ByteArrayInputStream(bytes)
            val gis    = new GZIPInputStream(bais)
            val reader = new InputStreamReader(gis)

            EventQueue.invokeLater(() => {
              workspace.importWorld(reader)
              workspace.view.dirty()
              workspace.view.repaint()
              source.close()
              bais.close()
              gis.close()
              reader.close()
            })
          })
        } catch {
          case _: ConnectException =>
            fileManager.newModel()

            new OptionPane(frame, I18N.gui.get("file.open.error.unloadable.title"),
                           I18N.gui.getN("file.open.error.unloadable.message", url),
                           OptionPane.Options.OkCancel, OptionPane.Icons.Warning)
        }

      case _ =>
        // if recent list is empty we need the new model, or if loading the recent model
        // fails then we'll fall back on it.  -Jeremy B June 2021
        fileManager.newModel()

        if (NetLogoPreferences.get("loadLastOnStartup", "false").toBoolean) {
          (new RecentFiles).models.headOption.foreach {
            case ModelEntry(path, modelType) =>
              fileManager.openFromPath(path, modelType)
          }
        }
    }
  }

  /// zooming

  def handle(e: ZoomedEvent): Unit = {
    smartPack(frame.getPreferredSize, false)
  }

  def resetZoom(): Unit = {
    new ZoomedEvent(0).raise(this)
  }

  lazy val openPreferencesDialog = new ShowPreferencesDialog(frame, tabManager, tabManager.interfaceTab.iP)

  lazy val openAboutDialog = new ShowAboutWindow(frame)

  lazy val openRGBAColorDialog = new OpenRGBAColorDialog(frame)

  lazy val openLibrariesDialog = {
    val updateSource =
      (transform: (String) => String) =>
        tabManager.mainCodeTab.innerSource = transform(tabManager.mainCodeTab.innerSource)
    new OpenLibrariesDialog( frame, workspace.getLibraryManager, compile
                           , workspace.compiler.tokenizeWithWhitespace(_, workspace.getExtensionManager), updateSource
                           , () => workspace.getExtensionPathMappings())
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
      new ShowSystemDynamicsModeler(workspace.aggregateManager),
      new OpenHubNetClientEditor(workspace, frame),
      workspace.hubNetControlCenterAction,
      new PreviewCommandsEditor.EditPreviewCommands(
        new PreviewCommandsEditor(frame, workspaceFactory, new GraphicsPreview), workspace,
        () => modelSaver.currentModel),
      FindDialog.FIND_ACTION,
      FindDialog.FIND_NEXT_ACTION,
      new ConvertWidgetSizes(frame, tabManager.interfaceTab.iP)
    ) ++
    HelpActions.apply ++
    FileActions(workspace, mainMenuBar.fileMenu) ++
    workspaceActions ++
    labManager.actions ++
    fileManager.actions

    osSpecificActions ++ generalActions
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
        try {
          fileManager.aboutToCloseFiles()
          ModelsLibrary.getModelPath(name).foreach { path =>
            val source = FileIO.fileToString(path)
            EventQueue.invokeLater(() => openFromSource(source, path, ModelType.Library))
          }
        } catch {
          case _: UserCancelException =>
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
    themeSyncManager.addSyncComponent(ts)
  }

  def removeSyncComponent(ts: ThemeSync): Unit = {
    themeSyncManager.removeSyncComponent(ts)
  }

  def addSyncFunction(f: () => Unit): Long = {
    themeSyncManager.addSyncFunction(f)
  }

  def removeSyncFunction(id: Long): Unit = {
    themeSyncManager.removeSyncFunction(id)
  }

  def syncWindowThemes(): Unit = {
    FindDialog.syncTheme()

    mainMenuBar.syncTheme()

    tabManager.syncTheme()
    frame.repaint()
    tabManager.separateTabsWindow.repaint()

    workspace.glView.syncTheme()
    workspace.glView.repaint()

    monitorManager.syncTheme()
    turtleShapesManager.syncTheme()
    linkShapesManager.syncTheme()
    labManager.syncTheme()

    openPreferencesDialog.syncTheme()
    openAboutDialog.syncTheme()
    openRGBAColorDialog.syncTheme()
    openLibrariesDialog.syncTheme()

    workspace.hubNetManager match {
      case Some(ts: ThemeSync) => ts.syncTheme()
      case _ =>
    }

    workspace.aggregateManager match {
      case ts: ThemeSync => ts.syncTheme()
      case _ =>
    }

    errorDialogManager.syncTheme()

    // this allows external objects like extension GUIs to sync with the theme (Isaac B 1/12/25)
    themeSyncManager.syncAll()

  }

  /**
   * Internal use only.
   */
  final def handle(e: AppEvents.SwitchedTabsEvent): Unit = {
    if (e.newTab == tabManager.interfaceTab) {
      monitorManager.showAll()
      frame.toFront()
    } else if (e.oldTab == tabManager.interfaceTab) {
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
      tabManager.interfaceTab.packSplitPane()
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
      tabManager.interfaceTab.resetSplitPane()
      preferredSizeAtLoadEndTime = frame.getPreferredSize()
    }

    if (KeyboardFocusManager.getCurrentKeyboardFocusManager.getActiveWindow != null) {
      frame.toFront()
      tabManager.interfaceTab.requestFocus()
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

  private def modelTitle(allowDirtyMarker: Boolean = true): String = {
    if (workspace.getModelFileName == null) "NetLogo"
    else {
      val title = frameTitle(workspace.modelNameForDisplay, allowDirtyMarker && dirtyMonitor.modelDirty)
      // OS X UI guidelines prohibit paths in title bars, but oh well...
      if (workspace.getModelType == ModelType.Normal) s"$title {${workspace.getModelDir}}" else title
    }
  }

  private def externalFileTitle(path: String): String = {
    val filename = TemporaryCodeTab.stripPath(path)
    (tabManager.getTabWithFilename(Right(path)) orElse tabManager.getTabWithFilename(Left(path))).
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
  def getProcedures: String = dispatchThreadOrBust(tabManager.mainCodeTab.innerSource)

  /**
   * Replaces the contents of the Code tab.
   * Does not recompile the model.
   * @param source new contents
   * @see #compile
   */
  def setProcedures(source:String): Unit = { dispatchThreadOrBust(tabManager.mainCodeTab.innerSource = source) }

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
    tabManager.interfaceTab.getInterfacePanel.getComponents
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

      frame.setMinimumSize(new Dimension(tabManager.interfaceTab.getMinimumWidth / 2, 300))

      // not sure why this is sometimes necessary - ST 11/24/03
      tabManager.mainTabs.requestFocus()
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
    tabManager.mainCodeTab.innerSource
  def widgets:          Seq[CoreWidget] = {
    tabManager.interfaceTab.iP.getWidgetsForSaving
  }
  def info:             String =
    tabManager.infoTab.info
  def turtleShapes:     Seq[VectorShape] =
    workspace.world.turtleShapeList.shapes.collect { case s: VectorShape => s }
  def linkShapes:       Seq[LinkShape] =
    workspace.world.linkShapeList.shapes.collect { case s: LinkShape => s }
  def additionalSections: Seq[ModelSections.ModelSaveable] = {
    val sections =
      Seq[ModelSections.ModelSaveable](workspace.previewCommands,
        labManager,
        workspace.aggregateManager,
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
