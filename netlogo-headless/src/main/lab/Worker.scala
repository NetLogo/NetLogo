// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.LabProtocol
import java.util.concurrent.{Callable, Executors, TimeUnit}
import org.nlogo.core.WorldDimensions
import org.nlogo.api.{Dump,LogoException, World,
                      WorldDimensionException, SimpleJobOwner}
import org.nlogo.nvm.{LabInterface, Workspace}
import org.nlogo.api.MersenneTwisterFast
import LabInterface.ProgressListener

class Worker(val protocol: LabProtocol)
  extends LabInterface.Worker
{
  val listeners = new collection.mutable.ListBuffer[ProgressListener]
  def addListener(listener: ProgressListener) {
    listeners += listener
  }
  def addSpreadsheetWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter) {
    addListener(new SpreadsheetExporter(modelFileName, initialDims, protocol, w))
  }
  def addTableWriter(modelFileName: String, initialDims: WorldDimensions, w: java.io.PrintWriter) {
    addListener(new TableExporter(modelFileName, initialDims, protocol, w))
  }
  var runners: Seq[Runner] = null
  // we only want to compile stuff once per workspace, so use this
  // (should use a Scala collection not a Java one, but oh well, too lazy today - ST 8/13/09)
  val proceduresMap = new java.util.WeakHashMap[Workspace, Procedures]
  def run(initialWorkspace: Workspace, fn: ()=>Workspace, threads: Int) {
    val executor = Executors.newFixedThreadPool(threads)
    try {
      listeners.foreach(_.experimentStarted())
      runners =
        (for((settings, runNumber) <- protocol.elements zip Stream.from(1).iterator)
         yield new Runner(runNumber, settings, fn)).toSeq
      val futures = {
        import collection.JavaConverters._
        // The explicit use of JavaConversions here with a type parameter, instead of just plain
        // "asJava", is required to compile against Java 5 - ST 8/17/11
        executor.invokeAll(runners.asJava).asScala
      }
      executor.shutdown()
      executor.awaitTermination(Int.MaxValue, TimeUnit.SECONDS)
      listeners.foreach(_.experimentCompleted())
      // this will cause the first ExecutionException we got to be thrown - ST 3/10/09
      futures.foreach(_.get)
    }
    catch { case _: InterruptedException => listeners.foreach(_.experimentAborted()) }
    finally {
      // "Invocation has no additional effect if already shut down." - API doc.
      // We need to be completely sure the executor is shut down otherwise we leak
      // threads (ticket #1185). - ST 2/11/11
      executor.shutdown()
      runners = null
    }
  }
  // result discarded -- we just want to see if compilation succeeds.
  // used in TestCompileAll, also used before the start of the
  // experiment in the GUI so if something doesn't compile we can fail early.
  def compile(w: Workspace) { new Procedures(w) }
  def abort() { if(runners != null) runners.foreach(_.aborted = true) }
  class Procedures(workspace: Workspace) {
    val setupProcedure = workspace.compileCommands(protocol.setupCommands)
    val goProcedure = workspace.compileCommands(protocol.goCommands
                                                + "\n" // protect against comments
                                                + "__experimentstepend")
    val finalProcedure = workspace.compileCommands(protocol.finalCommands)
    val exitProcedure =
      if(protocol.exitCondition.trim == "") None
      else Some(workspace.compileReporter(protocol.exitCondition))
    val metricProcedures = protocol.metrics.map(workspace.compileReporter(_))
  }
  class Runner(runNumber: Int, settings: List[(String, Any)], fn: ()=>Workspace)
    extends Callable[Unit]
  {
    class FailedException(message: String) extends LogoException(message)
    private def owner(rng: MersenneTwisterFast) =
      new SimpleJobOwner("BehaviorSpace", rng)
    @volatile var aborted = false
    // each Runner is on its own thread, but all the Runners share a ProgressListener,
    // so we need to synchronize
    def eachListener(fn: (ProgressListener)=>Unit) {
      listeners.synchronized { listeners.foreach(fn) }
    }
    def call() {
      // not clear why this check would be necessary, but perhaps it will
      // keep bug #1203 from happening - ST 2/16/11
      if(!aborted) {
        val workspace = fn.apply
        try callHelper(workspace)
        catch { case t: Throwable =>
          if(!aborted) eachListener(_.runtimeError(workspace, runNumber, t)) }
      }
    }
    def callHelper(ws: Workspace) {
      val procedures =
        if(proceduresMap.containsKey(ws))
          proceduresMap.get(ws)
        else {
          val newProcedures = new Procedures(ws)
          proceduresMap.put(ws, newProcedures)
          newProcedures
        }
      import procedures._
      def setVariables(settings: List[(String, Any)]) {
        val world = ws.world
        var d = world.getDimensions
        for((name, value) <- settings) {
          if(world.isDimensionVariable(name)) {
            val v = value.asInstanceOf[java.lang.Double].intValue
            try { d = setDimensionVariable(world, name, v, d) }
            catch {
              case e: WorldDimensionException =>
                throw new FailedException("You cannot set " + name + " to " + v)
            }
          }
          else if(name.equalsIgnoreCase("RANDOM-SEED"))
            ws.world.mainRNG.setSeed(value.asInstanceOf[java.lang.Double].longValue)
        }
        if(!world.equalDimensions(d)) ws.setDimensions(d)
        for((name, value) <- settings)
          if(!world.isDimensionVariable(name) && !name.equalsIgnoreCase("RANDOM-SEED"))
            ws.world.synchronized {
              if(ws.world.observerOwnsIndexOf(name.toUpperCase) == -1)
                throw new FailedException(
                  "Global variable does not exist:\n" + name)
              ws.world.setObserverVariableByName(name, value.asInstanceOf[AnyRef])
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
      def takeMeasurements(): List[AnyRef] =
        metricProcedures.map{proc =>
          val result = ws.runCompiledReporter(owner(ws.world.mainRNG.clone), proc)
          if(result == null)
            throw new FailedException(
              "Reporter for measuring runs failed to report a result:\n" + result)
          result }
      def checkForRuntimeError() {
        if(ws.lastLogoException != null) {
          val ex = ws.lastLogoException
          ws.clearLastLogoException()
          if(!aborted)
            eachListener(_.runtimeError(ws, runNumber, ex))
        }
      }
      ws.behaviorSpaceRunNumber(runNumber)
      setVariables(settings)
      eachListener(_.runStarted(ws, runNumber, settings))
      ws.runCompiledCommands(owner(ws.world.mainRNG), setupProcedure)
      checkForRuntimeError()
      if(protocol.runMetricsEveryStep && listeners.nonEmpty) {
        val m = takeMeasurements()
        eachListener(_.measurementsTaken(ws, runNumber, 0, m))
        checkForRuntimeError()
      }
      var steps = 0
      while((protocol.timeLimit == 0 || steps < protocol.timeLimit) &&
            !exitConditionTrue && !ws.runCompiledCommands(owner(ws.world.mainRNG), goProcedure))
      {
        checkForRuntimeError()
        steps += 1
        eachListener(_.stepCompleted(ws, steps))
        if(protocol.runMetricsEveryStep && listeners.nonEmpty) {
          val m = takeMeasurements()
          eachListener(_.measurementsTaken(ws, runNumber, steps, m))
          checkForRuntimeError()
        }
        ws.updateDisplay(false)
        if(aborted) return
      }
      if(!protocol.runMetricsEveryStep && listeners.nonEmpty) {
        val m = takeMeasurements()
        eachListener(_.measurementsTaken(ws, runNumber, steps, m))
        checkForRuntimeError()
      }
      ws.runCompiledCommands(owner(ws.world.mainRNG), finalProcedure)
      checkForRuntimeError()
      eachListener(_.runCompleted(ws, runNumber, steps))
    }
  }

  /// dimension variable handling

  // obviously hastily converted from Java to Scala; needs work - ST 8/22/13

  def setDimensionVariable(world: World, variableName: String, value: Int, dim: WorldDimensions): WorldDimensions = {
    case class MutableWorldDimensions(
      var minPxcor: Int, var maxPxcor: Int,
      var minPycor: Int, var maxPycor: Int)
    val d = MutableWorldDimensions(
      dim.minPxcor, dim.maxPxcor,
      dim.minPycor, dim.maxPycor)
    if (variableName.equalsIgnoreCase("MIN-PXCOR")) {
      d.minPxcor_$eq(value);
    } else if (variableName.equalsIgnoreCase("MAX-PXCOR")) {
      d.maxPxcor_$eq(value);
    } else if (variableName.equalsIgnoreCase("MIN-PYCOR")) {
      d.minPycor_$eq(value);
    } else if (variableName.equalsIgnoreCase("MAX-PYCOR")) {
      d.maxPycor_$eq(value);
    } else if (variableName.equalsIgnoreCase("WORLD-WIDTH")) {
      d.minPxcor_$eq(growMin(world.minPxcor, world.maxPxcor, value, d.minPxcor));
      d.maxPxcor_$eq(growMax(world.minPxcor, world.maxPxcor, value, d.maxPxcor));
    } else if (variableName.equalsIgnoreCase("WORLD-HEIGHT")) {
      d.minPycor_$eq(growMin(world.minPycor, world.maxPycor, value, d.minPycor));
      d.maxPycor_$eq(growMax(world.minPycor, world.maxPycor, value, d.maxPycor));
    }
    WorldDimensions(
      d.minPxcor, d.maxPxcor,
      d.minPycor, d.maxPycor)
  }

  private def growMin(min: Int, max: Int, value: Int, d: Int): Int = {
    if (value < 1) {
      throw new WorldDimensionException
    }
    if (max == -min) {
      if (value % 2 != 1) {
        throw new WorldDimensionException
      }
      return -(value - 1) / 2;
    } else if (max == 0) {
      return -(value - 1);
    }
    d
  }

  private def growMax(min: Int, max: Int, value: Int, d: Int): Int = {
    if (value < 1) {
      throw new WorldDimensionException
    }
    if (max == -min) {
      if (value % 2 != 1) {
        throw new WorldDimensionException()
      }
      return (value - 1) / 2;
    } else if (min == 0) {
      return (value - 1);
    }
    d
  }

}
