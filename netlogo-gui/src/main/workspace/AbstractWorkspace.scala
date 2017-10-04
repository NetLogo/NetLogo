// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{ Agent, World }
import org.nlogo.api.{ AggregateManagerInterface, FileIO, HubNetInterface, HubNetWorkspaceInterface,
  LogoException, LogoThunkFactory, PreviewCommands, Workspace => APIWorkspace, WorldPropertiesInterface }
import org.nlogo.core.{ CompilerException, Femto, Model, View, Widget => CoreWidget }
import org.nlogo.nvm.{ Command, Context, EditorWorkspace, FileManager,
  Instruction, Job, JobManagerInterface, JobManagerOwner, LoggingWorkspace,
  MutableLong, PresentationCompilerInterface, Procedure, RuntimePrimitiveException, Tracer, Workspace => NvmWorkspace }

import collection.mutable.WeakHashMap
import java.io.IOException
import java.nio.file.Paths

import scala.util.Try

import AbstractWorkspaceTraits._

trait HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspace): HubNetInterface
}

abstract class DefaultAbstractWorkspace(deps: WorkspaceDependencies) extends AbstractWorkspace(deps) {
  def this(_world: World,
    _compiler: PresentationCompilerInterface,
    _hubNetManagerFactory: HubNetManagerFactory,
    aggregateManager: AggregateManagerInterface) = this(
      new WorkspaceDependencies {
        val compiler: PresentationCompilerInterface = _compiler
        val world: World = _world
        val hubNetManagerFactory: HubNetManagerFactory = _hubNetManagerFactory
        val userInteraction: UserInteraction = DefaultUserInteraction
        val messageCenter: WorkspaceMessageCenter = new WorkspaceMessageCenter()
        val owner: JobManagerOwner = new HeadlessJobManagerOwner(messageCenter)
        val modelTracker: ModelTracker = new ModelTrackerImpl(messageCenter)
        val jobManager = Femto.get[JobManagerInterface]("org.nlogo.job.JobManager", owner, world)
        val evaluator = new Evaluator(jobManager, compiler, world)
        val extensionManager = new ExtensionManager(userInteraction, evaluator, messageCenter, new JarLoader(modelTracker))
        val compilerServices =
          new LiveCompilerServices(compiler, extensionManager, world, evaluator)
        val sourceOwners = Seq(aggregateManager)
      })
}

