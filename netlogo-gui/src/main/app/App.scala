// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.app.common.{ CodeToHtml, EditorFactory, FileActions, FindDialog, Events => AppEvents, SaveModelingCommonsAction }
import org.nlogo.app.interfacetab.{ InterfaceToolBar, WidgetPanel }
import org.nlogo.app.tools.{ AgentMonitorManager, GraphicsPreview, Preference, PreferencesDialog, PreviewCommandsEditor }
import org.nlogo.core.{ AgentKind, CompilerException, Dialect, I18N, LogoList, Model, Nobody,
  Shape, Token, Widget => CoreWidget }, Shape.{ LinkShape, VectorShape }
import org.nlogo.core.model.WidgetReader
import org.nlogo.agent.{Agent, World3D, World}
import org.nlogo.api._
import org.nlogo.awt.UserCancelException
import org.nlogo.log.Logger
import org.nlogo.nvm.{CompilerInterface, DefaultCompilerServices, Workspace}
import org.nlogo.fileformat, fileformat.{ ModelConversion, ModelConverter, NLogoFormat }
import org.nlogo.shape.{ShapesManagerInterface, LinkShapesManagerInterface, TurtleShapesManagerInterface}
import org.nlogo.util.Implicits.RichString
import org.nlogo.util.Implicits.RichStringLike
import org.nlogo.util.Pico
import org.nlogo.util.{ NullAppHandler, Pico }
import org.nlogo.window._
import org.nlogo.window.Events._
import org.nlogo.workspace.{AbstractWorkspace, AbstractWorkspaceScala, Controllable, CurrentModelOpener, HubNetManagerFactory, WorkspaceFactory}
import org.nlogo.window.Event.LinkParent
import org.nlogo.swing.Implicits.thunk2runnable

import org.picocontainer.adapters.AbstractAdapter
import org.picocontainer.Characteristics._
import org.picocontainer.parameters.{ConstantParameter, ComponentParameter}
import org.picocontainer.Parameter

import javax.swing._
import java.awt.{Toolkit, Dimension, Frame}
import java.awt.event.ActionEvent

import scala.language.postfixOps
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
object App{

  private val pico = new Pico()
  // all these guys are assigned in main. yuck
  var app: App = null
  var logger: Logger = null
  private var commandLineModelIsLaunch = false
  private var commandLineModel: String = null
  private var commandLineMagic: String = null
  private var commandLineURL: String = null
  private var loggingName: String = null

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
  def main(args:Array[String]){
    mainWithAppHandler(args, NullAppHandler)
  }

