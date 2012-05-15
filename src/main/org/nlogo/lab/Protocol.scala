// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

case class Protocol(name: String,
                    setupCommands: String,
                    goCommands: String,
                    finalCommands: String,
                    repetitions: Int,
                    runMetricsEveryStep: Boolean,
                    timeLimit: Int,
                    exitCondition: String,
                    metrics: List[String],
                    valueSets: List[ValueSet])
{
  def countRuns = repetitions * valueSets.map(_.toList.size).product
  // careful, gui.ManagerDialog shows this to the user
  override def toString =
    name + " (" + countRuns + " run" + (if(countRuns != 1) "s" else "") + ")"
  // Generate all the possible combinations of values from the ValueSets, in order.  (I'm using
  // Iterator here so that each combination we generate can be garbage collected when we're done
  // with it, instead of them all being held in memory until the end of the experiment.
  // - ST 5/1/08, see bug #63 - ST 2/28/12
  type SettingsIterator = Iterator[List[Pair[String, Any]]]
  def elements: SettingsIterator = {
    def combinations(sets: List[ValueSet]): SettingsIterator =
      sets match {
        case Nil => Iterator(Nil)
        case set::sets =>
          set.iterator.flatMap(v =>
            combinations(sets).map(m =>
              (set.variableName,v) :: m))
      }
    combinations(valueSets)
      .flatMap(Iterator.fill(repetitions)(_))
  }
}