abstract class AbstractWorkspace(private val deps: WorkspaceDependencies)
  extends {
    val compiler: PresentationCompilerInterface = deps.compiler
    val compilerServices = deps.compilerServices
    val evaluator: Evaluator = deps.evaluator
    val hubNetManagerFactory: HubNetManagerFactory = deps.hubNetManagerFactory
    val jobManager = deps.jobManager
    val messageCenter: WorkspaceMessageCenter = deps.messageCenter
    val owner: JobManagerOwner = deps.owner
    val modelTracker: ModelTracker = deps.modelTracker
    val userInteraction: UserInteraction = deps.userInteraction
    val world: World = deps.world
    val extensionManager: ExtensionManager = deps.extensionManager
    val sourceOwners = deps.sourceOwners
  }
  with APIWorkspace
  with NvmWorkspace
  with EditorWorkspace
  with LoggingWorkspace
  with LogoThunkFactory
  with HubNetWorkspaceInterface
  with ScalaAccessors
  with WorldProxyMethods
  with CompileSupport
  with APIConformant with Benchmarking
  with Checksums with Evaluating
  with Procedures
  with BehaviorSpaceInformation
  with Traceable with HubNetManager
  with Components
  with Exporting
  with Importing
  with Plotting
  with FileManagement
  with ModelTracking
  with WorkspaceMessageListener {

  def isHeadless: Boolean

  messageCenter.subscribe(this)

  private var _compilerTestingMode = false

  private val ownersMap = sourceOwners.map(o => o.classDisplayName -> o).toMap

  def compilerTestingMode: Boolean = _compilerTestingMode

  def compilerTestingMode_=(isOn: Boolean): Unit = {
    _compilerTestingMode = isOn
    messageCenter.send(ToggleCompilerTesting(isOn))
  }

  var previewCommands: PreviewCommands = PreviewCommands.Default

  // used by `_every`
  val lastRunTimes: WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]] =
    new WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]()

  // used to allow initialization of procedures without a direct dependency on workspace
  lazy val linker = new Evaluator.Linker {
    def link(p: Procedure): Procedure = {
      p.init(AbstractWorkspace.this)
      p
    }
  }

  // the original instruction here is _tick or a ScalaInstruction (currently still experimental)
  // it is only ever used if we need to generate an EngineException
  // the version of EngineException that takes an instruction is to be *very strongly* preferred.
  // otherwise we don't get accurate runtime error locations
  // we pass in the Instruction so that we dont have to duplicate the exception logic in both locations.
  // JC 5/19/10
  def tick(context: Context, originalInstruction: Instruction) {
    if(world.tickCounter.ticks == -1)
      throw new RuntimePrimitiveException(context, originalInstruction,
        "The tick counter has not been started yet. Use RESET-TICKS.")
    world.tickCounter.tick()
    updatePlots(context)
    requestDisplayUpdate(true)
  }

  def resetTicks(context:Context) {
    world.tickCounter.reset()
    setupPlots(context)
    updatePlots(context)
    requestDisplayUpdate(true)
  }

  def clearTicks {
    world.tickCounter.clear()
  }

  def clearAll {
    world.clearAll()
    clearOutput()
    clearDrawing()
    plotManager.clearAll()
    extensionManager.clearAll()
  }

  def loadWorld(view: View, worldInterface: WorldLoaderInterface): Unit = {
    val loader = new WorldLoader()
    loader.load(view, worldInterface)
  }

  def seedRNGs(seed: Int): Unit = {
    mainRNG.setSeed(seed)
    auxRNG.setSeed(seed)
    plotRNG.setSeed(seed)
  }

  @throws(classOf[IOException])
  def getSource(filename: String): String = {
    if (ownersMap.contains(filename)) {
      ownersMap(filename).innerSource
    } else {
      // when we stick a string into a JTextComponent, \r\n sequences
      // on Windows will get translated to just \n.  This is a problem
      // because when an error occurs we want to highlight the location
      // using the token location information recorded by the tokenizer,
      // but the removal of the \r characters will throw off that information.
      // So we do the stripping of \r here, *before* we run the tokenizer,
      // and that avoids the problem. - ST 9/14/04

      val sourceFile = new org.nlogo.api.LocalFile(filename)
      sourceFile.open(org.nlogo.core.FileMode.Read)
      val source = org.nlogo.api.FileIO.reader2String(sourceFile.reader)
      source.replaceAll("\r\n", "\n")
    }
  }

  // NOTE: It would probably be possible to inject this as a parameter at some point - RG 10/6/17
  override def getCompilationEnvironment = {
    new org.nlogo.core.CompilationEnvironment {
      def getSource(filename: String): String = AbstractWorkspace.this.getSource(filename)
      def profilingEnabled: Boolean = AbstractWorkspace.this.profilingEnabled
      def resolvePath(path: String): String = {
        try {
          val modelPath = Option(AbstractWorkspace.this.getModelPath)
            .flatMap(s => Try(Paths.get(s)).toOption)
          FileIO.resolvePath(path, modelPath).map(_.normalize.toString).getOrElse(path)
        } catch {
          case ex: Exception =>
            throw new IllegalStateException(s"$path is not a valid pathname: $ex")
        }
      }
    }
  }

  protected def loadFromModel(model: Model): Unit = {
    model.optionalSectionValue[PreviewCommands]("org.nlogo.modelsection.previewcommands").foreach { pc =>
      previewCommands = pc
    }
    model.optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient").foreach { hnWidgets =>
      if (hnWidgets.nonEmpty)
        getHubNetManager.foreach(_.load(model))
    }
  }

  /**
   * Displays a warning to the user, and determine whether to continue.
   * The default (non-GUI) implementation is to print the warning and
   * always continue.
   */
  def warningMessage(message: String): Boolean =
    userInteraction.warningMessage(message)

  def processWorkspaceEvent(evt: WorkspaceEvent): Unit = {
    evt match {
      case (_: ModelCompiledSuccess | _: ModelCompiledFailure) =>
        codeBits.clear()
      case AddInstrumentation("tracer", t: Tracer, _) =>
        setProfilingTracer(t)
      case RemoveInstrumentation("tracer", _) =>
        setProfilingTracer(null)
      case _ =>
    }
  }

  @throws(classOf[InterruptedException])
  def dispose(): Unit = {
    disposeComponents()
    jobManager.die()
    getExtensionManager.reset()
    messageCenter.removeSubscriptions()
  }

  // random abstract methods
  def clearDrawing(): Unit
  def magicOpen(name: String): Unit

  // called from an "other" thread (neither event thread nor job thread)
  @throws(classOf[IOException])
  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def open(path: String)

  @throws(classOf[CompilerException])
  @throws(classOf[LogoException])
  def openString(modelContents: String)

  // job / engine management

  def joinForeverButtons(agent: Agent): Unit = jobManager.joinForeverButtons(agent)
  def addJobFromJobThread(job: Job): Unit = jobManager.addJobFromJobThread(job)

  // called when the engine comes up for air
  def breathe(context: Context): Unit

  // called by _display from job thread
  def requestDisplayUpdate(force: Boolean): Unit

  def halt(): Unit = {
    jobManager.haltPrimary()
    enablePeriodicRendering()
  }
}

object AbstractWorkspaceTraits {
  trait Benchmarking { this: AbstractWorkspace =>
    def benchmark(minTime: Int, maxTime: Int) {
      new Thread("__bench") {
        override def run() {
          Benchmarker.benchmark(
            Benchmarking.this, minTime, maxTime)
        }}.start()
    }
  }

  trait Checksums { this: AbstractWorkspace =>
    override def worldChecksum =
      Checksummer.calculateWorldChecksum(this)
    override def graphicsChecksum =
      Checksummer.calculateGraphicsChecksum(this)
  }