  def mainWithAppHandler(args: Array[String], appHandler: Object) {
    // this call is reflective to avoid complicating dependencies
    appHandler.getClass.getDeclaredMethod("init").invoke(appHandler)

    AbstractWorkspace.isApp(true)
    AbstractWorkspace.isApplet(false)
    org.nlogo.window.VMCheck.detectBadJVMs()
    Logger.beQuiet()
    processCommandLineArguments(args)
    Splash.beginSplash() // also initializes AWT
    pico.add("org.nlogo.compile.Compiler")
    if (Version.is3D)
      pico.addScalaObject("org.nlogo.api.NetLogoThreeDDialect")
    else
      pico.addScalaObject("org.nlogo.api.NetLogoLegacyDialect")

    pico.add("org.nlogo.sdm.gui.NLogoGuiSDMFormat")
    pico.addScalaObject("org.nlogo.sdm.gui.SDMGuiAutoConvertable")

    class ModelLoaderComponent extends AbstractAdapter[ModelLoader](classOf[ModelLoader], classOf[ConfigurableModelLoader]) {
      import scala.collection.JavaConverters._

      def getDescriptor(): String = "ModelLoaderComponent"
      def verify(x$1: org.picocontainer.PicoContainer): Unit = {}

      def getComponentInstance(container: org.picocontainer.PicoContainer, into: java.lang.reflect.Type) = {
        val compiler         = container.getComponent(classOf[CompilerInterface])
        val compilerServices = new DefaultCompilerServices(compiler)

        val loader =
          fileformat.standardLoader(compilerServices)
        val additionalComponents =
          container.getComponents(classOf[ComponentSerialization[Array[String], NLogoFormat]]).asScala
        if (additionalComponents.nonEmpty)
          additionalComponents.foldLeft(loader) {
            case (l, serialization) =>
              l.addSerializer[Array[String], NLogoFormat](serialization)
          }
        else
          loader
      }
    }

    pico.addAdapter(new ModelLoaderComponent())

    class ModelConverterComponent extends AbstractAdapter[ModelConversion](classOf[ModelConversion], classOf[ModelConverter]) {
      import scala.collection.JavaConverters._

      def getDescriptor(): String = "ModelConverterComponent"
      def verify(x$1: org.picocontainer.PicoContainer): Unit = {}

      def getComponentInstance(container: org.picocontainer.PicoContainer, into: java.lang.reflect.Type) = {
        val workspace = container.getComponent(classOf[org.nlogo.api.Workspace])

        val allAutoConvertables =
          fileformat.defaultAutoConvertables ++ container.getComponents(classOf[AutoConvertable]).asScala

        fileformat.converter(workspace.getExtensionManager, workspace.getCompilationEnvironment, workspace, allAutoConvertables)(container.getComponent(classOf[Dialect]))
      }
    }

    pico.addAdapter(new ModelConverterComponent())

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
    pico.add(classOf[AggregateManagerInterface],
          "org.nlogo.sdm.gui.GUIAggregateManager",
          Array[Parameter] (
            new ComponentParameter(classOf[AppFrame]),
            new ComponentParameter(), new ComponentParameter(),
            new ComponentParameter(), new ComponentParameter()))
    pico.add("org.nlogo.lab.gui.LabManager")
    pico.add("org.nlogo.properties.EditDialogFactory")
    // we need to make HeadlessWorkspace objects for BehaviorSpace to use.
    // HeadlessWorkspace uses picocontainer too, but it could get confusing
    // to use the same container in both places, so I'm going to keep the
    // containers separate and just use Plain Old Java Reflection to
    // call HeadlessWorkspace's newInstance() method. - ST 3/11/09
    // And we'll conveniently reuse it for the preview commands editor! - NP 2015-11-18
    val factory = new WorkspaceFactory() with CurrentModelOpener {
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
    pico.add(
      classOf[PreviewCommandsEditorInterface],
      "org.nlogo.app.tools.PreviewCommandsEditor",
      new ComponentParameter(classOf[AppFrame]),
      new ComponentParameter(), new ComponentParameter())
    pico.add(classOf[MenuBar], "org.nlogo.app.MenuBar",
      new ConstantParameter(AbstractWorkspace.isApp))
    pico.addComponent(classOf[Tabs])
    pico.addComponent(classOf[AgentMonitorManager])
    app = pico.getComponent(classOf[App])
    // It's pretty silly, but in order for the splash screen to show up
    // for more than a fraction of a second, we want to initialize as
    // much stuff as we can from main() before handing off to the event
    // thread.  So what happens in the App constructor and what happens
    // in finishStartup() is pretty arbitrary -- it's whatever makes
    // the splash screen come up early without getting a bunch of Java
    // exceptions because we're doing too much on the main thread.
        // Hey, it's important to make a good first impression.
    //   - ST 8/19/03
    org.nlogo.awt.EventQueue.invokeAndWait(()=>app.finishStartup(appHandler))
  }

  private def processCommandLineArguments(args: Array[String]) {
    def printAndExit(s:String){ println(s); sys.exit(0) }
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
        val modelFile = new java.io.File(nextToken())
        // Best to check if the file exists here, because after the GUI thread has started,
        // NetLogo just hangs with the splash screen showing if file doesn't exist. ~Forrest (2/12/2009)
        if (!modelFile.exists) throw new IllegalStateException("File specified to open (" + token + ") was not found!")
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
      else if (token == "--version") printAndExit(Version.version)
      else if (token == "--extension-api-version") printAndExit(APIVersion.version)
      else if (token == "--builddate") printAndExit(Version.buildDate)
      else if (token == "--logging") loggingName = nextToken()
      else if (token == "--log-directory") {
        if (logger != null) logger.changeLogDirectory(nextToken())
        else JOptionPane.showConfirmDialog(null,
          "You need to initialize the logger using the --logging options before specifying a directory.",
          "NetLogo", JOptionPane.DEFAULT_OPTION)
      }
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
          throw new IllegalStateException("File specified to open (" + token + ") was not found!")
        commandLineModel = modelFile.getAbsolutePath()
      }
    }
  }
}

