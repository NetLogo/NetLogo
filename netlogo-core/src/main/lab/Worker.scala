// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import java.util.Locale
import java.util.concurrent.{ Callable, Executors, TimeUnit }

import org.nlogo.core.{ AgentKind, I18N, WorldDimensions }
import org.nlogo.api.{ Dump, ExportPlotWarningAction, LabPostProcessorInputFormat, LabProtocol, LogoException,
                       MersenneTwisterFast, PartialData, SimpleJobOwner, WorldDimensionException }
import org.nlogo.nvm.{ Command, LabInterface, Workspace }

import LabInterface.ProgressListener

class Worker(val protocol: LabProtocol)
  extends LabInterface.Worker
{
  val listeners = new collection.mutable.ListBuffer[ProgressListener]
  def addListener(listener: ProgressListener): Unit = {
    listeners += listener
  }
  def addSpreadsheetWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter,
                           partialData: PartialData = new PartialData): Unit = {
    addListener(new SpreadsheetExporter(modelFileName, initialDims, protocol, w, partialData))
  }
  def addTableWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter): Unit = {
    addListener(new TableExporter(modelFileName, initialDims, protocol, w))
  }
  def addStatsWriter(modelFileName: String, initialDims: WorldDimensions,
                      w: java.io.PrintWriter, in: LabPostProcessorInputFormat.Format): Unit = {
    addListener(new StatsExporter(modelFileName, initialDims, protocol, w, in))
  }
  def addListsWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter,
                     in: LabPostProcessorInputFormat.Format): Unit = {
    addListener(new ListsExporter(modelFileName, initialDims, protocol, w, in))
  }
  var runners: Seq[Runner] = null
  // we only want to compile stuff once per workspace, so use this
  // (should use a Scala collection not a Java one, but oh well, too lazy today - ST 8/13/09)
  val proceduresMap = new java.util.WeakHashMap[Workspace, Procedures]
  def run(initialWorkspace: Workspace, fn: () => Workspace, threads: Int): Unit = {
    val globals = initialWorkspace.world.program.interfaceGlobals
    val initialState = collection.mutable.Map[String, AnyRef]()
    for (g <- globals) {
      initialState(g) = initialWorkspace.world.getObserverVariableByName(g)
    }
    val executor = Executors.newFixedThreadPool(threads)
    try {
      listeners.foreach(_.experimentStarted())
      initialWorkspace.runCompiledCommands(new SimpleJobOwner("BehaviorSpace",
                                                              initialWorkspace.world.mainRNG,
                                                              AgentKind.Observer),
                                           new Procedures(initialWorkspace).preExperimentProcedure)
      if (initialWorkspace.lastLogoException != null) {
        val ex = initialWorkspace.lastLogoException
        initialWorkspace.clearLastLogoException()
        listeners.foreach(_.runtimeError(initialWorkspace, 0, ex))
      }
      runners =
        (for((settings, runNumber) <- (protocol.refElements zip LazyList.from(1).iterator).drop(protocol.runsCompleted))
         yield new Runner(runNumber, settings, fn)).toSeq
      val futures = {
        import scala.jdk.CollectionConverters.{ ListHasAsScala, SeqHasAsJava }
        // The explicit use of JavaConversions here with a type parameter, instead of just plain
        // "asJava", is required to compile against Java 5 - ST 8/17/11
        executor.invokeAll(runners.asJava).asScala
      }
      initialWorkspace.runCompiledCommands(new SimpleJobOwner("BehaviorSpace",
                                                              initialWorkspace.world.mainRNG,
                                                              AgentKind.Observer),
                                           new Procedures(initialWorkspace).postExperimentProcedure)
      if (initialWorkspace.lastLogoException != null) {
        val ex = initialWorkspace.lastLogoException
        initialWorkspace.clearLastLogoException()
        listeners.foreach(_.runtimeError(initialWorkspace, protocol.countRuns, ex))
      }
      executor.shutdown()
      executor.awaitTermination(java.lang.Integer.MAX_VALUE, TimeUnit.SECONDS)
      listeners.foreach(_.experimentCompleted())
      // this will cause the first ExecutionException we got to be thrown - ST 3/10/09
      futures.foreach(_.get)
    }
    catch {
      case _: InterruptedException =>
        runners.foreach(_.aborted = true)
        listeners.foreach(_.experimentAborted())
    } finally {
      // "Invocation has no additional effect if already shut down." - API doc.
      // We need to be completely sure the executor is shut down otherwise we leak
      // threads (ticket #1185). - ST 2/11/11
      executor.shutdown()
      runners = null
      for (g <- globals) {
        initialWorkspace.world.setObserverVariableByName(g, initialState(g))
      }
    }
  }
  // result discarded -- we just want to see if compilation succeeds.
  // used in TestCompileAll, also used before the start of the
  // experiment in the GUI so if something doesn't compile we can fail early.
  def compile(w: Workspace): Unit = { new Procedures(w) }
  override def abort(): Unit = {
    if (runners != null) runners.foreach(_.aborted = true)
  }
  class Procedures(workspace: Workspace) {
    val preExperimentProcedure = workspace.compileCommands(protocol.preExperimentCommands)
    val setupProcedure = workspace.compileCommands(protocol.setupCommands)
    val goProcedure = workspace.compileCommands(protocol.goCommands
                                                + "\n" // protect against comments
                                                + "__experimentstepend")
    val postRunProcedure = workspace.compileCommands(protocol.postRunCommands)
    val postExperimentProcedure = workspace.compileCommands(protocol.postExperimentCommands)
    val exitProcedure = protocol.exitCondition.split("\n").dropWhile(_.trim.startsWith(";")).dropWhile(_.trim.isEmpty)
                          .headOption.map(workspace.compileReporter)
    val metricProcedures = protocol.metrics.map(workspace.compileReporter(_))
    val runMetricsConditionProcedure = {
      if (protocol.runMetricsCondition.isEmpty) None
      else Some(workspace.compileReporter(protocol.runMetricsCondition))
    }
  }
  class Runner(runNumber: Int, settings: List[(String, AnyRef)], fn: ()=>Workspace)
    extends Callable[Unit]
  {
    class FailedException(message: String) extends LogoException(message)
    private def owner(rng: MersenneTwisterFast) =
      new SimpleJobOwner("BehaviorSpace", rng, AgentKind.Observer)
    @volatile var aborted = false
    // each Runner is on its own thread, but all the Runners share a ProgressListener,
    // so we need to synchronize
    def eachListener(fn: (ProgressListener)=>Unit): Unit = {
      listeners.synchronized { listeners.foreach(fn) }
    }
    def call(): Unit = {
      // not clear why this check would be necessary, but perhaps it will
      // keep bug #1203 from happening - ST 2/16/11
      if (!aborted) {
        val workspace = fn.apply()
        if (workspace != null) {
          try callHelper(workspace)
          catch { case t: Throwable =>
            if (!aborted) eachListener(_.runtimeError(workspace, runNumber, t)) }
        }
      }
    }
    def callHelper(ws: Workspace): Unit = {
      val procedures =
        if (proceduresMap.containsKey(ws))
          proceduresMap.get(ws)
        else {
          val newProcedures = new Procedures(ws)
          proceduresMap.put(ws, newProcedures)
          newProcedures
        }
      var lastMeasuredStep = -1
      import procedures._
      def setVariables(settings: List[(String, AnyRef)]): Unit = {
        val world = ws.world
        var d = world.getDimensions
        for((name, value) <- settings) {
          if (world.isDimensionVariable(name)) {
            val v = value.asInstanceOf[java.lang.Double].intValue
            try { d = world.setDimensionVariable(name, v, d) }
            catch {
              case e: WorldDimensionException =>
                throw new FailedException("You cannot set " + name + " to " + v)
            }
          }
          else if (name.equalsIgnoreCase("RANDOM-SEED"))
            ws.world.mainRNG.setSeed(value.asInstanceOf[java.lang.Double].longValue)
        }
        if (!world.equalDimensions(d)) ws.setDimensions(d)
        for((name, value) <- settings)
          if (!world.isDimensionVariable(name) && !name.equalsIgnoreCase("RANDOM-SEED"))
            ws.world.synchronized {
              if (ws.world.observerOwnsIndexOf(name.toUpperCase(Locale.ENGLISH)) == -1)
                throw new FailedException(
                  "Global variable does not exist:\n" + name)
              ws.world.setObserverVariableByName(name, value)
            }
      }
      def exitConditionTrue =
        exitProcedure match {
          case None => false
          case Some(procedure) =>
            ws.runCompiledReporter(owner(ws.world.mainRNG.clone), procedure) match {
              case t: Throwable =>
                throw t
              case b: java.lang.Boolean =>
                b.booleanValue
              case null =>
                throw new FailedException(
                  "Stopping condition failed to report a result:\n" +
                  protocol.exitCondition)
              case result: AnyRef =>
                throw new FailedException(
                  "Stopping condition should report true or false, but instead reported the " +
                  Dump.typeName(result) + " " + Dump.logoObject(result))
            }
        }
      // Runs runMetricsCondition if it exists, and returns false if it doesn't
      def shouldTakeMeasurements(): Boolean = {
        runMetricsConditionProcedure match {
          case None => false
          case Some(procedure) =>
            ws.runCompiledReporter(owner(ws.world.mainRNG.clone), procedure) match {
              case t: Throwable => {
                throw new FailedException("Metrics condition reporter encountered an error: " + t)
              }
              case b: java.lang.Boolean =>
                b.booleanValue
              case null =>
                throw new FailedException(
                  "Metrics condition reporter failed to report a result:\n" +
                  protocol.runMetricsCondition)
              case result: AnyRef =>
                throw new FailedException(
                  "Metrics condition should report true or false, but instead reported the " +
                  Dump.typeName(result) + " " + Dump.logoObject(result))
            }
          }
        }

      def takeMeasurements(): List[AnyRef] = {
        metricProcedures.map{proc =>
          val result = ws.runCompiledReporter(owner(ws.world.mainRNG.clone), proc)
          if (result == null)
            throw new FailedException(
              "Reporter for measuring runs failed to report a result:\n" + result)
          result }
        }

      def checkForPlotExportCommand(code: Array[Command]): Boolean = {
        var exportCommandFound = false
        for (c <- code) {
          c.getClass.getSimpleName match {
            case "_exportplots" | "_exportplot" | "_exportworld" | "_exportinterface" => {
              exportCommandFound = true
            }
            case _ =>
          }
        }
        exportCommandFound
      }

      def checkForPlotExport(): Unit = {
        if (!ws.shouldUpdatePlots() &&
            (checkForPlotExportCommand(preExperimentProcedure.code) ||
             checkForPlotExportCommand(postRunProcedure.code) ||
             checkForPlotExportCommand(goProcedure.code) ||
             checkForPlotExportCommand(setupProcedure.code) ||
             checkForPlotExportCommand(postExperimentProcedure.code))) {
          import ExportPlotWarningAction._
          ws.setTriedToExportPlot(true)
          ws.exportPlotWarningAction() match {
            case Output => {
              ws.setExportPlotWarningAction(ExportPlotWarningAction.Ignore)
              println(I18N.shared.get("tools.behaviorSpace.runoptions.updateplotsandmonitors.error"))
            }
            case _ =>
          }
        }
      }
      def checkForRuntimeError(): Unit = {
        if (ws.lastLogoException != null) {
          val ex = ws.lastLogoException
          ws.clearLastLogoException()
          if (!aborted) {
            eachListener(_.runtimeError(ws, runNumber, ex))

            if (protocol.errorBehavior == LabProtocol.AbortRun)
              aborted = true
          }
        }
      }
      ws.behaviorSpaceRunNumber(runNumber)
      ws.behaviorSpaceExperimentName(protocol.name)
      setVariables(settings)
      eachListener(_.runStarted(ws, runNumber, settings))

      checkForPlotExport()
      ws.runCompiledCommands(owner(ws.world.mainRNG), setupProcedure)
      checkForRuntimeError()

      if ((protocol.runMetricsEveryStep || shouldTakeMeasurements()) && listeners.nonEmpty) {
        val m = takeMeasurements()
        eachListener(_.measurementsTaken(ws, runNumber, 0, m))
        checkForRuntimeError()
        lastMeasuredStep = 0
      }
      var steps = 0
      while((protocol.timeLimit == 0 || steps < protocol.timeLimit) &&
            !exitConditionTrue && !ws.runCompiledCommands(owner(ws.world.mainRNG), goProcedure))
      {
        checkForRuntimeError()
        steps += 1
        eachListener(_.stepCompleted(ws, steps))
        if ((protocol.runMetricsEveryStep || shouldTakeMeasurements()) && listeners.nonEmpty) {
          val m = takeMeasurements()
          eachListener(_.measurementsTaken(ws, runNumber, steps, m))
          checkForRuntimeError()
          lastMeasuredStep = steps
        }
        ws.updateDisplay(false)
        if (aborted) return
      }
      if (!protocol.runMetricsEveryStep && steps != lastMeasuredStep && listeners.nonEmpty) {
        val m = takeMeasurements()
        eachListener(_.measurementsTaken(ws, runNumber, steps, m))
        checkForRuntimeError()
      }
      ws.runCompiledCommands(owner(ws.world.mainRNG), postRunProcedure)
      checkForRuntimeError()
      eachListener(_.runCompleted(ws, runNumber, steps))
    }
  }
}
