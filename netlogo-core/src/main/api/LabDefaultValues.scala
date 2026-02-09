// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object LabDefaultValues {
  def getDefaultName: String = "experiment"
  def getDefaultPreExperimentCommands: String = ""
  def getDefaultSetupCommands: String = "setup"
  def getDefaultGoCommands: String = "go"
  def getDefaultPostRunCommands: String = ""
  def getDefaultPostExperimentCommands: String = ""
  def getDefaultRepetitions: Int = 1
  def getDefaultSequentialRunOrder: Boolean = true
  def getDefaultRunMetricsEveryStep: Boolean = true
  def getDefaultRunMetricsCondition: String = ""
  def getDefaultTimeLimit: Int = 0
  def getDefaultExitCondition: String = ""
  def getDefaultMetrics: List[String] = List("count turtles")
  def getDefaultConstants: List[RefValueSet] = Nil
  def getDefaultSubExperiments: List[List[RefValueSet]] = Nil
  // Determines the number of threads in BehaviorSpace if the user has not specified a value
  def getDefaultThreads: Int = (Runtime.getRuntime.availableProcessors * 0.75).floor.toInt
  def getRecommendedMaxThreads: Int = Runtime.getRuntime.availableProcessors
  def getDefaultMemoryLimit: Int = 0
  def getDefaultTable: String = ""
  def getDefaultSpreadsheet: String = ""
  def getDefaultStats: String = ""
  def getDefaultLists: String = ""
  def getDefaultUpdateView: Boolean = true
  def getDefaultUpdatePlotsAndMonitors: Boolean = true
  def getDefaultMirrorHeadlessOutput: Boolean = false
}
