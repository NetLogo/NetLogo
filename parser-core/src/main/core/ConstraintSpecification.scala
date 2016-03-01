// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

sealed trait ConstraintSpecification

object ConstraintSpecification {

  case class NumericConstraintSpecification(defaultValue: java.lang.Double) extends ConstraintSpecification

  case class ChoiceConstraintSpecification(vals: List[AnyRef], defaultIndex: Int) extends ConstraintSpecification

  case class BooleanConstraintSpecification(default: java.lang.Boolean) extends ConstraintSpecification

  case class NumericInputConstraintSpecification(typeName: String, default: java.lang.Double) extends ConstraintSpecification

  case class StringInputConstraintSpecification(typeName: String, default: String) extends ConstraintSpecification

}
