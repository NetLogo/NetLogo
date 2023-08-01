// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

sealed trait BaseValueSet {
  val length: Int
  def variableName: String
}

@deprecated("6.0.2", "use RefValueSet instead")
sealed trait ValueSet extends Iterable[Any] with BaseValueSet

sealed trait RefValueSet extends Iterable[AnyRef] with BaseValueSet

@deprecated("6.0.2", "use RefEnumeratedValueSet instead")
case class EnumeratedValueSet(variableName: String,
                         values: List[Any])
  extends ValueSet
{
  val length = values.length
  def iterator = values.iterator
}

case class RefEnumeratedValueSet(variableName: String, values: List[AnyRef])
  extends RefValueSet {
    val length = values.length
    def iterator = values.iterator
  }


case class SteppedValueSet(variableName: String,
                      firstValue: BigDecimal,
                      step: BigDecimal,
                      lastValue: BigDecimal)
  extends RefValueSet
{
  val length = (((lastValue - firstValue) / step) + BigDecimal(1)).toInt
  def iterator =
    Iterator.from(0)
      .map(firstValue + step * _)
      .takeWhile(_ <= lastValue)
      .map(i => Double.box(i.toDouble))
}

case class TupleSet(values: List[(String, AnyRef)])

sealed trait ParameterList

case class ValueList(list: List[RefValueSet]) extends ParameterList
case class TupleList(list: List[TupleSet]) extends ParameterList