class App extends
    org.nlogo.window.Event.LinkChild with
    org.nlogo.api.Exceptions.Handler with
    org.nlogo.window.ExternalFileManager with
    AppEvent.Handler with
    BeforeLoadEvent.Handler with
    LoadBeginEvent.Handler with
    LoadEndEvent.Handler with
    ModelSavedEvent.Handler with
    ModelSections with
    AppEvents.SwitchedTabsEvent.Handler with
    AboutToQuitEvent.Handler with
    ZoomedEvent.Handler with
    Controllable {

  import App.{pico, logger, commandLineMagic, commandLineModel, commandLineURL, commandLineModelIsLaunch, loggingName}

  val frame = new AppFrame

  // all these guys get set in the locally block
  private var _workspace: GUIWorkspace = null
  def workspace = _workspace
  lazy val owner = new SimpleJobOwner("App", workspace.world.mainRNG, AgentKind.Observer)
  private var _tabs: Tabs = null
  def tabs = _tabs
  var menuBar: MenuBar = null
  var _fileManager: FileManager = null
  var monitorManager:AgentMonitorManager = null
  var aggregateManager: AggregateManagerInterface = null
  var colorDialog: ColorDialog = null
  var labManager:LabManagerInterface = null
  var recentFilesMenu: RecentFilesMenu = null
  private val listenerManager = new NetLogoListenerManager
  lazy val modelingCommons = pico.getComponent(classOf[ModelingCommonsInterface])
  private val ImportWorldURLProp = "netlogo.world_state_url"
  private val ImportRawWorldURLProp = "netlogo.raw_world_state_url"

  val isMac = System.getProperty("os.name").startsWith("Mac")

  /**
   * Quits NetLogo by exiting the JVM.  Asks user for confirmation first
   * if they have unsaved changes. If the user confirms, calls System.exit(0).
   */
  // part of controlling API; used by e.g. the Mathematica-NetLogo link
  // - ST 8/21/07
  @throws(classOf[UserCancelException])
  def quit(){ fileManager.quit() }

  locally {
    frame.addLinkComponent(this)
    pico.addComponent(frame)

    org.nlogo.swing.Utils.setSystemLookAndFeel()

    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      def uncaughtException(t: Thread, e: Throwable) { org.nlogo.api.Exceptions.handle(e) }
    })

    val interfaceFactory = new InterfaceFactory() {
      def widgetPanel(workspace: GUIWorkspace): AbstractWidgetPanel =
        new WidgetPanel(workspace)
      def toolbar(wp: AbstractWidgetPanel, workspace: GUIWorkspace, buttons: List[WidgetInfo], frame: Frame) = {
        new InterfaceToolBar(wp.asInstanceOf[WidgetPanel], workspace, buttons, frame,
          pico.getComponent(classOf[EditDialogFactoryInterface]))
      }
    }
    pico.add(classOf[HubNetManagerFactory], "org.nlogo.hubnet.server.gui.HubNetManagerFactory",
          Array[Parameter] (
            new ComponentParameter(classOf[AppFrame]),
            new ComponentParameter(), new ComponentParameter(),
            new ComponentParameter()))
    pico.addComponent(interfaceFactory)
    pico.addComponent(new EditorFactory(pico.getComponent(classOf[CompilerServices])))
    pico.addComponent(new MenuBarFactory())

    val controlSet = new AppControlSet()

    val world = if(Version.is3D) new World3D() else new World()
    pico.addComponent(world)
    _workspace = new GUIWorkspace(world, GUIWorkspace.KioskLevel.NONE,
                                  frame, frame, pico.getComponent(classOf[HubNetManagerFactory]), App.this, listenerManager, controlSet) {
      val compiler = pico.getComponent(classOf[CompilerInterface])
      // lazy to avoid initialization order snafu - ST 3/1/11
      lazy val updateManager = new UpdateManager {
        override def defaultFrameRate = _workspace.frameRate
        override def ticks = _workspace.world.tickCounter.ticks
        override def updateMode = _workspace.updateMode
      }
      def aggregateManager: AggregateManagerInterface = App.this.aggregateManager
      def inspectAgent(agent: org.nlogo.api.Agent, radius: Double) {
        val a = agent.asInstanceOf[org.nlogo.agent.Agent]
        monitorManager.inspect(a.kind, a, radius)
      }
      override def inspectAgent(agentClass: AgentKind, agent: Agent, radius: Double) {
        monitorManager.inspect(agentClass, agent, radius)
      }
      override def stopInspectingAgent(agent: Agent): Unit = {
        monitorManager.stopInspecting(agent)
      }
      override def stopInspectingDeadAgents(): Unit = {
        monitorManager.stopInspectingDeadAgents()
      }
      override def closeAgentMonitors() { monitorManager.closeAll() }
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
    }

    pico.addComponent(new EditorColorizer(workspace))

     val shapeChangeListener = new ShapeChangeListener(workspace, world)

    frame.addLinkComponent(workspace)

    frame.addLinkComponent(new ExtensionAssistant(frame))

    monitorManager = pico.getComponent(classOf[AgentMonitorManager])
    frame.addLinkComponent(monitorManager)

    _tabs = pico.getComponent(classOf[Tabs])
    controlSet.tabs = Some(_tabs)

    pico.addComponent(tabs.interfaceTab.getInterfacePanel)
    frame.getContentPane.add(tabs, java.awt.BorderLayout.CENTER)

    frame.addLinkComponent(new CompilerManager(workspace, tabs.codeTab))
    frame.addLinkComponent(listenerManager)

    org.nlogo.api.Exceptions.setHandler(this)

    if(loggingName != null)
     startLogging(loggingName)

  }

  private def finishStartup(appHandler: Object) {
    val app = pico.getComponent(classOf[App])
    val currentModelAsString = {() =>
      val modelSaver = pico.getComponent(classOf[ModelSaver])
      modelSaver.modelAsString(modelSaver.currentModel, ModelReader.modelSuffix)
    }
    pico.add(classOf[ModelingCommonsInterface],
          "org.nlogo.mc.ModelingCommons",
          Array[Parameter] (
            new ConstantParameter(currentModelAsString),
            new ComponentParameter(classOf[AppFrame]),
            new ConstantParameter(() => workspace.exportView),
            new ConstantParameter(() => Boolean.box(
              workspace.procedures.get("SETUP") != null &&
                workspace.procedures.get("GO") != null)),
            new ComponentParameter()))
    aggregateManager = pico.getComponent(classOf[AggregateManagerInterface])
    frame.addLinkComponent(aggregateManager)

    labManager = pico.getComponent(classOf[LabManagerInterface])
    frame.addLinkComponent(labManager)

    pico.addComponent(classOf[DirtyMonitor])
    val dirtyMonitor = pico.getComponent(classOf[DirtyMonitor])
    frame.addLinkComponent(dirtyMonitor)

    val menuBar = pico.getComponent(classOf[MenuBar])

    pico.add(classOf[FileManager],
      "org.nlogo.app.FileManager",
      new ComponentParameter(), new ComponentParameter(), new ComponentParameter(),
      new ComponentParameter(), new ComponentParameter(),
      new ConstantParameter(menuBar), new ConstantParameter(menuBar))
    setFileManager(pico.getComponent(classOf[FileManager]))

    val viewManager = pico.getComponent(classOf[GLViewManagerInterface])
    workspace.init(viewManager)
    frame.addLinkComponent(viewManager)

    tabs.init(Plugins.load(pico): _*)

    app.setMenuBar(menuBar)
    frame.setJMenuBar(menuBar)

    org.nlogo.window.RuntimeErrorDialog.init(frame)

    // OK, this is a little kludgy.  First we pack so everything
    // is realized, and all addNotify() methods are called.  But
    // the actual size we get won't be right yet, because the
    // default model hasn't been loaded.  So load it, then pack
    // again.  The first pack is needed because until everything
    // has been realized, the NetLogo event system won't work.
    //  - ST 8/16/03
    frame.pack()

    loadDefaultModel()
    // smartPack respects the command center's current size, rather
    // than its preferred size, so we have to explicitly set the
    // command center to the size we want - ST 1/7/05
    tabs.interfaceTab.commandCenter.setSize(tabs.interfaceTab.commandCenter.getPreferredSize)
    smartPack(frame.getPreferredSize, true)

    if (! isMac) { org.nlogo.awt.Positioning.center(frame, null) }

    org.nlogo.app.common.FindDialog.init(frame)

    Splash.endSplash()
    frame.setVisible(true)
    if(isMac){
      appHandler.getClass.getDeclaredMethod("ready", classOf[AnyRef]).invoke(appHandler, this)
    }
  }

  def startLogging(properties:String) {
    if(new java.io.File(properties).exists) {
      val username =
        JOptionPane.showInputDialog(null, "Enter your name:", "",
          JOptionPane.QUESTION_MESSAGE, null, null, "").asInstanceOf[String]
      if(username != null){
        logger = new Logger(username)
        listenerManager.addListener(logger)
        Logger.configure(properties)
        org.nlogo.api.Version.startLogging()
      }
    }
    else JOptionPane.showConfirmDialog(null, "The file " + properties + " does not exist.",
      "NetLogo", JOptionPane.DEFAULT_OPTION)
  }

  // This is for other windows to get their own copy of the menu
  // bar.  It's needed especially for OS X since the screen menu bar
  // doesn't get shared across windows.  -- AZS 6/17/2005
  private class MenuBarFactory extends org.nlogo.window.MenuBarFactory {
    import org.nlogo.swing.UserAction, UserAction.{ ActionCategoryKey, EditCategory, FileCategory, HelpCategory, ToolsCategory }
    def actions = allActions ++ tabs.permanentMenuActions

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

  ///
  private def loadDefaultModel(){
    if (commandLineModel != null) {
      if (commandLineModelIsLaunch) { // --launch through InstallAnywhere?
        // open up the blank model first so in case
        // the magic open fails for some reason
        // there's still a model loaded ev 3/7/06
        fileManager.newModel()
        open(commandLineModel)
      }
      else libraryOpen(commandLineModel) // --open from command line
    }
    else if (commandLineMagic != null)
      workspace.magicOpen(commandLineMagic)
    else if (commandLineURL != null) {

      try {

        fileManager.openFromURI(new java.net.URI(commandLineURL), ModelType.Library)

        import org.nlogo.awt.EventQueue, org.nlogo.swing.Implicits.thunk2runnable

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

            val source = Source.fromURL(url)(Codec.ISO8859)
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
          JOptionPane.showConfirmDialog(null,
            "Could not obtain NetLogo model from URL '%s'.\nNetLogo will instead start without any model loaded.".format(commandLineURL),
            "Connection Failed", JOptionPane.DEFAULT_OPTION)
      }

    }
    else fileManager.newModel()
  }

  /// zooming

  def handle(e: ZoomedEvent) {
    smartPack(frame.getPreferredSize, false)
  }

  def resetZoom() {
    new ZoomedEvent(0).raise(this)
  }

  lazy val openPreferencesDialog =
    new ShowPreferencesDialog(new PreferencesDialog(frame,
      Preference.Language,
      new Preference.LineNumbers(tabs)))

  lazy val openAboutDialog = new ShowAboutWindow(frame)

  lazy val openColorDialog = new OpenColorDialog(frame)

  lazy val allActions: Seq[javax.swing.Action] = {
    val osSpecificActions = if (isMac) Seq() else Seq(openPreferencesDialog, openAboutDialog)

    val workspaceActions = org.nlogo.window.WorkspaceActions(workspace)

    val generalActions    = Seq[javax.swing.Action](
      openColorDialog,
      new ShowShapeManager("turtleShapesEditor", turtleShapesManager),
      new ShowShapeManager("linkShapesEditor",   linkShapesManager),
      new ShowSystemDynamicsModeler(aggregateManager),
      new OpenHubNetClientEditor(workspace, frame),
      workspace.hubNetControlCenterAction,
      new PreviewCommandsEditor.EditPreviewCommands(
        pico.getComponent(classOf[PreviewCommandsEditorInterface]),
        workspace,
        () => pico.getComponent(classOf[ModelSaver]).asInstanceOf[ModelSaver].currentModel),
      new SaveModelingCommonsAction(modelingCommons, menuBar.fileMenu),
      FindDialog.FIND_ACTION,
      FindDialog.FIND_NEXT_ACTION
    ) ++
    HelpActions.apply ++
    FileActions(workspace, menuBar.fileMenu) ++
    workspaceActions ++
    labManager.actions ++
    fileManager.actions

    osSpecificActions ++ generalActions
  }

  def setMenuBar(menuBar: MenuBar): Unit = {
    if (menuBar != this.menuBar) {
      this.menuBar = menuBar
      tabs.setMenu(menuBar)
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

  // AppEvent stuff (kludgy)
  /**
   * Internal use only.
   */
  def handle(e:AppEvent){
    import AppEventType._
    e.`type` match {
      case RELOAD => reload()
      case MAGIC_OPEN => magicOpen(e.args(0).toString)
      case START_LOGGING =>
        startLogging(e.args(0).toString)
        if(logger!=null)
          logger.modelOpened(workspace.getModelPath)
      case ZIP_LOG_FILES =>
        if (logger==null)
          org.nlogo.log.Files.zipSessionFiles(System.getProperty("java.io.tmpdir"), e.args(0).toString)
        else
          logger.zipSessionFiles(e.args(0).toString)
      case DELETE_LOG_FILES =>
        if(logger==null)
          org.nlogo.log.Files.deleteSessionFiles(System.getProperty("java.io.tmpdir"))
        else
          logger.deleteSessionFiles()
      case _ =>
    }
  }

  private def reload() {
    val modelType = workspace.getModelType
    val path = workspace.getModelPath
    if (modelType != ModelType.New && path != null) openFromSource(FileIO.file2String(path), path, modelType)
    else commandLater("print \"can't, new model\"")
  }

  private def magicOpen(name: String) {
    import collection.JavaConverters._
    val matches = org.nlogo.workspace.ModelsLibrary.findModelsBySubstring(name).asScala
    if (matches.isEmpty) commandLater("print \"no models matching \\\"" + name + "\\\" found\"")
    else {
      val fullName =
        if (matches.size == 1) matches(0)
        else {
          val i = org.nlogo.swing.OptionDialog.showAsList(frame, "Magic Model Matcher", "You must choose!", matches.toArray)
          if (i != -1) matches(i) else null
        }
      if (fullName != null) {
        val path = org.nlogo.workspace.ModelsLibrary.getModelPath(fullName)
        val source = org.nlogo.api.FileIO.file2String(path)
        org.nlogo.awt.EventQueue.invokeLater(() => openFromSource(source, path, ModelType.Library))
      }
    }
  }

  ///

  /**
   * Internal use only.
   */
  final def handle(e: AppEvents.SwitchedTabsEvent) {
    if(e.newTab == tabs.interfaceTab){ monitorManager.showAll(); frame.toFront() }
    else if(e.oldTab == tabs.interfaceTab) monitorManager.hideAll()
  }

  /**
   * Internal use only.
   */
  def handle(e:ModelSavedEvent) {
    workspace.modelSaved(e.modelPath)
    org.nlogo.window.RuntimeErrorDialog.setModelName(workspace.modelNameForDisplay)
    if (AbstractWorkspace.isApp) {
      frame.setTitle(makeFrameTitle)
      workspace.hubNetManager.foreach { manager =>
        manager.setTitle(workspace.modelNameForDisplay, workspace.getModelDir, workspace.getModelType)
      }
    }
  }

  /**
   * Internal use only.
   */
  def handle(e:LoadBeginEvent){
    val modelName = workspace.modelNameForDisplay
    RuntimeErrorDialog.setModelName(modelName)
    if(AbstractWorkspace.isApp) frame.setTitle(makeFrameTitle)
    workspace.hubNetManager.foreach(_.closeClientEditor())
  }

  private var wasAtPreferredSizeBeforeLoadBegan = false
  private var preferredSizeAtLoadEndTime: java.awt.Dimension = null

  /**
   * Internal use only.
   */
  def handle(e:BeforeLoadEvent) {
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
  def handle(e:LoadEndEvent){
    turtleShapesManager.reset()
    linkShapesManager.reset()
    workspace.view.repaint()

    if(AbstractWorkspace.isApp){
      // if we don't call revalidate() here we don't get up-to-date
      // preferred size information - ST 11/4/03
      tabs.interfaceTab.getInterfacePanel.revalidate()
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
      preferredSizeAtLoadEndTime = frame.getPreferredSize()
    }
    frame.toFront()
    tabs.interfaceTab.requestFocus()
  }

  /**
   * Internal use only.
   */
  def handle(e:AboutToQuitEvent){ if(logger != null) logger.close() }

  /**
   * Generates OS standard frame title.
   */
  private def makeFrameTitle = {
    if (workspace.getModelFileName == null) "NetLogo"
    else{
      var title = workspace.modelNameForDisplay
      // on OS X, use standard window title format. otherwise use Windows convention
      if(! System.getProperty("os.name").startsWith("Mac")) title = title + " - " + "NetLogo"
      // 8212 is the unicode value for an em dash. we use the number since
      // we don't want non-ASCII characters in the source files -- AZS 6/14/2005
      else title = "NetLogo " + (8212.toChar) + " " + title

      // OS X UI guidelines prohibit paths in title bars, but oh well...
      if (workspace.getModelType == ModelType.Normal) title += " {" + workspace.getModelDir + "}"
      title
    }
  }

  /**
   * Internal use only.
   */
  def handle(t:Throwable): Unit = {
    try {
      val logo = t.isInstanceOf[LogoException]
      if (! logo) {
        t.printStackTrace(System.err)
        if (org.nlogo.window.RuntimeErrorDialog.suppressJavaExceptionDialogs) {
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
      if (!org.nlogo.window.RuntimeErrorDialog.alreadyVisible)
        org.nlogo.awt.EventQueue.invokeLater(() =>
            RuntimeErrorDialog.show(null, null, Thread.currentThread, t))
    }
    catch {
      case e2: RuntimeException => e2.printStackTrace(System.err)
    }
  }

  /// public methods for controlling (ModelCruncher uses them too)

  /**
   * Opens a model stored in a file.
   * @param path the path (absolute or relative) of the NetLogo model to open.
   */
  @throws(classOf[java.io.IOException])
  def open(path: String) {
    dispatchThreadOrBust(fileManager.openFromPath(path, ModelType.Normal))
  }

  /**
   * Saves the currently open model.
   * Should only be used by ModelResaver.
   */
  @throws(classOf[java.io.IOException])
  private[nlogo] def saveOpenModel(): Unit = {
    dispatchThreadOrBust(fileManager.save(false))
  }

  /**
   * This is called reflectively by the mac app wrapper with the full path.
   * This will only be called after appHandler.ready has been called.
   * @param path the path (absolute) to the NetLogo model to open.
   */
  def handleOpenPath(path: String) = {
    try {
      dispatchThreadOrBust {
        fileManager.offerSave()
        open(path)
      }
    } catch {
      case ex: UserCancelException => org.nlogo.api.Exceptions.ignore(ex)
      case ex: java.io.IOException =>
        javax.swing.JOptionPane.showMessageDialog(
          frame, ex.getMessage,
          I18N.gui.get("common.messages.error"), javax.swing.JOptionPane.ERROR_MESSAGE)
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
  def libraryOpen(path: String) {
    dispatchThreadOrBust(fileManager.openFromPath(path, ModelType.Library))
  }

  /**
   * Opens a model stored in a string.
   * @param name Model name (will appear in the main window's title bar)
   * @param source The complete model, including widgets and so forth,
   *               in the same format as it would be stored in a file.
   */
  def openFromSource(name:String, source:String){
    // I'm not positive that NORMAL is right here.
    openFromSource(source, name, ModelType.Normal)
  }

  def openFromSource(source:String, path:String, modelType:ModelType){
    import java.nio.file.Paths
    dispatchThreadOrBust(
      try fileManager.openFromURI(Paths.get(path).toUri, modelType)
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
  def command(source: String) {
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
  def commandLater(source: String){
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
    workspace.evaluateReporter(owner, source, workspace.world.observer())
  }

  /**
   * Returns the contents of the Code tab.
   * @return contents of Code tab
   */
  def getProcedures: String = dispatchThreadOrBust(tabs.codeTab.innerSource)

  /**
   * Replaces the contents of the Code tab.
   * Does not recompile the model.
   * @param source new contents
   * @see #compile
   */
  def setProcedures(source:String) { dispatchThreadOrBust(tabs.codeTab.innerSource = source) }

  /**
   * Recompiles the model.  Useful after calling
   * <code>setProcedures()</code>.
   * @see #setProcedures
   */
  def compile(){ dispatchThreadOrBust(new CompileAllEvent().raise(this)) }

  /**
   * Switches tabs.
   * @param number which tab to switch to.  0 is the Interface tab,
   *        1 the Info tab, 2 the Code tab, 3 the
   *        Errors tab.
   */
  def selectTab(number:Int){  // zero-indexed
    dispatchThreadOrBust(tabs.setSelectedIndex(number))
  }

  /**
   * Not currently supported.  For now, use <code>command</code>
   * or <code>commandLater()</code> instead.
   * @param name the button to press
   * @see #command
   * @see #commandLater
   */
  def pressButton(name:String) {
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
        try Thread sleep 100
        catch { case ex: InterruptedException => org.nlogo.api.Exceptions.ignore(ex) }
      }
    }
  }

  /**
   * Adds new widget to Interface tab given its specification,
   * in the same (undocumented) format found in a saved model.
   * @param text the widget specification
   */
  def makeWidget(text:String) {
    dispatchThreadOrBust(
      tabs.interfaceTab.getInterfacePanel.loadWidget(WidgetReader.read(text.lines.toList, workspace, fileformat.nlogoReaders(Version.is3D))))
  }

  /// helpers for controlling methods

  private def findButton(name:String): ButtonWidget =
    tabs.interfaceTab.getInterfacePanel.getComponents
      .collect{case bw: ButtonWidget => bw}
      .find(_.displayName == name)
      .getOrElse{throw new IllegalArgumentException(
        "button '" + name + "' not found")}

  def smartPack(targetSize:Dimension, allowShrink: Boolean) {
    val gc = frame.getGraphicsConfiguration
    val maxBounds = gc.getBounds
    val insets = Toolkit.getDefaultToolkit.getScreenInsets(gc)
    val maxWidth = maxBounds.width - insets.left - insets.right
    val maxHeight = maxBounds.height - insets.top - insets.bottom
    val maxBoundsX = maxBounds.x + insets.left
    val maxBoundsY = maxBounds.y + insets.top
    val maxX = maxBoundsX + maxWidth
    val maxY = maxBoundsY + maxHeight

    tabs.interfaceTab.adjustTargetSize(targetSize)

    import StrictMath.{ max, min }

    val (currentWidth, currentHeight) = (frame.getWidth, frame.getHeight)

    // Maybe grow the window, but never shrink it
    var newWidth  = min(targetSize.width, maxWidth)
    var newHeight = min(targetSize.height, maxHeight)
    if (!allowShrink) {
      newWidth = max(newWidth, currentWidth)
      newHeight = max(newHeight, currentHeight)
    }

    // move up/left to get more room if possible and necessary
    val moveLeft = max(0, frame.getLocation().x + newWidth  - maxX)
    val moveUp   = max(0, frame.getLocation().y + newHeight - maxY)

    // now we can compute our new position
    val newX = max(maxBoundsX, frame.getLocation().x - moveLeft)
    val newY = max(maxBoundsY, frame.getLocation().y - moveUp)

    // and now that we know our position, we can compute our new size
    newWidth  = min(newWidth, maxX - newX)
    newHeight = min(newHeight, maxY - newY)

    // now do it!
    frame.setBounds(newX, newY, newWidth, newHeight)
    frame.validate()

    // not sure why this is sometimes necessary - ST 11/24/03
    tabs.requestFocus()
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

  /**
   * Internal use only.
   */
  def getSource(filename:String) = tabs.getSource(filename)

  /// AppFrame
  def getLinkParent: AppFrame = frame // for Event.LinkChild

  private def dispatchThreadOrBust[T](f: => T) = {
    org.nlogo.awt.EventQueue.mustBeEventDispatchThread()
    f
  }

  def procedureSource:  String =
    tabs.codeTab.innerSource
  def widgets:          Seq[CoreWidget] = {
    import collection.JavaConverters._
    tabs.interfaceTab.iP.getWidgetsForSaving
  }
  def info:             String =
    tabs.infoTab.info
  def turtleShapes:     Seq[VectorShape] =
    tabs.workspace.world.turtleShapeList.shapes.collect { case s: VectorShape => s }
  def linkShapes:       Seq[LinkShape] =
    tabs.workspace.world.linkShapeList.shapes.collect { case s: LinkShape => s }
  def additionalSections: Seq[ModelSections.ModelSaveable] = {
    val sections =
      Seq[ModelSections.ModelSaveable](tabs.workspace.previewCommands,
        labManager,
        aggregateManager,
        tabs.workspace)
    workspace.hubNetManager.map(_ +: sections).getOrElse(sections)
  }
}
