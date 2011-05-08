// (c) 2009-2011 Uri Wilensky. See README.txt for terms of use.

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
  def countRuns = repetitions * valueSets.foldLeft(1)(_ * _.toList.size)
  // careful, gui.ManagerDialog shows this to the user
  override def toString =
    name + " (" + countRuns + " run" + (if(countRuns != 1) "s" else "") + ")"
  // Generate all the possible combinations of values from the ValueSets, in order.  (I'm using
  // Iterator here in the vague hope that each combination we generate can be garbage collected when
  // we're done with it, instead of them all being held in memory until the end of the experiment.
  // Does it really matter? Probably not.  Do I actually *know* that the following code lets us
  // iterate through the combinations without holding them all in memory?  No. - ST 5/1/08)
  type SettingsIterator = Iterator[List[Pair[String,Any]]]
  def elements:SettingsIterator = {
    def combinations(sets: List[ValueSet]): SettingsIterator =
      sets match {
        case Nil => Iterator.single(Nil)
        case set::sets =>
          set.iterator.flatMap(v =>
            combinations(sets).map(m =>
              (set.variableName,v) :: m))
      }
    combinations(valueSets)
      .flatMap(Stream.fill(repetitions)(_).iterator)
  }
}