  trait CompileSupport { this: AbstractWorkspace =>
    val dialect = compiler.dialect

    // this is used to cache the compiled code used by the "run"
    // and "runresult" prims - ST 6/7/07
    private val _codeBits: WeakHashMap[String, Procedure] = new WeakHashMap[String, Procedure]()

    def codeBits: WeakHashMap[String, Procedure] = _codeBits

    @throws(classOf[CompilerException])
    def compileForRun(source: String, context: Context, reporter: Boolean): Procedure = {
      val key = s"${source}@${context.activation.procedure.args.size}@${context.agentBit}"
      val storedProc = _codeBits.get(key)
      storedProc.getOrElse {
        val proc = evaluator.compileForRun(source, context, reporter)(getExtensionManager, getCompilationEnvironment, procedures, linker)
        _codeBits.put(key, proc)
        proc
      }
    }

    def readFromString(s: String): AnyRef =
      compilerServices.readFromString(s)
    def readNumberFromString(source: String): AnyRef =
      compilerServices.readNumberFromString(source)

    def procedures: Procedure.ProceduresMap = compilerServices.procedures
    def procedures_=(procs: Procedure.ProceduresMap): Unit = {
      compilerServices.procedures = procs
    }
    def setProcedures(procs: Procedure.ProceduresMap): Unit =
      compilerServices.setProcedures(procs)

    def init(): Unit = {
      procedures.values.foreach(_.init(this))
    }
  }

  trait APIConformant {
    def world: World
    // Members declared in org.nlogo.api.ViewSettings
    def drawSpotlight: Boolean = true
    private var _fontSize = 13
    def fontSize = _fontSize
    def fontSize(i: Int) { _fontSize = i }
    def perspective: org.nlogo.api.Perspective = world.observer.perspective
    def renderPerspective: Boolean = true
    def viewHeight: Double = world.patchSize * world.worldHeight
    def viewOffsetX: Double = world.patchSize * world.followOffsetX
    def viewOffsetY: Double = world.patchSize * world.followOffsetY
    def viewWidth: Double = world.patchSize * world.worldWidth

    // Members declared in org.nlogo.api.Workspace
    def previewCommandsString: String = ???
  }

  trait Procedures 

  trait BehaviorSpaceInformation {
    var _behaviorSpaceRunNumber = 0

    var _behaviorSpaceExperimentName = ""

    def behaviorSpaceRunNumber: Int =
      _behaviorSpaceRunNumber

    def behaviorSpaceExperimentName: String =
      _behaviorSpaceExperimentName

    def behaviorSpaceRunNumber(n: Int): Unit = {
      _behaviorSpaceRunNumber = n
    }

    def behaviorSpaceExperimentName(name: String): Unit = {
      _behaviorSpaceExperimentName = name
    }
  }

  trait Traceable {
    private var tracer: Tracer = null

    def profilingTracer: Tracer = tracer

    def profilingEnabled: Boolean =
      tracer != null

    def setProfilingTracer(t: Tracer): Unit = {
      tracer = t
    }
  }

  trait HubNetManager extends Components { self: AbstractWorkspace =>
    def hubNetManagerFactory: HubNetManagerFactory

    def getPropertiesInterface: WorldPropertiesInterface = null

    private var _hubNetRunning: Boolean = false

    if (hubNetManagerFactory != null) {
      addLifecycle(
        new ComponentLifecycle[HubNetInterface] {
          val klass = classOf[HubNetInterface]

          override def create(): Option[HubNetInterface] =
            Some(hubNetManagerFactory.newInstance(self))

          override def dispose(hubNet: HubNetInterface): Unit = {
            hubNet.disconnect()
          }
        })
    }

    def hubNetRunning = _hubNetRunning

    def hubNetRunning_=(running: Boolean): Unit = {
      _hubNetRunning = running;
    }

    def hubNetManager = getHubNetManager

    def getHubNetManager: Option[HubNetInterface] =
      getComponent(classOf[HubNetInterface])
  }


  trait FileManagement {
    def compiler: PresentationCompilerInterface
    def extensionManager: ExtensionManager
    def getModelDir: String
    def messageCenter: WorkspaceMessageCenter
    def modelTracker: ModelTracker
    val fileManager: FileManager = {
      val dfm = new DefaultFileManager(modelTracker, extensionManager, compiler.utilities)
      messageCenter.subscribe(dfm)
      dfm
    }
  }

  trait WorldProxyMethods {
    def world: World
    def auxRNG = world.auxRNG
    def mainRNG = world.mainRNG
  }

  trait ScalaAccessors { this: AbstractWorkspace =>
    def getExtensionManager: ExtensionManager = extensionManager
    def isExtensionName(name: String): Boolean = extensionManager.isExtensionName(name)

    @throws(classOf[org.nlogo.api.ExtensionException])
    def importExtensionData(name: String, data: java.util.List[Array[String]], handler: org.nlogo.api.ImportErrorHandler): Unit = {
      extensionManager.importExtensionData(name, data, handler)
    }
  }
}
