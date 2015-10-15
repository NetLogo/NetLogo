// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

abstract class ValueSet(val variableName: String)
  extends Iterable[Any]

class EnumeratedValueSet(variableName: String,
                         values: List[Any])
  extends ValueSet(variableName)
{
  def iterator = values.iterator
}

class SteppedValueSet(variableName: String,
                      val firstValue: BigDecimal,
                      val step: BigDecimal,
                      val lastValue: BigDecimal)
  extends ValueSet(variableName)
{
  def iterator =
    Iterator.from(0)
      .map(firstValue + step * _)
      .takeWhile(_ <= lastValue)
      .map(i => Double.box(i.toDouble))
}
