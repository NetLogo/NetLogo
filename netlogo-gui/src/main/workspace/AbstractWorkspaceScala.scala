// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{World, Agent, Observer, AbstractExporter, AgentSet, ArrayAgentSet, OutputObject}
import org.nlogo.api.{ PlotInterface, Dump, CommandLogoThunk, HubNetInterface, LogoException,
  ReporterLogoThunk, JobOwner, ModelType, OutputDestination, SimpleJobOwner, PreviewCommands,
  Workspace => APIWorkspace, WorldDimensions3D, Version }
import org.nlogo.core.{ AgentKind, CompilerException, LiteralParser, Model, View, Widget => CoreWidget, WorldDimensions }
import org.nlogo.nvm.{ Activation, CompilerInterface, FileManager, Instruction, EngineException, Context, Procedure, Tracer }
import org.nlogo.plot.{ PlotExporter, PlotManager }

import java.util.WeakHashMap
import java.net.URL
import java.io.File
import java.nio.file.Paths

import java.io.{IOException,PrintWriter}

import AbstractWorkspaceTraits._

trait HubNetManagerFactory {
  def newInstance(workspace: AbstractWorkspaceScala): HubNetInterface
}

abstract class AbstractWorkspaceScala(val world: World, val hubNetManagerFactory: HubNetManagerFactory)
  extends AbstractWorkspace(world)
  with APIConformant with Benchmarking
  with Checksums with Evaluating
  with ModelTracker
  with BehaviorSpaceInformation
  with Traceable with HubNetManager
  with ExtendableWorkspaceMethods with Exporting
  with Plotting {

  def isHeadless: Boolean

  def compilerTestingMode: Boolean

  var previewCommands: PreviewCommands = PreviewCommands.Default

  // the original instruction here is _tick or a ScalaInstruction (currently still experimental)
  // it is only ever used if we need to generate an EngineException
  // the version of EngineException that takes an instruction is to be *very strongly* preferred.
  // otherwise we don't get accurate runtime error locations
  // we pass in the Instruction so that we dont have to duplicate the exception logic in both locations.
  // JC 5/19/10
  def tick(context: Context, originalInstruction: Instruction) {
    if(world.tickCounter.ticks == -1)
      throw new EngineException(context, originalInstruction,
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

  def clearTicks{
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

  override def getCompilationEnvironment = {
    import java.io.{ File => JFile }
    import java.net.MalformedURLException

    new org.nlogo.core.CompilationEnvironment {
      def getSource(filename: String): String = AbstractWorkspaceScala.this.getSource(filename)
      def profilingEnabled: Boolean = AbstractWorkspaceScala.this.profilingEnabled
      def resolvePath(path: String): String = {
        try {
          val r = Paths.get(attachModelDir(path)).toFile
          try {
            r.getCanonicalPath
          } catch {
            case ex: IOException => r.getPath
          }
        } catch {
          case ex: MalformedURLException =>
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
      if (new File(filePath).isAbsolute)
        filePath
      else {
        val defaultPath = Paths.get(System.getProperty("user.home"))
        val modelParentPath =
          Option(getModelPath).map(s => Paths.get(s)).map(_.getParent)
            .getOrElse(defaultPath)
        val attachedPath = modelParentPath.resolve(filePath)

        attachedPath.toAbsolutePath.toString
      }
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

  trait HubNetManager extends AbstractWorkspace { this: AbstractWorkspaceScala =>
    def hubNetManagerFactory: HubNetManagerFactory

    private var _hubNetManager = Option.empty[HubNetInterface]

    private var _hubNetRunning: Boolean = false

    def hubNetManager = _hubNetManager

    @throws(classOf[InterruptedException])
    abstract override def dispose(): Unit = {
      super.dispose()
      hubNetManager.foreach(_.disconnect())
    }

    def getHubNetManager: Option[HubNetInterface] = {
      if (hubNetManager.isEmpty && hubNetManagerFactory != null) {
        _hubNetManager = Some(hubNetManagerFactory.newInstance(this))
      }
      _hubNetManager
    }

    def hubNetRunning = _hubNetRunning

    def hubNetRunning_=(running: Boolean): Unit = {
      _hubNetRunning = running;
    }
  }
}
