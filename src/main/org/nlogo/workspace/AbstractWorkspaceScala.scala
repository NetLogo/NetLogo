// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{ World, Agent, Observer, AbstractExporter, AgentSet }
import org.nlogo.api.{ AgentKind, PlotInterface, Dump, CommandLogoThunk, ReporterLogoThunk,
                       CompilerException, LogoException, JobOwner, SimpleJobOwner, Token, ModelType}
import org.nlogo.nvm.{ CompilerInterface, FileManager, Instruction, EngineException, Context,
                       Procedure, Job, Command, MutableLong, Workspace, Activation }
import org.nlogo.plot.{ PlotExporter, PlotManager }
import java.io.{ IOException, PrintWriter }
import java.util.WeakHashMap

import AbstractWorkspaceTraits._

object AbstractWorkspaceScala {
  val DefaultPreviewCommands = "setup repeat 75 [ go ]"
}

abstract class AbstractWorkspaceScala(val world: World)
extends AbstractWorkspace
with Workspace with Procedures with Plotting with Exporting with Evaluating with Benchmarking
with Compiling with Profiling with Extensions with BehaviorSpace with Paths {

  val fileManager: FileManager = new DefaultFileManager(this)

  /**
   * previewCommands used by make-preview and model test
   */
  var previewCommands = AbstractWorkspaceScala.DefaultPreviewCommands

  val lastRunTimes = new WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]

  // for _thunkdidfinish (says that a thunk finished running without having stop called)
  val completedActivations = new WeakHashMap[Activation, java.lang.Boolean]

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
    requestDisplayUpdate(context, true)
  }

  def resetTicks(context: Context) {
    world.tickCounter.reset()
    setupPlots(context)
    updatePlots(context)
    requestDisplayUpdate(context, true)
  }

  def clearTicks() {
    world.tickCounter.clear()
  }

  def clearAll() {
    world.clearAll()
    clearOutput()
    clearDrawing()
    plotManager.clearAll()
    getExtensionManager.clearAll()
  }

  /**
   * Shuts down the background thread associated with this workspace,
   * allowing resources to be freed.
   */
  @throws(classOf[InterruptedException])
  def dispose() {
    jobManager.die()
    plotManager.forgetAll()
    getExtensionManager.reset()
  }

  override def mainRNG = world.mainRNG
  override def auxRNG = world.auxRNG
  override def lastLogoException: LogoException = null
  override def clearLastLogoException() { }

}

object AbstractWorkspaceTraits {

  trait Compiling { this: AbstractWorkspaceScala =>

    override def readNumberFromString(source: String) =
      compiler.readNumberFromString(
        source, world, getExtensionManager)

    override def checkReporterSyntax(source: String) =
      compiler.checkReporterSyntax(
        source, world.program, procedures, getExtensionManager, false)

    def checkCommandSyntax(source: String) =
      compiler.checkCommandSyntax(
        source, world.program, procedures, getExtensionManager, false)

    def isConstant(s: String) =
      try {
        compiler.readFromString(s)
        true
      }
      catch { case _: CompilerException => false }

    override def isValidIdentifier(s: String) =
      compiler.isValidIdentifier(s)

    override def isReporter(s: String) =
      compiler.isReporter(s, world.program, procedures, getExtensionManager)

    override def tokenizeForColorization(s: String): Seq[Token] =
      compiler.tokenizeForColorization(
        s, getExtensionManager)

    override def getTokenAtPosition(s: String, pos: Int): Token =
      compiler.getTokenAtPosition(s, pos)

    override def findProcedurePositions(source: String) =
      compiler.findProcedurePositions(source)

  }

  trait Procedures { this: AbstractWorkspaceScala =>
    var procedures: CompilerInterface.ProceduresMap =
      CompilerInterface.NoProcedures
    def init() {
      procedures.values.foreach(_.init(this))
    }
  }


  trait Plotting { this: AbstractWorkspace =>

    val plotManager = new PlotManager(this)

