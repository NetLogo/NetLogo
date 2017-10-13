// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{ Agent, World }
import org.nlogo.api.{ CompilerServices, FileIO, HubNetInterface,
  HubNetWorkspaceInterface, LogoException, LogoThunkFactory, PreviewCommands,
  SourceOwner, Workspace => APIWorkspace, WorldPropertiesInterface }
import org.nlogo.core.{ CompilerException, Model, View, Widget => CoreWidget }
import org.nlogo.nvm.{ Command, Context, EditorWorkspace, FileManager,
  Instruction, Job, JobManagerInterface, JobManagerOwner, Linker, LoggingWorkspace,
  MutableLong, PresentationCompilerInterface,
  Procedure, RuntimePrimitiveException, Tracer, Workspace => NvmWorkspace }

import collection.mutable.WeakHashMap
import java.io.IOException
import java.nio.file.Paths

import scala.util.Try

import AbstractWorkspaceTraits._

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
  with BaseWorkspace
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
  with BehaviorSpaceInformation
  with Traceable with HubNetManager
  with Components
  with Exporting
  with Importing
  with Plotting
  with FileManagement
  with ModelTracking
  with DeprecatedCompilerServices {
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

    // called when the engine comes up for air
    def breathe(context: Context): Unit

    // called by _display from job thread
    def requestDisplayUpdate(force: Boolean): Unit

    messageCenter.subscribe(this)

    private val ownersMap = sourceOwners.map(o => o.classDisplayName -> o).toMap

    // used by `_every`
    val lastRunTimes: WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]] =
      new WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]()

    def resetTicks(context:Context): Unit = {
      world.tickCounter.reset()
      setupPlots(context)
      updatePlots(context)
      requestDisplayUpdate(true)
    }

    def clearTicks(): Unit = {
      world.tickCounter.clear()
    }

    def clearAll(): Unit = {
      world.clearAll()
      clearOutput()
      clearDrawing()
      plotManager.clearAll()
      extensionManager.clearAll()
    }

    @throws(classOf[InterruptedException])
    def dispose(): Unit = {
      disposeComponents()
      jobManager.die()
      getExtensionManager.reset()
      messageCenter.removeSubscriptions()
    }

    def halt(): Unit = {
      jobManager.haltPrimary()
      enablePeriodicRendering()
    }

    // used to allow initialization of procedures without a direct dependency on workspace
    lazy val linker = new Linker {
      def link(p: Procedure): Procedure = {
        p.init(AbstractWorkspace.this)
        p
      }
    }

    def seedRNGs(seed: Int): Unit = {
      mainRNG.setSeed(seed)
      auxRNG.setSeed(seed)
      plotRNG.setSeed(seed)
    }

    /**
     * Displays a warning to the user, and determine whether to continue.
     * The default (non-GUI) implementation is to print the warning and
     * always continue.
     */
    def warningMessage(message: String): Boolean =
      userInteraction.warningMessage(message)

    protected def loadFromModel(model: Model): Unit = {
      modelTracker.updateModel(m => model)
      model.optionalSectionValue[PreviewCommands]("org.nlogo.modelsection.previewcommands").foreach { pc =>
        previewCommands = pc
      }
      model.optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient").foreach { hnWidgets =>
        if (hnWidgets.nonEmpty)
          getHubNetManager.foreach(_.load(model))
      }
    }

    def loadWorld(view: View, worldInterface: WorldLoaderInterface): Unit = {
      val loader = new WorldLoader()
      loader.load(view, worldInterface)
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
            val modelPath = Option(modelTracker.getModelPath)
              .flatMap(s => Try(Paths.get(s)).toOption)
              FileIO.resolvePath(path, modelPath).map(_.normalize.toString).getOrElse(path)
            } catch {
              case ex: Exception =>
                throw new IllegalStateException(s"$path is not a valid pathname: $ex")
            }
        }
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
  }

object AbstractWorkspaceTraits {
  trait BaseWorkspace extends WorkspaceMessageListener {
    def codeBits: WeakHashMap[String, Procedure]
    def compiler: PresentationCompilerInterface
    def compilerServices: CompilerServices
    def evaluator: Evaluator
    def hubNetManagerFactory: HubNetManagerFactory
    def jobManager: JobManagerInterface
    def messageCenter: WorkspaceMessageCenter
    def owner: JobManagerOwner
    def modelTracker: ModelTracker
    def userInteraction: UserInteraction
    def world: World
    def extensionManager: ExtensionManager
    def sourceOwners: Seq[SourceOwner]
    def isHeadless: Boolean

    private var _compilerTestingMode = false

    def compilerTestingMode: Boolean = _compilerTestingMode

    def compilerTestingMode_=(isOn: Boolean): Unit = {
      _compilerTestingMode = isOn
      messageCenter.send(ToggleCompilerTesting(isOn))
    }

    var previewCommands: PreviewCommands = PreviewCommands.Default

