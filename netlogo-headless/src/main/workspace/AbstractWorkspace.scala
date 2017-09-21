// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

// omg. rat's nest. - ST 5/3/13

import
  java.io.{ IOException, PrintWriter }

import
  java.nio.file.{ Paths => NioPaths }

import
  scala.collection.mutable.WeakHashMap

import
  org.nlogo.{ agent, api, core, nvm, plot },
  agent.{ AbstractExporter, Agent, AgentSet, World },
  api.{PlotInterface, CommandLogoThunk, Dump, Exceptions,
    JobOwner, LogoException, MersenneTwisterFast, ModelType, PreviewCommands, ReporterLogoThunk, SimpleJobOwner},
  core.{ CompilationEnvironment, AgentKind, CompilerException, Femto, File, FileMode, LiteralParser},
  nvm.{ Activation, Command, Context, FileManager, ImportHandler,
    Instruction, Job, MutableLong, Procedure, RuntimePrimitiveException, Workspace },
    Procedure.{ NoProcedures, ProceduresMap },
  plot.{ PlotExporter, PlotManager }

import AbstractWorkspaceTraits._

object AbstractWorkspace {

  val DefaultPreviewCommands = "setup repeat 75 [ go ]"

  /**
   * converts a model's filename to an externally displayable model name.
   * The argument may be null, the return value will never be.
   */
  def makeModelNameForDisplay(str: String): String =
    if (str == null)
      "Untitled"
    else {
      var result = str
      var suffixIndex = str.lastIndexOf(".nlogo")
      if (suffixIndex > 0 && suffixIndex == result.size - 6)
        result = result.substring(0, result.size - 6)
      suffixIndex = result.lastIndexOf(".nlogo3d")
      if (suffixIndex > 0 && suffixIndex == result.size - 8)
        result = result.substring(0, str.size - 8)
      result
    }

  def setHeadlessProperty() {
    // force headless mode if it is not set.  This is necessary for the headless workspace to run
    // on most platforms when a display is not available. --CLB
    // note that since our check is for null, so the user can still force the property to false and
    // not be overridden by this - ST 4/21/05
    val p = "java.awt.headless"
    if(System.getProperty(p) == null)
      System.setProperty(p, "true")
  }

}

