// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.nlogo.agent.{World, Agent, Observer, AbstractExporter, AgentSet, ArrayAgentSet}
import org.nlogo.api.{PlotInterface, Dump, CommandLogoThunk, ReporterLogoThunk, CompilerException, JobOwner, SimpleJobOwner}
import org.nlogo.nvm.{Instruction, EngineException, Context, Procedure}
import org.nlogo.plot.{ PlotExporter, PlotManager }

import java.io.{IOException,PrintWriter}

import AbstractWorkspaceTraits._

object AbstractWorkspaceScala {
  val DefaultPreviewCommands = "setup repeat 75 [ go ]"
}

abstract class AbstractWorkspaceScala(private val _world: World)
  extends AbstractWorkspace(_world)
  with Plotting with Exporting with Evaluating {

  /**
   * previewCommands used by make-preview and model test
   */
  var previewCommands = AbstractWorkspaceScala.DefaultPreviewCommands

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

  trait Exporting extends Plotting { this: AbstractWorkspace =>

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

    def exportPlotsToCSV(writer: PrintWriter) {
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
    @throws(classOf[CompilerException])
    def makeReporterThunk(source: String, jobOwnerName: String): ReporterLogoThunk =
      evaluator.makeReporterThunk(source, world.observer,
                                  new SimpleJobOwner(jobOwnerName, auxRNG, classOf[Observer]))
    @throws(classOf[CompilerException])
    def makeCommandThunk(source: String, jobOwnerName: String): CommandLogoThunk =
      evaluator.makeCommandThunk(source, world.observer,
                                 new SimpleJobOwner(jobOwnerName, auxRNG, classOf[Observer]))
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
      val agents = new ArrayAgentSet(agent.getAgentClass, 1, false, world)
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
      val agents = new ArrayAgentSet(agent.getAgentClass, 1, false, world)
      agents.add(agent)
      evaluator.evaluateReporter(owner, source, agents)
    }
    @throws(classOf[CompilerException])
    def evaluateReporter(owner: JobOwner, source: String, agents: AgentSet) =
      evaluator.evaluateReporter(owner, source, agents)
    @throws(classOf[CompilerException])
    def compileCommands(source: String): Procedure =
      compileCommands(source, classOf[Observer])
    @throws(classOf[CompilerException])
    def compileCommands(source: String, agentClass: Class[_ <: Agent]): Procedure =
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
  }
}