    def processWorkspaceEvent(evt: WorkspaceEvent): Unit = {
      evt match {
        case (_: ModelCompiledSuccess | _: ModelCompiledFailure) =>
          codeBits.clear()
        case SwitchModel(_, _) =>
          jobManager.haltSecondary()
          jobManager.haltPrimary()
          previewCommands = PreviewCommands.Default
        case _ =>
      }
    }

    // job / engine management

    def joinForeverButtons(agent: Agent): Unit = jobManager.joinForeverButtons(agent)
    def addJobFromJobThread(job: Job): Unit = jobManager.addJobFromJobThread(job)
  }

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

  trait CompileSupport extends BaseWorkspace with WorkspaceMessageListener { this: AbstractWorkspace =>
    val dialect = compiler.dialect

    // this is used to cache the compiled code used by the "run"
    // and "runresult" prims - ST 6/7/07
    private val _codeBits: WeakHashMap[String, Procedure] = new WeakHashMap[String, Procedure]()

    def codeBits: WeakHashMap[String, Procedure] = _codeBits

    def clearRunCache(): Unit = {
      codeBits.clear()
    }

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
    override def readNumberFromString(source: String): java.lang.Double =
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

    override def processWorkspaceEvent(e: WorkspaceEvent): Unit = {
      super.processWorkspaceEvent(e)
      e match {
        case SwitchModel(_, _) => setProcedures(Procedure.NoProcedures)
        case _ =>
      }
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
  }

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

  trait DeprecatedCompilerServices { this: AbstractWorkspace =>
    import org.nlogo.core.{ ProcedureSyntax, Token }
    @deprecated("Workspace.checkCommandSyntax is deprecated, use Workspace.compilerServices.checkCommandSyntax", "6.1.0")
    def checkCommandSyntax(source: String): Unit = compilerServices.checkCommandSyntax(source)
    @deprecated("Workspace.checkReporterSyntax is deprecated, use Workspace.compilerServices.checkReporterSyntax", "6.1.0")
    def checkReporterSyntax(source: String): Unit = compilerServices.checkReporterSyntax(source)
    @deprecated("Workspace.findProcedurePositions is deprecated, use Workspace.compilerServices.findProcedurePositions", "6.1.0")
    def findProcedurePositions(source: String): Map[String, ProcedureSyntax] =
      compilerServices.findProcedurePositions(source)
    @deprecated("Workspace.getTokenAtPosition is deprecated, use Workspace.compilerServices.getTokenAtPosition", "6.1.0")
    def getTokenAtPosition(source: String, position: Int): Token =
      compilerServices.getTokenAtPosition(source, position)
    @deprecated("Workspace.isConstant is deprecated, use Workspace.compilerServices.isConstant", "6.1.0")
    def isConstant(s: String): Boolean = compilerServices.isConstant(s)
    @deprecated("Workspace.isReporter is deprecated, use Workspace.compilerServices.isReporter", "6.1.0")
    def isReporter(s: String): Boolean = compilerServices.isReporter(s)
    @deprecated("Workspace.isValidIdentifier is deprecated, use Workspace.compilerServices.isValidIdentifier", "6.1.0")
    def isValidIdentifier(s: String): Boolean = compilerServices.isValidIdentifier(s)
    @deprecated("Workspace.tokenizeForColorization is deprecated, use Workspace.compilerServices.tokenizeForColorization", "6.1.0")
    def tokenizeForColorization(source: String): Array[Token] = compilerServices.tokenizeForColorization(source)
    @deprecated("Workspace.tokenizeForColorizationIterator is deprecated, use Workspace.compilerServices.tokenizeForColorizationIterator", "6.1.0")
    def tokenizeForColorizationIterator(source: String): Iterator[Token] =
      compilerServices.tokenizeForColorizationIterator(source)
  }

  trait Traceable extends BaseWorkspace {
    private var tracer: Tracer = null

    def profilingTracer: Tracer = tracer

    def profilingEnabled: Boolean =
      tracer != null

    def setProfilingTracer(t: Tracer): Unit = {
      tracer = t
    }

    abstract override def processWorkspaceEvent(e: WorkspaceEvent): Unit = {
      super.processWorkspaceEvent(e)
      e match {
        case AddInstrumentation("tracer", t: Tracer, _) =>
          setProfilingTracer(t)
        case RemoveInstrumentation("tracer", _) =>
          setProfilingTracer(null)
        case _ =>
      }
    }
  }

  trait HubNetManager extends Components with WorkspaceMessageListener { self: AbstractWorkspace =>
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

    def hubNetInitialized: Boolean =
      isComponentInitialized(classOf[HubNetInterface])

    abstract override def processWorkspaceEvent(e: WorkspaceEvent): Unit = {
      super.processWorkspaceEvent(e)
      e match {
        case SwitchModel(_, _) if hubNetInitialized =>
          hubNetManager.foreach { m =>
            m.disconnect()
            m.closeClientEditor()
          }
        case _ =>
      }
    }
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
