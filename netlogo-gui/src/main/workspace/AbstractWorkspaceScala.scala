// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{World, Agent, Observer, AbstractExporter, AgentSet, ArrayAgentSet, OutputObject}
import org.nlogo.api.{ PlotInterface, Dump, CommandLogoThunk, LogoException, ReporterLogoThunk, JobOwner, OutputDestination, SimpleJobOwner, PreviewCommands, Workspace => APIWorkspace }
import org.nlogo.core.{ AgentKind, CompilerException }
import org.nlogo.nvm.{ Activation, Instruction, EngineException, Context, Procedure, Tracer }
import org.nlogo.plot.{ PlotExporter, PlotManager }
import org.nlogo.workspace.AbstractWorkspace.HubNetManagerFactory

import java.util.WeakHashMap
import java.net.URL
import java.io.File
import java.nio.file.Paths

import java.io.{IOException,PrintWriter}

import AbstractWorkspaceTraits._

abstract class AbstractWorkspaceScala(val world: World, hubNetManagerFactory: HubNetManagerFactory)
  extends AbstractWorkspace(world, hubNetManagerFactory)
  with APIConformant with Benchmarking
  with Checksums with Evaluating
  with ModelDir with BehaviorSpaceInformation
  with Traceable
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
}

object AbstractWorkspaceTraits {

  trait Plotting { this: AbstractWorkspace =>

    val plotManager = new PlotManager(this)

    // methods used when importing plots
    def currentPlot(plot: String) {
      plotManager.currentPlot = Some(plotManager.getPlot(plot))
    }

    def getPlot(plot: String): PlotInterface = plotManager.getPlot(plot)

    // The PlotManager has already-compiled thunks that it runs to setup and update
    // plots.  But those thunks need a Context to run in, which isn't known until
    // runtime.  So once we know the Context, we store it in a bit of mutable state
    // in Evaluator. - ST 3/2/10

    def updatePlots(c: Context) {
      evaluator.withContext(c){ plotManager.updatePlots() }
    }

    def setupPlots(c: Context) {
      evaluator.withContext(c){ plotManager.setupPlots() }
    }

  }

  trait Exporting extends Plotting { this: AbstractWorkspace with ModelDir =>

    def exportDrawingToCSV(writer:PrintWriter)
    def exportOutputAreaToCSV(writer:PrintWriter)

    @throws(classOf[IOException])
    def exportWorld(filename: String) {
      new AbstractExporter(filename) {
        def export(writer:PrintWriter){
          world.exportWorld(writer,true)
          exportDrawingToCSV(writer)
          exportOutputAreaToCSV(writer)
          exportPlotsToCSV(writer)
          extensionManager.exportWorld(writer)
        } }.export("world",modelFileName,"")
    }

    def exportWorld(writer:PrintWriter){
      world.exportWorld(writer,true)
      exportDrawingToCSV(writer)
      exportOutputAreaToCSV(writer)
      exportPlotsToCSV(writer)
      extensionManager.exportWorld(writer)
    }

    def exportPlotsToCSV(writer: PrintWriter) = {
      writer.println(Dump.csv.encode("PLOTS"))
      writer.println(
        Dump.csv.encode(
          plotManager.currentPlot.map(_.name).getOrElse("")))
      plotManager.getPlotNames.foreach { name =>
        new PlotExporter(plotManager.getPlot(name),Dump.csv).export(writer)
        writer.println()
      }
    }

    @throws(classOf[IOException])
    def exportPlot(plotName: String,filename: String) {
      new AbstractExporter(filename) {
        override def export(writer: PrintWriter) {
          exportInterfaceGlobals(writer)
          new PlotExporter(plotManager.getPlot(plotName),Dump.csv).export(writer)
        }
      }.export("plot",modelFileName,"")
    }

    @throws(classOf[IOException])
    def exportAllPlots(filename: String) {
      new AbstractExporter(filename) {
        override def export(writer: PrintWriter) {
          exportInterfaceGlobals(writer)

          plotManager.getPlotNames.foreach { name =>
            new PlotExporter(plotManager.getPlot(name),Dump.csv).export(writer)
            writer.println()
          }
        }
      }.export("plots",modelFileName,"")
    }
  }

  trait Evaluating { this: AbstractWorkspace =>
    var lastLogoException: LogoException = null

    override def clearLastLogoException() { lastLogoException = null }

