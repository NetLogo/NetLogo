package org.nlogo.ls.gui

import java.awt.EventQueue.isDispatchThread
import java.awt.event.KeyAdapter
import java.awt.image.BufferedImage
import java.nio.file.Paths
import javax.swing.JFrame

import org.nlogo.agent.{ Agent, CompilationManagement, World, World2D, World3D }
import org.nlogo.api.{ Agent => APIAgent, AppInterface, ControlSet, LabProtocol, ModelCoordinator, ModelType,
                       NetLogoLegacyDialect, NetLogoThreeDDialect, ProceduresInterface, Version }
import org.nlogo.app.codetab.ExternalFileManager
import org.nlogo.app.tools.AgentMonitorManager
import org.nlogo.awt.EventQueue
import org.nlogo.compile.Compiler
import org.nlogo.core.{ AgentKind, CompilerUtilitiesInterface, Model, Widget }
import org.nlogo.gl.view.ViewManager
import org.nlogo.nvm.PresentationCompilerInterface
import org.nlogo.sdm.AggregateManagerLite
import org.nlogo.swing.{ BoxAlign, BoxRow }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.Events.CompiledEvent
import org.nlogo.window.{ CompilerManager, DefaultEditorFactory, ErrorDialogManager, Event, FileController,
                          GUIWorkspace, InterfacePanelLite, LinkRoot, NetLogoListenerManager, OutputWidget,
                          ReconfigureWorkspaceUI, UpdateManager }
import org.nlogo.workspace.OpenModelFromURI
import org.nlogo.fileformat.FileFormat

import scala.concurrent.{ Future, Promise }
import scala.util.Try

class InterfaceComponent(app: AppInterface, frame: JFrame)
  extends BoxRow(BoxAlign.Start) with Event.LinkParent with LinkRoot with ControlSet with ModelCoordinator {

  val listenerManager = new NetLogoListenerManager
  val world: World = if(Version.is3D) new World3D() else new World2D()

  // KioskLevel.None - We want a 3d button
  val workspace: GUIWorkspace = new GUIWorkspace(Option(app), world, GUIWorkspace.KioskLevel.None, frame, frame, null,
                                                 new ExternalFileManager, listenerManager,
                                                 new ErrorDialogManager(frame), this) {

    val compiler: PresentationCompilerInterface =
      new Compiler(if (Version.is3D) NetLogoThreeDDialect else NetLogoLegacyDialect)

    override val utilities: CompilerUtilitiesInterface = compiler.utilities

    override val compilerManager = new CompilerManager(this, this.world.asInstanceOf[World & CompilationManagement]) {
      override def procedures: ProceduresInterface =
        InterfaceComponent.this.procedures
    }

    lazy val updateManager = new UpdateManager {
      override def defaultFrameRate = workspace.frameRate
      override def ticks = workspace.world.tickCounter.ticks
      override def updateMode = workspace.updateMode()
    }

    val aggregateManager = new AggregateManagerLite

    override def inspectAgent(agent: APIAgent, radius: Double) = {
      val a = agent.asInstanceOf[Agent]
      monitorManager.inspect(a.kind, a, radius)
    }
    override def inspectAgent(kind: AgentKind, agent: Agent, radius: Double) =
      monitorManager.inspect(kind, agent, radius)
    override def stopInspectingAgent(agent: Agent): Unit = monitorManager.stopInspecting(agent)
    override def stopInspectingDeadAgents(): Unit = monitorManager.stopInspectingDeadAgents()
    override def closeAgentMonitors() = monitorManager.closeAll()
    override def newRenderer = new org.nlogo.render.Renderer(this.world)
    override def updateModel(m: Model): Model = m

    override def tickStateChanged(initialized: Boolean): Unit = {
      interfacePanel.tickStateChanged(initialized)
    }
  }

  val monitorManager = new AgentMonitorManager(workspace)
  addLinkComponent(monitorManager)

  val procedures = new Procedures(workspace.compilerManager)
  val liteEditorFactory = new DefaultEditorFactory(workspace)
  val interfacePanel = new InterfacePanelLite(workspace.viewWidget, workspace, workspace, workspace,
                                              workspace.plotManager, liteEditorFactory, workspace.getExtensionManager,
                                              workspace.compilerManager)

  val viewManager = new ViewManager(app, workspace, interfacePanel, frame, new KeyAdapter {})

  workspace.init(viewManager)
  addLinkComponent(viewManager)

  addLinkComponent(workspace.aggregateManager)
  addLinkComponent(workspace)
  addLinkComponent(workspace.compilerManager)
  addLinkComponent(new CompiledEvent.Handler {
    override def handle(e: CompiledEvent): Unit = {
      if (e.error != null)
        throw e.error
  }})
  addLinkComponent(listenerManager)

  add(interfacePanel)

  def open(path: String) = {
    EventQueue.mustBeEventDispatchThread()
    val uri = Paths.get(path).toUri
    interfacePanel.reset()
    val controller = new FileController(this, workspace)
    val loader = FileFormat.standardAnyLoader(false, workspace.compiler.utilities)
    val modelOpt = OpenModelFromURI(uri, controller, loader, FileFormat.defaultConverter, Version)
    val protocols = modelOpt.flatMap(model => {
      ReconfigureWorkspaceUI(this, this, uri, ModelType.Library, model, workspace)

      model.optionalSectionValue[Seq[LabProtocol]]("org.nlogo.modelsection.behaviorspace")
    }).getOrElse(Seq[LabProtocol]())
    workspace.getExperimentManager.setGUIExperiments(protocols)
  }

  def userInterface: Future[BufferedImage] = {
    if (isDispatchThread)
      Promise.fromTry(Try(interfacePanel.interfaceImage)).future
    else {
      val promise = Promise[BufferedImage]()
      EventQueue.invokeLater { () =>
        promise.complete(Try(interfacePanel.interfaceImage))
        ()
      }
      promise.future
    }
  }

  def userOutput: Future[String] = {
    def findOutput(ipl: InterfacePanelLite): String =
      ipl.getComponents.collect {
        case ow: OutputWidget => ow.valueText
      }.headOption.getOrElse("")
    if (isDispatchThread)
      Promise.fromTry(Try(findOutput(interfacePanel))).future
    else {
      val promise = Promise[String]()
      EventQueue.invokeLater { () =>
        promise.complete(Try(findOutput(interfacePanel)))
        ()
      }
      promise.future
    }
  }

  override def beforeLoad(modelPath: Option[String], modelType: ModelType): Unit = {
    workspace.beforeLoad(modelPath, modelType)
  }

  override def loadModel(model: Model): Unit = {
    workspace.loadModel(model)
    procedures.loadModel(model)
  }

  override def loadWidgets(widgets: Seq[Widget], convertWidgetSizes: Boolean): Unit = {
    interfacePanel.loadWidgets(widgets)
  }

  override def loadEnd(shouldAutoInstallLibs: Boolean): Unit = {}

  override def afterLoad(): Unit = {
    interfacePanel.afterLoad()
    workspace.afterLoad()
  }

  override def modelSaved(modelPath: String, isNew: Boolean): Unit = {
    workspace.modelSaved(modelPath)
  }

  def syncTheme(): Unit = {
    setBackground(InterfaceColors.interfaceBackground())

    interfacePanel.syncTheme()
    monitorManager.syncTheme()
    viewManager.syncTheme()
    viewManager.repaint()
  }
}
