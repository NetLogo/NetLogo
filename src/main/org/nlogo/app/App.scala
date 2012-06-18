// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.agent.{Agent, World3D, World}
import org.nlogo.api._
import org.nlogo.awt.UserCancelException
import org.nlogo.log.Logger
import org.nlogo.nvm.{CompilerInterface, Workspace, WorkspaceFactory}
import org.nlogo.shape.{ShapesManagerInterface, ShapeChangeListener, LinkShapesManagerInterface, TurtleShapesManagerInterface}
import org.nlogo.util.Pico
import org.nlogo.window._
import org.nlogo.window.Events._
import org.nlogo.workspace.{AbstractWorkspace, Controllable}
import org.nlogo.window.Event.LinkParent
import org.nlogo.swing.Implicits.thunk2runnable

import org.picocontainer.Characteristics._
import org.picocontainer.parameters.{ConstantParameter, ComponentParameter}
import org.picocontainer.Parameter

import javax.swing._
import java.awt.event.{WindowAdapter, WindowEvent}
import java.awt.{Toolkit, Dimension, Frame}

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
    // on Mac OS X 10.5, we have to explicitly ask for the Quartz
    // renderer. perhaps we should eventually switch to the Sun
    // renderer since that's the new default, but for now, the
    // Quartz renderer is what we've long used and tested, so
    // let's stick with it - ST 12/4/07
    System.setProperty("apple.awt.graphics.UseQuartz", "true")
    System.setProperty("apple.awt.showGrowBox", "true")
    System.setProperty("apple.laf.useScreenMenuBar", "true")
    // tweak behavior of Quaqua
    System.setProperty("Quaqua.visualMargin", "1,1,1,1")
    // we need to call MacHandlers.init() very early (I'm guessing
    // it must be before the AWT initializes), in order for the
    // handlers to work.  At this point, we don't have an app
    // instance yet, so that's why we have to pass it in later
    // when we call MacHandlers.ready() - ST 11/13/03
    if(System.getProperty("os.name").startsWith("Mac")) MacHandlers.init()

    AbstractWorkspace.isApp(true)
    AbstractWorkspace.isApplet(false)
    org.nlogo.window.VMCheck.detectBadJVMs()
    Logger.beQuiet()
    processCommandLineArguments(args)
    Splash.beginSplash() // also initializes AWT
    pico.addScalaObject("org.nlogo.compiler.Compiler")
    pico.addComponent(classOf[AppletSaver])
    pico.addComponent(classOf[ProceduresToHtml])
    pico.addComponent(classOf[App])
    pico.as(NO_CACHE).addComponent(classOf[FileMenu])
    pico.addComponent(classOf[ModelSaver])
    pico.addComponent(classOf[ToolsMenu])
    pico.add("org.nlogo.gl.view.ViewManager")
    // Anything that needs a parent Frame, we need to use ComponentParameter
    // and specify classOf[AppFrame], otherwise PicoContainer won't know which
    // Frame to use - ST 6/16/09
    // we need to give TurtleShapeManagerDialog and LinkShapeManagerDialog different
    // ShapeSectionReader objects, so we use ConstantParameter for that - ST 6/16/09
    pico.add(classOf[TurtleShapesManagerInterface],
          "org.nlogo.shape.editor.TurtleShapeManagerDialog",
          Array[Parameter] (
            new ComponentParameter(classOf[AppFrame]),
            new ComponentParameter(), new ComponentParameter(),
            new ConstantParameter(new ShapeSectionReader(ModelSection.TurtleShapes))))
    pico.add(classOf[LinkShapesManagerInterface],
          "org.nlogo.shape.editor.LinkShapeManagerDialog",
          Array[Parameter] (
            new ComponentParameter(classOf[AppFrame]),
            new ComponentParameter(), new ComponentParameter(),
            new ConstantParameter(new ShapeSectionReader(ModelSection.LinkShapes))))
    pico.add(classOf[AggregateManagerInterface],
          "org.nlogo.sdm.gui.GUIAggregateManager",
          Array[Parameter] (
            new ComponentParameter(classOf[AppFrame]),
            new ComponentParameter(), new ComponentParameter(),
            new ComponentParameter(), new ComponentParameter()))
    pico.add(classOf[HubNetInterface],
          "org.nlogo.hubnet.server.gui.GUIHubNetManager",
          Array[Parameter] (
            new ComponentParameter(), new ComponentParameter(classOf[AppFrame]),
            new ComponentParameter(), new ComponentParameter(),
            new ComponentParameter()))
    pico.add("org.nlogo.lab.gui.LabManager")
    pico.add("org.nlogo.properties.EditDialogFactory")
    // we need to make HeadlessWorkspace objects for BehaviorSpace to use.
    // HeadlessWorkspace uses picocontainer too, but it could get confusing
    // to use the same container in both places, so I'm going to keep the
    // containers separate and just use Plain Old Java Reflection to
    // call HeadlessWorkspace's newInstance() method. - ST 3/11/09
    val factory = new WorkspaceFactory() {
      def newInstance: Workspace = {
        val w = Class.forName("org.nlogo.headless.HeadlessWorkspace").
                getMethod("newInstance").invoke(null).asInstanceOf[Workspace]
        w.setModelPath(app.workspace.getModelPath())
        w.openString(new ModelSaver(pico.getComponent(classOf[App])).save)
        w
      }
    }
    pico.addComponent(classOf[WorkspaceFactory], factory)
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
    org.nlogo.awt.EventQueue.invokeAndWait(()=>app.finishStartup())
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

  // TODO: lots of duplication here...
  private class ShapeSectionReader(section: ModelSection) extends org.nlogo.shape.ModelSectionReader {
    @throws(classOf[java.io.IOException])
    def read(path: String) = {
      val map = ModelReader.parseModel(FileIO.file2String(path))
      if (map == null ||
              map.get(ModelSection.Version) == null ||
              map.get(ModelSection.Version).length == 0 ||
              !ModelReader.parseVersion(map).startsWith("NetLogo")) {
        // not a valid model file
        Array.empty[String]
      }
      else map.get(section)
    }

    @throws(classOf[java.io.IOException])
    override def getVersion(path:String) = {
      val map = ModelReader.parseModel(FileIO.file2String(path))
      if (map == null ||
              map.get(ModelSection.Version) == null ||
              map.get(ModelSection.Version).length == 0 ||
              !ModelReader.parseVersion(map).startsWith("NetLogo")) {
        // not a valid model file
        null;
      }
      else ModelReader.parseVersion(map)
    }
  }
}
  
