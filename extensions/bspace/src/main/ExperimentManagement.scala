// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.extensions.bspace

import org.nlogo.api.{ Argument, Command, Context, LabProtocol, LabRunOptions }
import org.nlogo.core.I18N
import org.nlogo.core.Syntax._
import org.nlogo.lab.gui.Supervisor
import org.nlogo.window.GUIWorkspace

object CreateExperiment extends Command {
  override def getSyntax = {
    commandSyntax(right = List(StringType))
  }

  def perform(args: Array[Argument], context: Context) {
    if (BehaviorSpaceExtension.experimentType(args(0).getString, context) != ExperimentType.None)
      return BehaviorSpaceExtension.nameError(I18N.gui.getN("tools.behaviorSpace.extension.alreadyExists", args(0).getString), context)
    if (args(0).getString.isEmpty)
      return BehaviorSpaceExtension.nameError(I18N.gui.get("edit.behaviorSpace.name.empty"), context)

    BehaviorSpaceExtension.experiments += ((args(0).getString, new ExperimentData()))
    BehaviorSpaceExtension.experiments(args(0).getString).name = args(0).getString
  }
}

object DeleteExperiment extends Command {
  override def getSyntax = {
    commandSyntax(right = List(StringType))
  }

  def perform(args: Array[Argument], context: Context) {
    if (!BehaviorSpaceExtension.validateForEditing(args(0).getString, context)) return

    BehaviorSpaceExtension.experiments -= args(0).getString
  }
}

object RunExperiment extends Command {
  override def getSyntax = {
    commandSyntax(right = List(StringType))
  }

  def perform(args: Array[Argument], context: Context) {
    val ws = context.workspace.asInstanceOf[GUIWorkspace]

    val protocol = BehaviorSpaceExtension.experimentType(args(0).getString, context) match {
      case ExperimentType.GUI =>
        context.workspace.getBehaviorSpaceExperiments.find(x => x.name == args(0).getString).get
      case ExperimentType.Code =>
        if (BehaviorSpaceExtension.savedExperiments.contains(args(0).getString))
          BehaviorSpaceExtension.savedExperiments(args(0).getString)
        else {
          val data = BehaviorSpaceExtension.experiments(args(0).getString)

          new LabProtocol(data.name, data.preExperimentCommands, data.setupCommands, data.goCommands,
                                          data.postRunCommands, data.postExperimentCommands, data.repetitions,
                                          data.sequentialRunOrder, data.runMetricsEveryStep, data.runMetricsCondition,
                                          data.timeLimit, data.exitCondition, data.metrics, data.constants,
                                          data.subExperiments, data.returnReporters.toMap,
                                          runOptions = new LabRunOptions(data.threadCount, data.table,
                                                                            data.spreadsheet, data.stats, data.lists,
                                                                            data.updateView,
                                                                            data.updatePlotsAndMonitors))
        }
      case _ => return BehaviorSpaceExtension.nameError(I18N.gui.getN("tools.behaviorSpace.extension.noExperiment", args(0).getString), context)
    }

    javax.swing.SwingUtilities.invokeLater(() => {
      Supervisor.runFromExtension(protocol, context.workspace.asInstanceOf[GUIWorkspace],
                                  org.nlogo.app.App.app.workspaceFactory, (protocol) => {
        if (BehaviorSpaceExtension.savedExperiments.contains(protocol.name)) {
          if (protocol.runsCompleted == 0)
            BehaviorSpaceExtension.savedExperiments -= protocol.name
          else
            BehaviorSpaceExtension.savedExperiments(protocol.name) = protocol
        }
        else if (protocol.runsCompleted != 0)
          BehaviorSpaceExtension.savedExperiments += ((protocol.name, protocol))
      })
    })
  }
}

object RenameExperiment extends Command {
  override def getSyntax = {
    commandSyntax(right = List(StringType, StringType))
  }

  def perform(args: Array[Argument], context: Context) {
    if (!BehaviorSpaceExtension.validateForEditing(args(0).getString, context)) return
    if (BehaviorSpaceExtension.experimentType(args(1).getString, context) != ExperimentType.None)
      return BehaviorSpaceExtension.nameError(I18N.gui.getN("tools.behaviorSpace.extension.alreadyExists", args(1).getString), context)
    if (args(1).getString.isEmpty)
      return BehaviorSpaceExtension.nameError(I18N.gui.get("edit.behaviorSpace.name.empty"), context)

    val data = BehaviorSpaceExtension.experiments(args(0).getString)

    data.name = args(1).getString

    BehaviorSpaceExtension.experiments -= args(0).getString
    BehaviorSpaceExtension.experiments += ((args(1).getString, data))
  }
}
