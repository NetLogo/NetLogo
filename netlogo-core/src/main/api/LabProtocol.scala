// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object LabProtocol {
  def defaultGUIProtocol: LabProtocol = {
    new LabProtocol(
      LabDefaultValues.getDefaultName,
      LabDefaultValues.getDefaultPreExperimentCommands,
      LabDefaultValues.getDefaultSetupCommands,
      LabDefaultValues.getDefaultGoCommands,
      LabDefaultValues.getDefaultPostRunCommands,
      LabDefaultValues.getDefaultPostExperimentCommands,
      LabDefaultValues.getDefaultRepetitions,
      LabDefaultValues.getDefaultSequentialRunOrder,
      LabDefaultValues.getDefaultRunMetricsEveryStep,
      LabDefaultValues.getDefaultRunMetricsCondition,
      LabDefaultValues.getDefaultTimeLimit,
      LabDefaultValues.getDefaultExitCondition,
      LabDefaultValues.getDefaultMetrics,
      LabDefaultValues.getDefaultConstants,
      LabDefaultValues.getDefaultSubExperiments
    )
  }

  def defaultCodeProtocol(name: String): LabProtocol = {
    new LabProtocol(
      name,
      "",
      "",
      "",
      "",
      "",
      LabDefaultValues.getDefaultRepetitions,
      LabDefaultValues.getDefaultSequentialRunOrder,
      LabDefaultValues.getDefaultRunMetricsEveryStep,
      "",
      LabDefaultValues.getDefaultTimeLimit,
      "",
      Nil,
      Nil,
      Nil
    )
  }
}

class LabProtocol(
  var name: String,
  var preExperimentCommands: String,
  var setupCommands: String,
  var goCommands: String,
  var postRunCommands: String,
  var postExperimentCommands: String,
  var repetitions: Int,
  var sequentialRunOrder: Boolean,
  var runMetricsEveryStep: Boolean,
  var runMetricsCondition: String,
  var timeLimit: Int,
  var exitCondition: String,
  var metrics: List[String],
  var constants: List[RefValueSet],
  var subExperiments: List[List[RefValueSet]],
  var threadCount: Int = LabDefaultValues.getDefaultThreads,
  var table: String = LabDefaultValues.getDefaultTable,
  var spreadsheet: String = LabDefaultValues.getDefaultSpreadsheet,
  var stats: String = LabDefaultValues.getDefaultStats,
  var lists: String = LabDefaultValues.getDefaultLists,
  var updateView: Boolean = LabDefaultValues.getDefaultUpdateView,
  var updatePlotsAndMonitors: Boolean = LabDefaultValues.getDefaultUpdatePlotsAndMonitors,
  var mirrorHeadlessOutput: Boolean = LabDefaultValues.getDefaultMirrorHeadlessOutput,
  var runsCompleted: Int = 0
) {
  val valueSets =
    if (subExperiments.isEmpty)
      List(constants)
    else {
      val variables = (constants.map(_.variableName) ::: subExperiments.flatten.map(_.variableName)).distinct
      for (subExperiment <- subExperiments) yield {
        var filled = List[RefValueSet]()
        for (variable <- variables) {
          filled = filled :+ subExperiment.find(_.variableName == variable)
                                          .getOrElse(constants.find(_.variableName == variable)
                                          .getOrElse(new RefEnumeratedValueSet(variable, List(null).asInstanceOf[List[AnyRef]])))
        }
        filled
      }
    }

  def countRuns = repetitions * valueSets.map(_.map(_.length.toInt).product).sum

  // Generate all the possible combinations of values from the ValueSets, in order.  (I'm using
  // Iterator here so that each combination we generate can be garbage collected when we're done
  // with it, instead of them all being held in memory until the end of the experiment.
  // - ST 5/1/08, see bug #63 - ST 2/28/12
  @deprecated("6.0.2", "use AnyRefSettingsIterator instead")
  type SettingsIterator = Iterator[List[(String, Any)]]

  @deprecated("6.0.2", "use refElements instead")
  def elements: SettingsIterator = refElements

  type AnyRefSettingsIterator = Iterator[List[(String, AnyRef)]]

  def refElements: AnyRefSettingsIterator = {
    def combinations(sets: List[RefValueSet]): AnyRefSettingsIterator =
      sets match {
        case Nil => Iterator(Nil)
        case set::sets =>
          set.iterator.flatMap(v =>
            combinations(sets).map(m =>
              if (sequentialRunOrder) (set.variableName,v) :: m
              else m :+ set.variableName -> v))
      }
    if (sequentialRunOrder) {
      valueSets.map(combinations(_).flatMap(x => Iterator.fill(repetitions)(x))).flatten.iterator
    }
    else {
      Iterator.fill(repetitions)(valueSets.map(x => combinations(x.reverse)).flatten).flatten
    }
  }

  def copy(
    name: String = name,
    preExperimentCommands: String = preExperimentCommands,
    setupCommands: String = setupCommands,
    goCommands: String = goCommands,
    postRunCommands: String = postRunCommands,
    postExperimentCommands: String = postExperimentCommands,
    repetitions: Int = repetitions,
    sequentialRunOrder: Boolean = sequentialRunOrder,
    runMetricsEveryStep: Boolean = runMetricsEveryStep,
    runMetricsCondition: String = runMetricsCondition,
    timeLimit: Int = timeLimit,
    exitCondition: String = exitCondition,
    metrics: List[String] = metrics,
    constants: List[RefValueSet] = constants,
    subExperiments: List[List[RefValueSet]] = subExperiments,
    threadCount: Int = threadCount,
    table: String = table,
    spreadsheet: String = spreadsheet,
    stats: String = stats,
    lists: String = lists,
    updateView: Boolean = updateView,
    updatePlotsAndMonitors: Boolean = updatePlotsAndMonitors,
    mirrorHeadlessOutput: Boolean = mirrorHeadlessOutput,
    runsCompleted: Int = runsCompleted
  ): LabProtocol = {
    new LabProtocol(
      name,
      preExperimentCommands,
      setupCommands,
      goCommands,
      postRunCommands,
      postExperimentCommands,
      repetitions,
      sequentialRunOrder,
      runMetricsEveryStep,
      runMetricsCondition,
      timeLimit,
      exitCondition,
      metrics,
      constants,
      subExperiments,
      threadCount,
      table,
      spreadsheet,
      stats,
      lists,
      updateView,
      updatePlotsAndMonitors,
      mirrorHeadlessOutput,
      runsCompleted
    )
  }
}
