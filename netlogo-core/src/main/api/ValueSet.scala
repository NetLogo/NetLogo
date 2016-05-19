// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

sealed trait ValueSet extends Iterable[Any] {
  def variableName: String
}

case class EnumeratedValueSet(variableName: String,
                         values: List[Any])
  extends ValueSet
{
  def iterator = values.iterator
}

case class SteppedValueSet(variableName: String,
                      firstValue: BigDecimal,
                      step: BigDecimal,
                      lastValue: BigDecimal)
  extends ValueSet
{
  def iterator =
    Iterator.from(0)
      .map(firstValue + step * _)
      .takeWhile(_ <= lastValue)
      .map(i => Double.box(i.toDouble))
}
