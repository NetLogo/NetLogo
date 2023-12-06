package org.nlogo.extensions.bspace

import org.nlogo.api
import org.nlogo.core.Syntax._
import org.nlogo.lab.gui.Supervisor
import org.nlogo.window.GUIWorkspace

import scala.collection.mutable.Map

class ExperimentData {
  var name = ""
  var preExperimentCommands = ""
  var setupCommands = ""
  var goCommands = ""
  var postRunCommands = ""
  var postExperimentCommands = ""
  var repetitions = 1
  var sequentialRunOrder = true
  var runMetricsEveryStep = true
  var runMetricsCondition = ""
  var timeLimit = 0
  var exitCondition = ""
  var metrics: List[String] = Nil
  var variables = ""
  var threadCount = 1
  var table = ""
  var spreadsheet = ""
  var stats = ""
  var lists = ""
  var updateView = true
  var updatePlotsAndMonitors = true
}

class BehaviorSpaceExtension extends api.DefaultClassManager {
  val experiments = Map[String, ExperimentData]()

  def load(manager: api.PrimitiveManager) {
    manager.addPrimitive("create-experiment", CreateExperiment)
    manager.addPrimitive("run-experiment", RunExperiment)
    manager.addPrimitive("set-name", SetName)
    manager.addPrimitive("set-pre-experiment-commands", SetPreExperimentCommands)
    manager.addPrimitive("set-setup-commands", SetSetupCommands)
    manager.addPrimitive("set-go-commands", SetGoCommands)
    manager.addPrimitive("set-post-run-commands", SetPostRunCommands)
    manager.addPrimitive("set-post-experiment-commands", SetPostExperimentCommands)
    manager.addPrimitive("set-repetitions", SetRepetitions)
    manager.addPrimitive("set-sequential-run-order", SetSequentialRunOrder)
    manager.addPrimitive("set-run-metrics-every-step", SetRunMetricsEveryStep)
    manager.addPrimitive("set-run-metrics-condition", SetRunMetricsCondition)
    manager.addPrimitive("set-time-limit", SetTimeLimit)
    manager.addPrimitive("set-exit-condition", SetExitCondition)
    manager.addPrimitive("set-metrics", SetMetrics)
    manager.addPrimitive("set-variables", SetVariables)
    manager.addPrimitive("set-thread-count", SetThreadCount)
    manager.addPrimitive("set-table", SetTable)
    manager.addPrimitive("set-spreadsheet", SetSpreadsheet)
    manager.addPrimitive("set-stats", SetStats)
    manager.addPrimitive("set-lists", SetLists)
    manager.addPrimitive("set-update-view", SetUpdateView)
    manager.addPrimitive("set-update-plots", SetUpdatePlots)
  }

  object CreateExperiment extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      experiments += ((args(0).getString, new ExperimentData()))
    }
  }

  object RunExperiment extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      val data = experiments(args(0).getString)

      val ws = context.workspace.asInstanceOf[GUIWorkspace]

      val parsed = api.LabProtocol.parseVariables(data.variables, context.world, ws)

      if (!parsed.isDefined)
        return println("Invalid variable definition.")

      val protocol = new api.LabProtocol(data.name, data.preExperimentCommands, data.setupCommands, data.goCommands,
                                         data.postRunCommands, data.postExperimentCommands, data.repetitions,
                                         data.sequentialRunOrder, data.runMetricsEveryStep, data.runMetricsCondition,
                                         data.timeLimit, data.exitCondition, data.metrics, parsed.get._1, parsed.get._2,
                                         runOptions = new api.LabRunOptions(data.threadCount, data.table,
                                                                            data.spreadsheet, data.stats, data.lists,
                                                                            data.updateView, data.updatePlotsAndMonitors))

      javax.swing.SwingUtilities.invokeLater(() => {
        Supervisor.runFromExtension(protocol, context.workspace.asInstanceOf[GUIWorkspace])
      })
    }
  }

  object SetName extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      val data = experiments(args(0).getString)

      data.name = args(1).getString

      experiments -= args(0).getString
      experiments += ((args(1).getString, data))
    }
  }

  object SetPreExperimentCommands extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).preExperimentCommands = args(1).getString
    }
  }

  object SetSetupCommands extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).setupCommands = args(1).getString
    }
  }

  object SetGoCommands extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).goCommands = args(1).getString
    }
  }

  object SetPostRunCommands extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).postRunCommands = args(1).getString
    }
  }

  object SetPostExperimentCommands extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).postExperimentCommands = args(1).getString
    }
  }

  object SetRepetitions extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, NumberType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).repetitions = args(1).getIntValue
    }
  }

  object SetSequentialRunOrder extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, BooleanType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).sequentialRunOrder = args(1).getBooleanValue
    }
  }

  object SetRunMetricsEveryStep extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, BooleanType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).runMetricsEveryStep = args(1).getBooleanValue
    }
  }

  object SetRunMetricsCondition extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).runMetricsCondition = args(1).getString
    }
  }

  object SetTimeLimit extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, NumberType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).timeLimit = args(1).getIntValue
    }
  }

  object SetExitCondition extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).exitCondition = args(1).getString
    }
  }

  object SetMetrics extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, ListType | StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).metrics = args(1).getList.toList.map(_.toString)
    }
  }

  object SetVariables extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).variables = args(1).getString
    }
  }

  object SetThreadCount extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, NumberType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).threadCount = args(1).getIntValue
    }
  }

  object SetTable extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).table = args(1).getString
    }
  }

  object SetSpreadsheet extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).spreadsheet = args(1).getString
    }
  }

  object SetStats extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).stats = args(1).getString
    }
  }

  object SetLists extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, StringType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).lists = args(1).getString
    }
  }

  object SetUpdateView extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, BooleanType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).updateView = args(1).getBooleanValue
    }
  }

  object SetUpdatePlots extends api.Command {
    override def getSyntax = {
      commandSyntax(right = List(StringType, BooleanType))
    }

    def perform(args: Array[api.Argument], context: api.Context) {
      if (!experiments.contains(args(0).getString))
        return println("No experiment exists with that name.")

      experiments(args(0).getString).updatePlotsAndMonitors = args(1).getBooleanValue
    }
  }
}
