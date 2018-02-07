// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait ParameterSet {
  def countRuns: Int
  def iterator: Iterator[List[(String, AnyRef)]]
}

object CartesianProductParameterSet {
  def empty: CartesianProductParameterSet = CartesianProductParameterSet(1, true, Nil)
}

case class CartesianProductParameterSet(repetitions: Int, sequentialRunOrder: Boolean, valueSets: List[RefValueSet]) extends ParameterSet {
  def countRuns = repetitions * valueSets.map(_.toList.size).product

  def iterator: Iterator[List[(String, AnyRef)]] = {
    def combinations(sets: List[RefValueSet]): Iterator[List[(String, AnyRef)]] =
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
