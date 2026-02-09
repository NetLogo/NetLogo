// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object LabProtocol {
  private type AnyRefSettingsIterator = Iterator[List[(String, AnyRef)]]

  def defaultGUIProtocol: LabProtocol = {
    LabProtocol(
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
    LabProtocol(
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

  def refElementsFor(constants: List[RefValueSet],
                     subExperiments: List[List[RefValueSet]]): AnyRefSettingsIterator =
    defaultGUIProtocol.copy(constants = constants, subExperiments = subExperiments).refElements
}

case class LabProtocol(
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
  var metricsForSaving: List[String],
  var constants: List[RefValueSet],
  var subExperiments: List[List[RefValueSet]],
  var threadCount: Int = LabDefaultValues.getDefaultThreads,
  var memoryLimit: Int = LabDefaultValues.getDefaultMemoryLimit,
  var table: String = LabDefaultValues.getDefaultTable,
  var spreadsheet: String = LabDefaultValues.getDefaultSpreadsheet,
  var stats: String = LabDefaultValues.getDefaultStats,
  var lists: String = LabDefaultValues.getDefaultLists,
  var updateView: Boolean = LabDefaultValues.getDefaultUpdateView,
  var updatePlotsAndMonitors: Boolean = LabDefaultValues.getDefaultUpdatePlotsAndMonitors,
  var mirrorHeadlessOutput: Boolean = LabDefaultValues.getDefaultMirrorHeadlessOutput,
  var runsCompleted: Int = 0
) {
  import LabProtocol.AnyRefSettingsIterator

  def valueSets: List[List[RefValueSet]] = {
    if (subExperiments.isEmpty) {
      List(constants)
    } else {
      val variables = (constants.map(_.variableName) ::: subExperiments.flatten.map(_.variableName)).distinct
      for (subExperiment <- subExperiments) yield {
        var filled = List[RefValueSet]()
        for (variable <- variables) {
          filled = filled :+
            subExperiment.find(_.variableName == variable).getOrElse(constants.find(_.variableName == variable)
                          .getOrElse(new RefEnumeratedValueSet(variable, List(null).asInstanceOf[List[AnyRef]])))
        }
        filled
      }
    }
  }

  def countRuns = repetitions * valueSets.map(_.map(_.length.toInt).product).sum

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

  // metrics excluding comments; this helps Worker and ProgressDialog more easily track
  // the list of valid metrics for an experiment (Isaac B 7/13/25)
  def metrics: List[String] =
    metricsForSaving.filter(!_.trim.startsWith(";")).map(m => ";.*$".r.replaceFirstIn(m, "").trim)

}
