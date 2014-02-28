// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

object ValueConstraint {
  class Violation(message: String) extends AgentException(message)
}

/** Interface for objects which provide constraints for values */
trait ValueConstraint {

  /** Throws a Violation condition if the input is not acceptable. */
  @throws(classOf[ValueConstraint.Violation])
  def assertConstraint(value: AnyRef)

  /** Returns the constrained value, which can differ from the input.
    * Throws a Violation condition if the input is not coercable. */
  @throws(classOf[ValueConstraint.Violation])
  def coerceValue(value: AnyRef): AnyRef

  /** Returns the default value for this constraint. */
  def defaultValue: AnyRef
}