    @throws(classOf[CompilerException])
    def makeReporterThunk(source: String, jobOwnerName: String): ReporterLogoThunk =
      evaluator.makeReporterThunk(source, world.observer,
                                  new SimpleJobOwner(jobOwnerName, auxRNG, AgentKind.Observer))
    @throws(classOf[CompilerException])
    def makeCommandThunk(source: String, jobOwnerName: String): CommandLogoThunk =
      evaluator.makeCommandThunk(source, world.observer,
                                 new SimpleJobOwner(jobOwnerName, auxRNG, AgentKind.Observer))
    @throws(classOf[CompilerException])
    def evaluateCommands(owner: JobOwner, source: String) {
      evaluator.evaluateCommands(owner, source)
    }
    @throws(classOf[CompilerException])
    def evaluateCommands(owner: JobOwner, source: String, waitForCompletion: Boolean) {
      evaluator.evaluateCommands(owner, source, world.observers, waitForCompletion)
    }
    @throws(classOf[CompilerException])
    def evaluateCommands(owner: JobOwner, source: String, agent: Agent,
                         waitForCompletion: Boolean) {
      val agents = new ArrayAgentSet(agent.kind, 1, false)
      agents.add(agent)
      evaluator.evaluateCommands(owner, source, agents, waitForCompletion)
    }
    @throws(classOf[CompilerException])
    def evaluateCommands(owner: JobOwner, source: String, agents: AgentSet,
                         waitForCompletion: Boolean) {
      evaluator.evaluateCommands(owner, source, agents, waitForCompletion)
    }
    @throws(classOf[CompilerException])
    def evaluateReporter(owner: JobOwner, source: String) =
      evaluator.evaluateReporter(owner, source, world.observers)
    @throws(classOf[CompilerException])
    def evaluateReporter(owner: JobOwner, source: String, agent: Agent) = {
      val agents = new ArrayAgentSet(agent.kind, 1, false)
      agents.add(agent)
      evaluator.evaluateReporter(owner, source, agents)
    }
    @throws(classOf[CompilerException])
    def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet) =
      evaluator.evaluateReporter(owner, source, agents)
    @throws(classOf[CompilerException])
    def compileCommands(source: String): Procedure =
      compileCommands(source, AgentKind.Observer)
    @throws(classOf[CompilerException])
    def compileCommands(source: String, agentClass: AgentKind): Procedure =
      evaluator.compileCommands(source, agentClass)
    @throws(classOf[CompilerException])
    def compileReporter(source: String): Procedure =
      evaluator.compileReporter(source)
    def runCompiledCommands(owner: JobOwner, procedure: Procedure): Boolean =
      evaluator.runCompiledCommands(owner, procedure)
    def runCompiledReporter(owner: JobOwner, procedure: Procedure): AnyRef =
      evaluator.runCompiledReporter(owner, procedure)

    @throws(classOf[CompilerException])
    def readFromString(string: String): AnyRef =
      evaluator.readFromString(string)

    val defaultOwner =
      new SimpleJobOwner(getClass.getSimpleName, world.mainRNG, AgentKind.Observer)
    /**
     * Runs NetLogo commands and waits for them to complete.
     *
     * @param source The command or commands to run
     * @throws org.nlogo.core.CompilerException
     *                       if the code fails to compile
     * @throws org.nlogo.api.LogoException if the code fails to run
     */
    @throws(classOf[CompilerException])
    @throws(classOf[LogoException])
    def command(source: String): Unit = {
      evaluator.evaluateCommands(defaultOwner, source, world.observers, true)
      if (lastLogoException != null) {
        val ex = lastLogoException
        lastLogoException = null
        throw ex
      }
    }

    /**
     * Runs a NetLogo reporter.
     *
     * @param source The reporter to run
     * @return the result reported; may be of type java.lang.Integer, java.lang.Double,
     *         java.lang.Boolean, java.lang.String, {@link org.nlogo.core.LogoList},
     *         {@link org.nlogo.api.Agent}, AgentSet, or Nobody
     * @throws org.nlogo.core.CompilerException
     *                       if the code fails to compile
     * @throws org.nlogo.api.LogoException if the code fails to run
     */
    @throws(classOf[CompilerException])
    @throws(classOf[LogoException])
    def report(source: String): AnyRef = {
      val result = evaluator.evaluateReporter(defaultOwner, source, world.observers)
      if (lastLogoException != null) {
        val ex = lastLogoException
        lastLogoException = null
        throw ex
      }
      result
    }
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

  trait Checksums { this: AbstractWorkspace with APIWorkspace =>
    override def worldChecksum =
      Checksummer.calculateWorldChecksum(this)
    override def graphicsChecksum =
      Checksummer.calculateGraphicsChecksum(this)
  }

  trait ModelDir { this: AbstractWorkspace =>
    /**
     * path to the directory from which the current model was loaded. NetLogo
     * uses this as the default path for file I/O, when reloading models,
     * locating HubNet clients, etc. This is null if this is a new (unsaved)
     * model.
     */
    private[workspace] var modelDir: String = null

    /**
     * name of the currently loaded model. Will be null if this is a new
     * (unsaved) model. To get a version for display to the user, see
     * modelNameForDisplay(). This is NOT a full path name, however, it does
     * end in ".nlogo".
     */
    var _modelFileName: String = null

    /**
     * returns the full path to the directory from which the current model was
     * loaded. May be null if, for example, this is a new model.
     */
    def getModelDir: String = modelDir

    /**
     * returns the name of the file from which the current model was loaded.
     * May be null if, for example, this is a new model.
     */
    def getModelFileName: String = _modelFileName

    def modelFileName: String = _modelFileName

    /**
     * returns the full pathname of the currently loaded model, if any. This
     * may return null in some cases, for instance if this is a new model.
     */
    def getModelPath: String = {
      if (modelDir == null || modelFileName == null)
        null
      else
        modelDir + java.io.File.separatorChar + modelFileName
    }

    def setModelPath(modelPath: String): Unit = {
      if (modelPath == null) {
        _modelFileName = null
        modelDir = null
      } else {
        val file = new java.io.File(modelPath).getAbsoluteFile
        _modelFileName = file.getName
        modelDir = file.getParent
        if (modelDir == "") {
          modelDir = null
        }
        if (modelDir != null) {
          fileManager.setPrefix(modelDir)
        }
      }
    }

    def modelNameForDisplay: String =
      AbstractWorkspace.makeModelNameForDisplay(modelFileName)
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
}
