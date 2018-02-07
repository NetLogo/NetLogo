// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object LabProtocol {
  @deprecated("6.1.0", "use LabProtocol.fromValueSets instead")
  def apply(name: String,
            setupCommands: String,
            goCommands: String,
            finalCommands: String,
            repetitions: Int,
            sequentialRunOrder: Boolean,
            runMetricsEveryStep: Boolean,
            timeLimit: Int,
            exitCondition: String,
            metrics: List[String],
            valueSets: List[RefValueSet]) =
              new LabProtocol(name, setupCommands, goCommands, finalCommands, repetitions,
                sequentialRunOrder, runMetricsEveryStep, timeLimit, exitCondition, metrics, valueSets)

  def fromValueSets(name: String,
            setupCommands: String,
            goCommands: String,
            finalCommands: String,
            repetitions: Int,
            sequentialRunOrder: Boolean,
            runMetricsEveryStep: Boolean,
            timeLimit: Int,
            exitCondition: String,
            metrics: List[String],
            valueSets: List[RefValueSet]): LabProtocol =
    LabProtocol(name, setupCommands, goCommands, finalCommands, runMetricsEveryStep, timeLimit, exitCondition, metrics,
      CartesianProductParameterSet(repetitions, sequentialRunOrder, valueSets))

}

case class LabProtocol(name: String,
                    setupCommands: String,
                    goCommands: String,
                    finalCommands: String,
                    runMetricsEveryStep: Boolean,
                    timeLimit: Int,
                    exitCondition: String,
                    metrics: List[String],
                    parameterSet: ParameterSet) {

  @deprecated("6.1.0", "use LabProtocol.fromValueSets instead")
  def this(name: String,
            setupCommands: String,
            goCommands: String,
            finalCommands: String,
            repetitions: Int,
            sequentialRunOrder: Boolean,
            runMetricsEveryStep: Boolean,
            timeLimit: Int,
            exitCondition: String,
            metrics: List[String],
            valueSets: List[RefValueSet]) =
    this(name, setupCommands, goCommands, finalCommands, runMetricsEveryStep, timeLimit, exitCondition, metrics,
      CartesianProductParameterSet(repetitions, sequentialRunOrder, valueSets))

  def countRuns = parameterSet.countRuns

  @deprecated("6.1.0", "inspect parameterSet or use countRuns instead")
  def sequentialRunOrder: Boolean = parameterSet match {
    case c: CartesianProductParameterSet => c.sequentialRunOrder
    case _ => true
  }

  @deprecated("6.1.0", "inspect parameterSet instead")
  def repetitions: Int = parameterSet match {
    case c: CartesianProductParameterSet => c.repetitions
    case _ => 0
  }

  @deprecated("6.1.0", "inspect parameterSet instead")
  def valueSets: List[RefValueSet] = parameterSet match {
    case c: CartesianProductParameterSet => c.valueSets
    case _ => Nil
  }

  // Generate all the possible combinations of values from the ValueSets, in order.  (I'm using
  // Iterator here so that each combination we generate can be garbage collected when we're done
  // with it, instead of them all being held in memory until the end of the experiment.
  // - ST 5/1/08, see bug #63 - ST 2/28/12
  @deprecated("6.0.2", "use AnyRefSettingsIterator instead")
  type SettingsIterator = Iterator[List[(String, Any)]]

  @deprecated("6.0.2", "use refElements instead")
  def elements: SettingsIterator = refElements

  type AnyRefSettingsIterator = Iterator[List[(String, AnyRef)]]

  def refElements: AnyRefSettingsIterator = parameterSet.iterator
}