    // methods used when importing plots
    def currentPlot(plot: String) {
      plotManager.currentPlot = plotManager.getPlot(plot)
    }

    def getPlot(plot: String): PlotInterface = plotManager.getPlot(plot).orNull

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

  trait Exporting extends Plotting { this: AbstractWorkspaceScala =>

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
          getExtensionManager.exportWorld(writer)
        } }.export("world",getModelFileName,"")
    }

    def exportWorld(writer:PrintWriter){
      world.exportWorld(writer,true)
      exportDrawingToCSV(writer)
      exportOutputAreaToCSV(writer)
      exportPlotsToCSV(writer)
      getExtensionManager.exportWorld(writer)
    }

    def exportPlotsToCSV(writer: PrintWriter) {
      writer.println(Dump.csv.encode("PLOTS"))
      writer.println(
        Dump.csv.encode(
          plotManager.currentPlot.map(_.name).getOrElse("")))
      plotManager.getPlotNames.foreach { name =>
        new PlotExporter(plotManager.getPlot(name).orNull, Dump.csv).export(writer)
        writer.println()
      }
    }

    @throws(classOf[IOException])
    def exportPlot(plotName: String,filename: String) {
      new AbstractExporter(filename) {
        override def export(writer: PrintWriter) {
          exportInterfaceGlobals(writer)
          new PlotExporter(plotManager.getPlot(plotName).orNull, Dump.csv).export(writer)
        }
      }.export("plot",getModelFileName,"")
    }

    @throws(classOf[IOException])
    def exportAllPlots(filename: String) {
      new AbstractExporter(filename) {
        override def export(writer: PrintWriter) {
          exportInterfaceGlobals(writer)

          plotManager.getPlotNames.foreach { name =>
            new PlotExporter(plotManager.getPlot(name).orNull, Dump.csv).export(writer)
            writer.println()
          }
        }
      }.export("plots",getModelFileName,"")
    }
  }

  trait Evaluating { this: AbstractWorkspaceScala =>
    def makeReporterThunk(source: String, jobOwnerName: String): ReporterLogoThunk =
      evaluator.makeReporterThunk(source, world.observer,
                                  new SimpleJobOwner(jobOwnerName, auxRNG))
    def makeCommandThunk(source: String, jobOwnerName: String): CommandLogoThunk =
      evaluator.makeCommandThunk(source, world.observer,
                                 new SimpleJobOwner(jobOwnerName, auxRNG))
    def evaluateCommands(owner: JobOwner, source: String) {
      evaluator.evaluateCommands(owner, source)
    }
    def evaluateCommands(owner: JobOwner, source: String, waitForCompletion: Boolean) {
      evaluator.evaluateCommands(owner, source, world.observers, waitForCompletion)
    }
    def evaluateCommands(owner: JobOwner, source: String, agent: Agent,
                         waitForCompletion: Boolean) {
      evaluator.evaluateCommands(owner, source,
        AgentSet.fromAgent(agent), waitForCompletion)
    }
    def evaluateCommands(owner: JobOwner, source: String, agents: AgentSet,
                         waitForCompletion: Boolean) {
      evaluator.evaluateCommands(owner, source, agents, waitForCompletion)
    }
    def evaluateReporter(owner: JobOwner, source: String) =
      evaluator.evaluateReporter(owner, source, world.observers)
    def evaluateReporter(owner: JobOwner, source: String, agent: Agent) = {
      evaluator.evaluateReporter(owner, source,
        AgentSet.fromAgent(agent))
    }
    def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet) =
      evaluator.evaluateReporter(owner, source, agents)
    def compileCommands(source: String): Procedure =
      compileCommands(source, AgentKind.Observer)
    def compileCommands(source: String, kind: AgentKind): Procedure =
      evaluator.compileCommands(source, kind)
    def compileReporter(source: String): Procedure =
      evaluator.compileReporter(source)
    def runCompiledCommands(owner: JobOwner, procedure: Procedure): Boolean =
      evaluator.runCompiledCommands(owner, procedure)
    def runCompiledReporter(owner: JobOwner, procedure: Procedure): AnyRef =
      evaluator.runCompiledReporter(owner, procedure)
    def readFromString(string: String): AnyRef =
      evaluator.readFromString(string)
  }

  trait Benchmarking { this: AbstractWorkspaceScala =>
    override def benchmark(minTime: Int, maxTime: Int) {
      new Thread("__bench") {
        override def run() {
          Benchmarker.benchmark(
            Benchmarking.this, minTime, maxTime)
        }}.start()
    }
  }

  trait Profiling { this: AbstractWorkspaceScala =>
    private var _tracer: org.nlogo.nvm.Tracer = null
    override def profilingEnabled = _tracer != null
    override def profilingTracer = _tracer
    def setProfilingTracer(tracer: org.nlogo.nvm.Tracer) {
      _tracer = tracer
    }
  }

  trait Extensions { this: AbstractWorkspaceScala =>
    private val _extensionManager: ExtensionManager =
      new ExtensionManager(this)
    override def getExtensionManager =
      _extensionManager
    override def isExtensionName(name: String) =
      _extensionManager.isExtensionName(name);
    @throws(classOf[org.nlogo.api.ExtensionException])
    override def importExtensionData(name: String, data: java.util.List[Array[String]], handler: org.nlogo.api.ImportErrorHandler) {
      _extensionManager.importExtensionData(name, data, handler)
    }
  }

  trait BehaviorSpace { this: org.nlogo.api.Workspace =>
    private var _behaviorSpaceRunNumber = 0
    override def behaviorSpaceRunNumber = _behaviorSpaceRunNumber
    override def behaviorSpaceRunNumber(n: Int) {
      _behaviorSpaceRunNumber = n
    }
  }

  trait Paths { this: AbstractWorkspaceScala =>

    /**
     * name of the currently loaded model. Will be null if this is a new
     * (unsaved) model. To get a version for display to the user, see
     * modelNameForDisplay(). This is NOT a full path name, however, it does
     * end in ".nlogo".
     */
    private var _modelFileName: String = null

    /**
     * path to the directory from which the current model was loaded. NetLogo
     * uses this as the default path for file I/O, when reloading models,
     * locating HubNet clients, etc. This is null if this is a new (unsaved)
     * model.
     */
    private var _modelDir: String = null

    /**
     * type of the currently loaded model. Certain aspects of NetLogo's
     * behavior depend on this, i.e. whether to force a save-as and so on.
     */
    private var _modelType: ModelType = ModelType.New

    def setModelType(modelType: ModelType) {
      _modelType = modelType
    }

    /**
     * returns the full pathname of the currently loaded model, if any. This
     * may return null in some cases, for instance if this is a new model.
     */
    def getModelPath: String =
      if (_modelDir == null || _modelFileName == null)
        null
      else
        _modelDir + java.io.File.separatorChar + _modelFileName

    /**
     * returns the name of the file from which the current model was loaded.
     * May be null if, for example, this is a new model.
     */
    def getModelFileName = _modelFileName

    /**
     * returns the full path to the directory from which the current model was
     * loaded. May be null if, for example, this is a new model.
     */
    def getModelDir = _modelDir

    def getModelType = _modelType

    /**
     * whether the user needs to enter a new filename to save this model.
     * We need to do a "save as" if the model is new, from the
     * models library, or converted.
     * <p/>
     * Basically, only normal models can get silently saved.
     */
    def forceSaveAs =
      _modelType == ModelType.New || _modelType == ModelType.Library

    def modelNameForDisplay =
      AbstractWorkspace.makeModelNameForDisplay(_modelFileName)

    def setModelPath(modelPath: String) {
      if (modelPath == null) {
        _modelFileName = null
        _modelDir = null
      }
      else {
        val file = new java.io.File(modelPath).getAbsoluteFile
        _modelFileName = file.getName
        _modelDir = file.getParent
        if (_modelDir == "")
          _modelDir = null
        if (_modelDir != null)
          fileManager.setPrefix(_modelDir)
      }
    }

  }

}
