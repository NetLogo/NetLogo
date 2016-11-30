// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

sealed trait ConstraintSpecification

object ConstraintSpecification {

  sealed trait NumericConstraintSpecification extends ConstraintSpecification {
    def defaultValue: java.lang.Double
  }

  object NumericConstraintSpecification {
    def apply(defaultValue: java.lang.Double): NumericConstraintSpecification =
      UnboundedNumericConstraintSpecification(defaultValue)
    def unapply(spec: NumericConstraintSpecification): Option[java.lang.Double] =
      Some(spec.defaultValue)
  }

  case class UnboundedNumericConstraintSpecification(defaultValue: java.lang.Double) extends NumericConstraintSpecification

  case class BoundedNumericConstraintSpecification(lowerBound: java.lang.Double, defaultValue: java.lang.Double, upperBound: java.lang.Double, increment: java.lang.Double)
    extends NumericConstraintSpecification

  case class ChoiceConstraintSpecification(vals: List[AnyRef], defaultIndex: Int) extends ConstraintSpecification

  case class BooleanConstraintSpecification(default: java.lang.Boolean) extends ConstraintSpecification

  case class NumericInputConstraintSpecification(typeName: String, default: java.lang.Double) extends ConstraintSpecification

  case class StringInputConstraintSpecification(typeName: String, default: String) extends ConstraintSpecification

}