class App extends
    org.nlogo.window.Event.LinkChild with
    org.nlogo.util.Exceptions.Handler with
    org.nlogo.window.ExternalFileManager with
    AppEvent.Handler with
    BeforeLoadEvent.Handler with
    LoadBeginEvent.Handler with
    LoadSectionEvent.Handler with
    LoadEndEvent.Handler with
    ModelSavedEvent.Handler with
    Events.SwitchedTabsEvent.Handler with
    AboutToQuitEvent.Handler with
    Controllable {

  import App.{pico, logger, commandLineMagic, commandLineModel, commandLineURL, commandLineModelIsLaunch, loggingName}

  val frame = new AppFrame

  // all these guys get set in the locally block
  private var _workspace: GUIWorkspace = null
  def workspace = _workspace
  lazy val owner = new SimpleJobOwner("App", workspace.world.mainRNG, classOf[Observer])
  private var _tabs: Tabs = null
  def tabs = _tabs
  var dirtyMonitor:DirtyMonitor = null // accessed from FileMenu - ST 2/26/04
  var helpMenu:HelpMenu = null
  var fileMenu: FileMenu = null
  var monitorManager:AgentMonitorManager = null
  var aggregateManager: AggregateManagerInterface = null
  var colorDialog: ColorDialog = null
  var labManager:LabManagerInterface = null
  private val listenerManager = new NetLogoListenerManager

  /**
   * Quits NetLogo by exiting the JVM.  Asks user for confirmation first
   * if they have unsaved changes. If the user confirms, calls System.exit(0).
   */
  // part of controlling API; used by e.g. the Mathematica-NetLogo link
  // - ST 8/21/07
  @throws(classOf[UserCancelException])
  def quit(){ fileMenu.quit() }

  locally{
    frame.addLinkComponent(this)
    pico.addComponent(frame)

    org.nlogo.swing.Utils.setSystemLookAndFeel()

    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      def uncaughtException(t: Thread, e: Throwable) { org.nlogo.util.Exceptions.handle(e) }
    })

    val interfaceFactory = new InterfaceFactory() {
      def widgetPanel(workspace: GUIWorkspace): AbstractWidgetPanel = new WidgetPanel(workspace)
      def toolbar(wp: AbstractWidgetPanel, workspace: GUIWorkspace, buttons: List[WidgetInfo], frame: Frame) = {
        new InterfaceToolBar(wp.asInstanceOf[WidgetPanel], workspace, buttons, frame,
          pico.getComponent(classOf[EditDialogFactoryInterface])) {
          override def addControls() {
            super.addControls()
            add(new JButton(fileMenu.saveClientAppletAction()))
          }
        }
      }
    }
    pico.addComponent(interfaceFactory)

    val hubNetManagerFactory = new AbstractWorkspace.HubNetManagerFactory() {
      def newInstance(workspace: AbstractWorkspace): HubNetInterface = {
        pico.getComponent(classOf[HubNetInterface])
      }
    }

    val world = if(Version.is3D) new World3D() else new World()
    pico.addComponent(world)
    _workspace = new GUIWorkspace(world, GUIWorkspace.KioskLevel.NONE,
                                  frame, frame, hubNetManagerFactory, App.this, listenerManager) {
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
        monitorManager.inspect(a.getAgentClass(), a, radius)
      }
      override def inspectAgent(agentClass: Class[_ <: Agent], agent: Agent, radius: Double) {
        monitorManager.inspect(agentClass, agent, radius)
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
    }
    pico.addComponent(new EditorColorizer(workspace))
    pico.addComponent(new ShapeChangeListener() {
      def shapeChanged(shape: Shape) {workspace.shapeChanged(shape)}
      def shapeRemoved(shape: org.nlogo.api.Shape) {
        if (shape.isInstanceOf[org.nlogo.shape.LinkShape]) {
          workspace.world.linkBreedShapes.removeFromBreedShapes(shape.getName)
        }
        else workspace.world.turtleBreedShapes.removeFromBreedShapes(shape.getName)
      }
    })

    frame.addLinkComponent(workspace)

    dirtyMonitor = new DirtyMonitor(frame)
    frame.addLinkComponent(dirtyMonitor)

    monitorManager = pico.getComponent(classOf[AgentMonitorManager])
    frame.addLinkComponent(monitorManager)

    _tabs = pico.getComponent(classOf[Tabs])
    pico.addComponent(tabs.interfaceTab.getInterfacePanel)
    frame.getContentPane.add(tabs, java.awt.BorderLayout.CENTER)

    frame.addLinkComponent(new CompilerManager(workspace, tabs.proceduresTab))
    frame.addLinkComponent(listenerManager)

    org.nlogo.util.Exceptions.setHandler(this)

    if(loggingName != null)
     startLogging(loggingName)

  }

  private def finishStartup() {
    pico.addComponent(new MenuBarFactory())
    aggregateManager = pico.getComponent(classOf[AggregateManagerInterface])
    frame.addLinkComponent(aggregateManager)
    
    pico.addComponent(new EditorFactory(workspace))
    
    labManager = pico.getComponent(classOf[LabManagerInterface])
    frame.addLinkComponent(labManager)

    tabs.init(Plugins.load(pico): _*)

    val viewManager = pico.getComponent(classOf[GLViewManagerInterface])
    workspace.init(viewManager)
    frame.addLinkComponent(viewManager)    

    fileMenu = pico.getComponent(classOf[FileMenu])
    val menuBar = new JMenuBar(){
      add(fileMenu)
      add(new EditMenu(App.this))
      add(pico.getComponent(classOf[ToolsMenu]))
      add(new ZoomMenu(App.this))
      add(tabs.tabsMenu)
    }
    // a little ugly we have to typecast here, but oh well - ST 10/11/05
    helpMenu = new MenuBarFactory().addHelpMenu(menuBar).asInstanceOf[HelpMenu]
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
    smartPack(frame.getPreferredSize)

    if(! System.getProperty("os.name").startsWith("Mac")){ org.nlogo.awt.Positioning.center(frame, null) }
    
    org.nlogo.app.FindDialog.init(frame) 
    
    Splash.endSplash()
    frame.setVisible(true)
    if(System.getProperty("os.name").startsWith("Mac")){ MacHandlers.ready(this) }
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
  private class MenuBarFactory extends org.nlogo.window.MenuBarFactory{
    def createFileMenu:  JMenu = pico.getComponent(classOf[FileMenu])
    def createEditMenu:  JMenu = new EditMenu(App.this)
    def createToolsMenu: JMenu = new ToolsMenu(App.this)
    def createZoomMenu:  JMenu = new ZoomMenu(App.this)
    override def addHelpMenu(menuBar:JMenuBar) = {
      val newMenu = new HelpMenu (App.this, new EditorColorizer(workspace))
      menuBar.add(newMenu)
      try if(AbstractWorkspace.isApp) menuBar.setHelpMenu(newMenu)
      catch{
        // if not implemented in this VM (e.g. 1.4 on Mac as of right now),
        // then oh well - ST 6/23/03, 8/6/03
        case e: Error => org.nlogo.util.Exceptions.ignore(e)
      }
      newMenu
    }
  }

  ///
  private def loadDefaultModel(){
    if (commandLineModel != null) {
      if (commandLineModelIsLaunch) { // --launch through InstallAnywhere?
        // open up the blank model first so in case
        // the magic open fails for some reason
        // there's still a model loaded ev 3/7/06
        fileMenu.newModel()
        open(commandLineModel)
      }
      else libraryOpen(commandLineModel) // --open from command line
    }
    else if (commandLineMagic != null)
      workspace.magicOpen(commandLineMagic)
    else if (commandLineURL != null)
      fileMenu.openFromSource(
        org.nlogo.util.Utils.url2String(commandLineURL),
        null, "Starting...", ModelType.Library)
    else fileMenu.newModel()
  }

  /// zooming stuff
  private var zoomSteps = 0
  def zoomLarger(){ zoomSteps+=1; finishZoom() }
  def resetZoom() { zoomSteps=0; finishZoom() }
  def zoomSmaller() {
    zoomSteps-=1
    zoomSteps = StrictMath.max(-5, zoomSteps)
    finishZoom()
  }
  private def finishZoom() {
    new ZoomedEvent(1.0 + zoomSteps * 0.1).raise(this)
    smartPack(frame.getPreferredSize)
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
          logger.modelOpened(workspace.getModelPath())
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
      case CHANGE_LANGUAGE => changeLanguage()
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

  def changeLanguage() {
    val locales = I18N.availableLocales
    val languages = locales.map{l => l.getDisplayName(l) }
    val index = org.nlogo.swing.OptionDialog.showAsList(frame,
      "Change Language", "Choose a new language.", languages.asInstanceOf[Array[Object]])
    if(index > -1) {
      val chosenLocale = locales(index)
      val netLogoPrefs = java.util.prefs.Preferences.userRoot.node("/org/nlogo/NetLogo")
      netLogoPrefs.put("user.language", chosenLocale.getLanguage)
      netLogoPrefs.put("user.country", chosenLocale.getCountry)
    }
    val restart = "Langauge changed.\nYou must restart NetLogo for the changes to take effect."
    org.nlogo.swing.OptionDialog.show(frame, "Change Language", restart, Array(I18N.gui.get("common.buttons.ok")))
  }

  ///

  /**
   * Internal use only.
   */
  def handle(e: Events.SwitchedTabsEvent) {
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
      if (workspace.hubnetManager() != null) {
        workspace.hubnetManager().setTitle(workspace.modelNameForDisplay,
          workspace.getModelDir, workspace.getModelType)
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
    if(workspace.hubnetManager() != null) workspace.hubnetManager().closeClientEditor()
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
      if(wasAtPreferredSizeBeforeLoadBegan) smartPack(frame.getPreferredSize)
      else{
        val currentSize = frame.getSize
        val preferredSize = frame.getPreferredSize
        var newWidth = currentSize.width
        if(preferredSize.width > newWidth) newWidth = preferredSize.width
        var newHeight = currentSize.height
        if(preferredSize.height > newHeight) newHeight = preferredSize.height
        if(newWidth != currentSize.width || newHeight != currentSize.height) smartPack(new Dimension(newWidth, newHeight))
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
    if(workspace.getModelFileName() == null) "NetLogo"
    else{
      var title = workspace.modelNameForDisplay
      // on OS X, use standard window title format. otherwise use Windows convention
      if(! System.getProperty("os.name").startsWith("Mac")) title = title + " - " + "NetLogo"
      // 8212 is the unicode value for an em dash. we use the number since
      // we don't want non-ASCII characters in the source files -- AZS 6/14/2005
      else title = "NetLogo " + (8212.toChar) + " " + title

      // OS X UI guidelines prohibit paths in title bars, but oh well...
      if (workspace.getModelType() == ModelType.Normal) title += " {" + workspace.getModelDir() + "}"
      title 
    }
  }

  /**
   * Internal use only.
   */
  def handle(t:Throwable){
    try {
      val logo = t.isInstanceOf[LogoException]
      if (logo) {
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
            RuntimeErrorDialog.show("Runtime Error", null, null, Thread.currentThread, t))
      }
      else {
        t.printStackTrace(System.err)
        if(! org.nlogo.window.RuntimeErrorDialog.suppressJavaExceptionDialogs &&
          // Not entirely clear what to do if a second exception
          // comes in while the window is still up and showing a
          // previous one... for now, let's spit it to stdout but
          // otherwise ignore it - ST 6/10/02
          ! org.nlogo.window.RuntimeErrorDialog.alreadyVisible) {
          org.nlogo.awt.EventQueue.invokeLater(() =>
            RuntimeErrorDialog.show("Internal Error", null, null, Thread.currentThread, t))
        }
      }
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
  def open(path:String)  { dispatchThreadOrBust(fileMenu.openFromPath(path, ModelType.Normal)) }

  @throws(classOf[java.io.IOException])
  def libraryOpen(path:String){ dispatchThreadOrBust(path, ModelType.Library) }

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
    dispatchThreadOrBust(
      try fileMenu.openFromSource(source, path, "Loading...", modelType)
      catch{ case ex:UserCancelException => org.nlogo.util.Exceptions.ignore(ex) })
  }

  /**
   * Runs NetLogo commands and waits for them to complete.
   * <p>This method must <strong>not</strong> be called from the AWT event
   * queue thread or while that thread is blocked.
   * It is an error to do so.
   * @param source The command or commands to run
   * @throws org.nlogo.api.CompilerException if the code fails to compile
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
   * @throws org.nlogo.api.CompilerException if the code fails to compile
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
   *         java.lang.Boolean, java.lang.String, {@link org.nlogo.api.LogoList},
   *         {@link org.nlogo.api.Agent}, AgentSet, or Nobody
   * @throws org.nlogo.api.CompilerException if the code fails to compile
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
  def getProcedures: String = dispatchThreadOrBust(tabs.proceduresTab.innerSource)

  /**
   * Replaces the contents of the Code tab.
   * Does not recompile the model.
   * @param source new contents
   * @see #compile
   */
  def setProcedures(source:String) { dispatchThreadOrBust(tabs.proceduresTab.innerSource(source)) }

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
        catch { case ex: InterruptedException => org.nlogo.util.Exceptions.ignore(ex) }
      }
    }
  }

  /**
   * Adds new widget to Interface tab given its specification,
   * in the same (undocumented) format found in a saved model.
   * @param text the widget specification
   */
  def makeWidget(text:String){
    dispatchThreadOrBust( tabs.interfaceTab.getInterfacePanel.loadWidget(text.split("\n").toArray, Version.version) )
  }

  /// helpers for controlling methods

  private def findButton(name:String): ButtonWidget =
    tabs.interfaceTab.getInterfacePanel.getComponents
      .collect{case bw: ButtonWidget => bw}
      .find(_.displayName == name)
      .getOrElse{throw new IllegalArgumentException(
        "button '" + name + "' not found")}
  
  def smartPack(targetSize:Dimension) {
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
    
    // reduce our size ambitions if necessary
    var newWidth  = StrictMath.min(targetSize.width, maxWidth )
    var newHeight = StrictMath.min(targetSize.height, maxHeight)
    
    // move up/left to get more room if possible and necessary
    val moveLeft = StrictMath.max(0, frame.getLocation().x + newWidth  - maxX)
    val moveUp   = StrictMath.max(0, frame.getLocation().y + newHeight - maxY)
    
    // now we can compute our new position
    val newX = StrictMath.max(maxBoundsX, frame.getLocation().x - moveLeft)
    val newY = StrictMath.max(maxBoundsY, frame.getLocation().y - moveUp  )
    
    // and now that we know our position, we can compute our new size
    newWidth  = StrictMath.min(newWidth, maxX - newX)
    newHeight = StrictMath.min(newHeight, maxY - newY)
    
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
  def showAboutWindow() { new AboutWindow(frame).setVisible(true) }

  /**
   * Internal use only.
   */
  def handle(e:LoadSectionEvent){
    if(e.section == ModelSection.HubNetClient && e.lines.length > 0)
      frame.addLinkComponent(workspace.getHubNetManager.clientEditor)
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
}

class AppFrame extends JFrame with LinkParent {
  setIconImage(org.nlogo.awt.Images.loadImageResource("/images/arrowhead.gif"))
  setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE)
  getContentPane.setLayout(new java.awt.BorderLayout)
  org.nlogo.awt.FullScreenUtilities.setWindowCanFullScreen(this, true)
  private val linkComponents = new collection.mutable.ListBuffer[Object]()
  addWindowListener(new WindowAdapter() {
    override def windowClosing(e: WindowEvent) {
      try App.app.fileMenu.quit()
      catch {case ex: UserCancelException => org.nlogo.util.Exceptions.ignore(ex)}
    }
    override def windowIconified(e: WindowEvent) {new IconifiedEvent(AppFrame.this, true).raise(App.app)}
    override def windowDeiconified(e: WindowEvent) {new IconifiedEvent(AppFrame.this, false).raise(App.app)}
  })
  def addLinkComponent(c:Object) { linkComponents += (c) }
  def getLinkChildren: Array[Object] = linkComponents.toArray
}
