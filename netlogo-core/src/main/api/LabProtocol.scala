// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

case class LabProtocol(name: String,
                    setupCommands: String,
                    goCommands: String,
                    finalCommands: String,
                    repetitions: Int,
                    sequentialRunOrder: Boolean,
                    runMetricsEveryStep: Boolean,
                    timeLimit: Int,
                    exitCondition: String,
                    metrics: List[String],
                    valueSets: List[RefValueSet])
{
  def countRuns = repetitions * valueSets.map(_.toList.size).product

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
              if(sequentialRunOrder) (set.variableName,v) :: m
              else m :+ set.variableName -> v))
      }
    if(sequentialRunOrder) combinations(valueSets)
      .flatMap(Iterator.fill(repetitions)(_))
    else {
      val runners = combinations(valueSets.reverse).toList
      (for(i <- 1 to repetitions) yield runners).flatten.toIterator
    }
  }
}
