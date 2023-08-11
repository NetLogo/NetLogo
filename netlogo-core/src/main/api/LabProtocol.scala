// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

case class LabProtocol(name: String,
                    setupCommands: String,
                    goCommands: String,
                    finalCommands: String,
                    repetitions: Int,
                    sequentialRunOrder: Boolean,
                    runMetricsEveryStep: Boolean,
                    runMetricsCondition: String,
                    timeLimit: Int,
                    exitCondition: String,
                    metrics: List[String],
                    constants: List[RefValueSet],
                    subExperiments: List[List[RefValueSet]] = Nil)
{
  val valueSets =
    if (subExperiments.isEmpty)
      List(constants)
    else {
      val variables = subExperiments.flatten.map(_.variableName).distinct
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
    valueSets.map(set =>
      if (sequentialRunOrder) combinations(set)
        .flatMap(Iterator.fill(repetitions)(_))
      else {
        val runners = combinations(set.reverse).toList
        (for(i <- 1 to repetitions) yield runners).flatten.toIterator
      }).flatten.toIterator
  }
}
