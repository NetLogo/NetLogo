// (c) 2009-2011 Uri Wilensky. See README.txt for terms of use.

package org.nlogo.lab

abstract class ValueSet(val variableName: String)
  extends collection.IterableProxy[Any]

class EnumeratedValueSet(variableName: String,
                         values: List[Any])
  extends ValueSet(variableName)
{
  def self = values
}

class SteppedValueSet(variableName: String,
                      val firstValue: Double,
                      val step: Double,
                      val lastValue: Double)
  extends ValueSet(variableName)
{
  def self = Stream.from(0)
                   .map(firstValue + step * _)
                   .takeWhile(_ <= lastValue)
                   .map(Double.box(_))
}