abstract class AbstractWorkspace(val world: World)
extends api.LogoThunkFactory with LiteralParser
with Workspace with Procedures with Plotting with Exporting with Evaluating with Benchmarking
with Compiling with Profiling with Extensions with BehaviorSpace with Paths with Checksums
with RunCache with Jobs with Warning with OutputArea with Importing
with ExtendableWorkspace with ExtensionCompilationEnvironment with APIConformant {
  val fileManager: FileManager = new DefaultFileManager(this)

  /**
   * previewCommands used by make-preview and model test
   */
  var previewCommands: PreviewCommands = PreviewCommands.Default

  val lastRunTimes = new WeakHashMap[Job, WeakHashMap[Agent, WeakHashMap[Command, MutableLong]]]

  // for _thunkdidfinish (says that a thunk finished running without having stop called)
  val completedActivations = new WeakHashMap[Activation, Boolean]

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

  def resetTicks(context: Context) {
    world.tickCounter.reset()
    setupPlots(context)
    updatePlots(context)
    requestDisplayUpdate(true)
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

  def seedRNGs(seed: Int): Unit = {
    mainRNG.setSeed(seed)
    auxRNG.setSeed(seed)
  }

  override def getCompilationEnvironment = {
    import java.net.MalformedURLException

    new org.nlogo.core.CompilationEnvironment {
      def getSource(filename: String): String = AbstractWorkspace.this.getSource(filename)
      def profilingEnabled: Boolean = AbstractWorkspace.this.profilingEnabled
      def resolvePath(path: String): String = {
        try {
          val r = NioPaths.get(attachModelDir(path)).toFile
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

  trait Compiling { this: AbstractWorkspace =>

    // beware! this should be used everywhere the workspace invokes the compiler, but I doubt that's
    // been achieved. for now, we're only sure that it is used in enough places for the Tortoise
    // docking tests to pass. - ST 10/24/13
    var flags = nvm.CompilerFlags(optimizations = nvm.Optimizations.headlessOptimizations)

    override def readNumberFromString(source: String) =
      compiler.utilities.readNumberFromString(
        source, new ImportHandler(world, getExtensionManager))

    def isReporter(s: String) =
      compiler.utilities.isReporter(s, world.program, procedures, getExtensionManager)
  }

  trait Procedures { this: AbstractWorkspace =>
    var procedures: ProceduresMap =
      NoProcedures
    def init() {
      procedures.values.foreach(_.init(this))
    }
  }


  trait Plotting { this: AbstractWorkspace with Evaluating =>

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

  trait Exporting extends Plotting { this: AbstractWorkspace with Evaluating =>

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

    def exportInterfaceGlobals(writer: java.io.PrintWriter) {
      writer.println(Dump.csv.header("MODEL SETTINGS"))
      val globals = world.program.interfaceGlobals
      writer.println(Dump.csv.variableNameRow(globals))
      writer.println(
        Dump.csv.dataRow(
          globals.map(world.getObserverVariableByName).toArray))
      writer.println()
    }

    def guessExportName(defaultName: String): String = {
      val modelName = getModelFileName
      if (modelName == null)
        defaultName
      else {
        val index = modelName.lastIndexOf(".nlogo")
        val trimmedName =
          if (index == -1)
            modelName
          else
            modelName.take(index)
        trimmedName + " " + defaultName
      }
    }

    @throws(classOf[java.io.IOException])
    def exportBehaviors(filename: String, experimentName: String, includeHeader: Boolean): File = {
      val file = new api.LocalFile(filename)
      file.open(FileMode.Write)
      if (includeHeader) {
        agent.AbstractExporter.exportHeader(
          file.getPrintWriter, "BehaviorSpace", getModelFileName, experimentName)
        file.getPrintWriter.flush()
      }
      file
    }

  }

  trait Evaluating { this: AbstractWorkspace =>
    val evaluator = new Evaluator(this)
    def makeReporterThunk(source: String, jobOwnerName: String): ReporterLogoThunk =
      evaluator.makeReporterThunk(source, world.observer,
                                  new SimpleJobOwner(jobOwnerName, auxRNG))
    def makeCommandThunk(source: String, jobOwnerName: String): CommandLogoThunk =
      makeCommandThunk(source, jobOwnerName, auxRNG)
    def makeCommandThunk(source: String, jobOwnerName: String, rng: MersenneTwisterFast): CommandLogoThunk =
      evaluator.makeCommandThunk(source, world.observer,
        new SimpleJobOwner(jobOwnerName, rng, AgentKind.Observer))
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

  trait Benchmarking { this: AbstractWorkspace =>
    override def benchmark(minTime: Int, maxTime: Int) {
      new Thread("__bench") {
        override def run() {
          Benchmarker.benchmark(
            Benchmarking.this, minTime, maxTime)
        }}.start()
    }
  }

  trait Profiling { this: AbstractWorkspace =>
    private var _tracer: org.nlogo.nvm.Tracer = null
    override def profilingEnabled = _tracer != null
    override def profilingTracer = _tracer
    def setProfilingTracer(tracer: org.nlogo.nvm.Tracer) {
      _tracer = tracer
    }
  }

  trait Extensions { this: AbstractWorkspace =>
    private val _extensionManager: ExtensionManager =
      new ExtensionManager(this, new JarLoader(this));
    override def getExtensionManager =
      _extensionManager
    override def isExtensionName(name: String) =
      _extensionManager.isExtensionName(name)
    @throws(classOf[org.nlogo.api.ExtensionException])
    override def importExtensionData(name: String, data: java.util.List[Array[String]], handler: org.nlogo.api.ImportErrorHandler) {
      _extensionManager.importExtensionData(name, data, handler)
    }
  }

  trait Checksums { this: AbstractWorkspace =>
    override def worldChecksum =
      Checksummer.calculateWorldChecksum(this)
    override def graphicsChecksum =
      Checksummer.calculateGraphicsChecksum(this)
  }

  trait BehaviorSpace { this: api.Workspace =>
    private var _behaviorSpaceRunNumber = 0
    override def behaviorSpaceRunNumber = _behaviorSpaceRunNumber
    override def behaviorSpaceRunNumber(n: Int) {
      _behaviorSpaceRunNumber = n
    }
  }

  trait Paths { this: AbstractWorkspace =>

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

  // this is used to cache the compiled code used by the "run"
  // and "runresult" prims - ST 6/7/07
  trait RunCache { this: AbstractWorkspace =>
    private val runCache = new java.util.WeakHashMap[String, Procedure]
    def clearRunCache() {
      runCache.clear()
    }
    def compileForRun(source: String, context: Context, reporter: Boolean): Procedure = {
      val key =
        source + "@" + context.activation.procedure.args.size +
          "@" + context.agentBit
      Option(runCache.get(key)).getOrElse{
        val proc = evaluator.compileForRun(source, context, reporter)
        runCache.put(key, proc)
        proc
      }
    }
  }

  trait Jobs { this: AbstractWorkspace =>
    val jobManager: nvm.JobManagerInterface =
      Femto.get("org.nlogo.job.JobManager", this, world)
    def halt() {
      jobManager.haltPrimary()
      enablePeriodicRendering()
    }
    /// methods that may be called from the job thread by prims
    def joinForeverButtons(agent: Agent) {
      jobManager.joinForeverButtons(agent)
    }
    def addJobFromJobThread(job: Job) {
      jobManager.addJobFromJobThread(job)
    }
  }

  trait Warning {
    /**
     * Displays a warning to the user, and determine whether to continue.
     * The default (non-GUI) implementation is to print the warning and
     * always continue.
     */
    def warningMessage(message: String): Boolean = {
      System.err.println()
      System.err.println("WARNING: " + message)
      System.err.println()
      // always continue
      true
    }
  }

  trait OutputArea { this: AbstractWorkspace =>

    def clearOutput()

    // called from job thread - ST 10/1/03
    def sendOutput(oo: agent.OutputObject, toOutputArea: Boolean)

    /// importing
    def setOutputAreaContents(text: String) {
      try {
        clearOutput()
        if (text.nonEmpty)
          sendOutput(new agent.OutputObject("", text, false, false), true)
      }
      catch { case e: LogoException => Exceptions.handle(e) }
    }

    def outputObject(obj: AnyRef, owner: AnyRef, addNewline: Boolean, readable: Boolean, destination: api.OutputDestination) {
      val caption = owner match {
        case _: agent.Agent =>
          Dump.logoObject(owner)
        case _ =>
          ""
      }
      val message =
        (if (readable && !(owner.isInstanceOf[agent.Agent]))
          " "
        else
          "") + Dump.logoObject(obj, readable, false)
      val oo = new agent.OutputObject(caption, message, addNewline, false)
      destination match {
        case api.OutputDestination.File =>
          fileManager.writeOutputObject(oo)
        case _ =>
          sendOutput(oo, destination == api.OutputDestination.OutputArea)
      }
    }

  }

  trait Importing { this: nvm.Workspace =>

    abstract class FileImporter(val filename: String) {
      @throws(classOf[java.io.IOException])
      def doImport(reader: File)
    }

    def importerErrorHandler: agent.ImporterJ.ErrorHandler

    @throws(classOf[java.io.IOException])
    def importWorld(filename: String) {
      // we need to clearAll before we import in case
      // extensions are hanging on to old data. ev 4/10/09
      clearAll()
      doImport(
        new BufferedReaderImporter(filename) {
          @throws(classOf[java.io.IOException])
          override def doImport(reader: java.io.BufferedReader) {
              world.asInstanceOf[agent.World].importWorld(
                importerErrorHandler, Importing.this, stringReader, reader)
          }})
    }

    @throws(classOf[java.io.IOException])
    def importWorld(reader: java.io.Reader) {
      // we need to clearAll before we import in case
      // extensions are hanging on to old data. ev 4/10/09
      clearAll()
      world.asInstanceOf[agent.World].importWorld(
        importerErrorHandler, Importing.this, stringReader,
        new java.io.BufferedReader(reader))
    }

    private def stringReader: agent.ImporterJ.StringReader =
      new agent.ImporterJ.StringReader {
        @throws(classOf[agent.ImporterJ.StringReaderException])
        def readFromString(s: String): AnyRef =
          try compiler.utilities.readFromString(s, new ImportHandler(world, getExtensionManager))
          catch { case ex: CompilerException =>
              throw new agent.ImporterJ.StringReaderException(ex.getMessage)
          }
      }

    @throws(classOf[java.io.IOException])
    def importDrawing(filename: String) {
      doImport(
        new FileImporter(filename) {
          @throws(classOf[java.io.IOException])
          override def doImport(file: File) {
            importDrawing(file)
          }
        })
    }

    @throws(classOf[java.io.IOException])
    def importDrawing(file: File)

    @throws(classOf[java.io.IOException])
    def doImport(importer: BufferedReaderImporter) {
      val file = new api.LocalFile(importer.filename)
      try {
        file.open(FileMode.Read)
        importer.doImport(file.reader)
      }
      finally
        try file.close(false)
        catch { case ex2: java.io.IOException =>
            org.nlogo.api.Exceptions.ignore(ex2)
        }
    }

    @throws(classOf[java.io.IOException])
    def doImport(importer: FileImporter) {
      importer.doImport(
        new api.LocalFile(importer.filename))
    }

  }

  trait ExtensionCompilationEnvironment { this: Paths with Profiling =>
    import java.io.{ File => JFile }
    import java.net.MalformedURLException
    import java.net.URL

    def attachModelDir(filePath: String): String = {
      if (new JFile(filePath).isAbsolute())
        filePath
      else {
        val path =
          Option(getModelPath).getOrElse(
            System.getProperty("user.home")
            + JFile.separatorChar + "dummy.txt")
        val urlForm = new URL(new JFile(path).toURI.toURL, filePath);
        new JFile(urlForm.getFile()).getAbsolutePath();
      }
    }

    def getSource(filename: String): String = {
      throw new UnsupportedOperationException("Source not available from headless workspace")
    }

    val compilationEnvironment = new CompilationEnvironment {

      def getSource(filename: String): String =
        ExtensionCompilationEnvironment.this.getSource(filename)

      def profilingEnabled: Boolean =
        ExtensionCompilationEnvironment.this.profilingEnabled

      def resolvePath(path: String): String = {
        try {
          val r = new JFile(attachModelDir(path))
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

  trait APIConformant {
    var _behaviorSpaceExperimentName: String = ""
    def behaviorSpaceExperimentName(name: String): Unit = {
      _behaviorSpaceExperimentName = name
    }
    def behaviorSpaceExperimentName: String = _behaviorSpaceExperimentName
    def getComponent[A <: AnyRef](componentClass: Class[A]): Option[A] = None
    def inspectAgent(agent: org.nlogo.api.Agent,radius: Double): Unit = { }
    def stopInspectingAgent(agent: org.nlogo.agent.Agent): Unit = { }
    def stopInspectingDeadAgents(): Unit = { }
  }
}
