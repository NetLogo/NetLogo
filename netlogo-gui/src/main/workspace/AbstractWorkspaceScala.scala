// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.ByteArrayInputStream
import java.io.IOException
import java.nio.file.{ Path, Paths }
import java.util.Base64

import scala.collection.mutable.WeakHashMap
import scala.util.Try

import org.nlogo.agent.{ World, Agent, OutputObject }
import org.nlogo.api.{ Dump, ExtensionManager => APIEM, FileIO, HubNetInterface, LibraryManager,
  LogoException, OutputDestination, PreviewCommands, Workspace => APIWorkspace, WorldDimensions3D }
import org.nlogo.core.{ CompilerException, Model, View, Widget => CoreWidget, WorldDimensions }
import org.nlogo.nvm.{ Activation, Instruction, Command, Context, Job, MutableLong, Procedure, Tracer }
import org.nlogo.nvm.RuntimePrimitiveException

import AbstractWorkspaceTraits._

trait HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspaceScala): HubNetInterface
}

abstract class AbstractWorkspaceScala(val world: World, val hubNetManagerFactory: HubNetManagerFactory)
  extends AbstractWorkspace(world)
  with APIConformant with Benchmarking
  with Checksums with Evaluating
  with ModelTracker
  with Procedures
  with Compiling
  with BehaviorSpaceInformation
  with Traceable with HubNetManager
  with Components
  with ExtendableWorkspaceMethods with Exporting
  with Plotting
  with Extensions {

  private val libraryManager = new LibraryManager(APIEM.userExtensionsPath, extensionManager.reset _)

  def isHeadless: Boolean

  def compilerTestingMode: Boolean

  var previewCommands: PreviewCommands = PreviewCommands.Default

  // used by `_every`
  val lastRunTimes: WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]] =
    new WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]()

  override def getLibraryManager = libraryManager

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
    val loader = view.dimensions match {
      case d: WorldDimensions3D => new WorldLoader3D()
      case d: WorldDimensions   => new WorldLoader()
    }
    loader.load(view, worldInterface)
  }

  def seedRNGs(seed: Int): Unit = {
    mainRNG.setSeed(seed)
    auxRNG.setSeed(seed)
    plotRNG.setSeed(seed)
  }

  // called from an "other" thread (neither event thread nor job thread)
  @throws(classOf[CompilerException])
  @throws(classOf[IOException])
  @throws(classOf[LogoException])
  def open(path: String, shouldAutoInstallLibs: Boolean = false): Unit

  @throws(classOf[IOException])
  def getSource(filename: String): String = {
    // this `filename ==` feels very hacky. We should look for a way to pass
    // in source-providing components without needing to special-case them
    // here - RG 12/19/16
    if (filename == "aggregate") {
      aggregateManager.innerSource
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

  @throws(classOf[java.io.IOException])
  def importDrawingBase64(base64: String): Unit = {
    val MimeRegex = "data:(.*);base64".r
    val Array(MimeRegex(contentType), byteString) = base64.split(",")
    val bytes = Base64.getDecoder.decode(byteString)
    importDrawing(new ByteArrayInputStream(bytes), Option(contentType))
  }

  override def getCompilationEnvironment = {
    new org.nlogo.core.CompilationEnvironment {
      def getSource(filename: String): String = AbstractWorkspaceScala.this.getSource(filename)
      def profilingEnabled: Boolean = AbstractWorkspaceScala.this.profilingEnabled
      def resolvePath(path: String): String = {
        try {
          val modelPath = Option(AbstractWorkspaceScala.this.getModelPath)
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

  trait Checksums { this: AbstractWorkspace with APIWorkspace =>
    override def worldChecksum =
      Checksummer.calculateWorldChecksum(this)
    override def graphicsChecksum =
      Checksummer.calculateGraphicsChecksum(this)
  }

  trait APIConformant { this: AbstractWorkspace =>
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

    def outputObject(obj: AnyRef,
      owner: AnyRef,
      addNewline: Boolean,
      readable: Boolean,
      destination: OutputDestination): Unit = {
        val caption = owner match {
          case a: Agent => Dump.logoObject(owner)
          case _        => ""
        }
        val message = ((owner match {
          case a: Agent      => ""
          case _ if readable => " "
          case _             => ""
        }) + Dump.logoObject(obj, readable, false))
        val oo = new OutputObject(caption, message, addNewline, false);
        destination match {
          case OutputDestination.File => fileManager.writeOutputObject(oo)
          case _ =>
            sendOutput(oo, destination == OutputDestination.OutputArea)
        }
    }

    // for _thunkdidfinish (says that a thunk finished running without having stop called)
    val completedActivations: WeakHashMap[Activation, Boolean]  = new WeakHashMap()
  }

  trait ExtendableWorkspaceMethods { this: AbstractWorkspace =>
    /**
     * attaches the current model directory to a relative path, if necessary.
     * If filePath is an absolute path, this method simply returns it.
     * If it's a relative path, then the current model directory is prepended
     * to it. If this is a new model, the user's platform-dependent home
     * directory is prepended instead.
     */
    @throws(classOf[java.net.MalformedURLException])
    def attachModelDir(filePath: String): String = {
      FileIO.resolvePath(filePath,
        Option(getModelPath).flatMap(s => Try(Paths.get(s)).toOption))
          .map(_.toString)
          .getOrElse(filePath)
    }
  }

  trait Procedures { this: AbstractWorkspace =>
    private var _procedures: Procedure.ProceduresMap = Procedure.NoProcedures

    override def procedures: Procedure.ProceduresMap = _procedures

    def procedures_=(procs: Procedure.ProceduresMap): Unit = {
      _procedures = procs
    }

    override def setProcedures(procs: Procedure.ProceduresMap): Unit = {
      _procedures = procs
    }

    override def init(): Unit = {
      procedures.values.foreach(_.init(this))
    }
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

  trait Traceable {
    private var tracer: Tracer = null

    def profilingTracer: Tracer = tracer

    def profilingEnabled: Boolean =
      tracer != null

    def setProfilingTracer(t: Tracer): Unit = {
      tracer = t
    }
  }

  trait HubNetManager extends AbstractWorkspace with Components { self: AbstractWorkspaceScala =>
    def hubNetManagerFactory: HubNetManagerFactory

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

  trait Extensions { self: AbstractWorkspaceScala =>
    def getExtensionPathMappings(): Map[String, Path] =
      (for (path <- Option(getModelPath).toSeq.map(mp => Paths.get(mp).getParent) ++ Seq(APIEM.extensionsPath);
            dir  <- path.toFile.listFiles.filter(_.isDirectory);
            jar  <- dir.listFiles.find(_.getName == s"${dir.getName}.jar").toSeq)
       yield (dir.getName, Paths.get(jar.getCanonicalFile.toURI))
      ).toMap
  }

}
