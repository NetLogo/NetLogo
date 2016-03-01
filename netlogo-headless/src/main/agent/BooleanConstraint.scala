// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.ValueConstraint
import org.nlogo.core.I18N

/**
 * Constraint suitable for Switch variables.
 */
class BooleanConstraint(var defaultValue: java.lang.Boolean) extends ValueConstraint {

  def this() = this(java.lang.Boolean.FALSE)

  @throws(classOf[ValueConstraint.Violation])
  def assertConstraint(value: AnyRef) {
    if(!value.isInstanceOf[java.lang.Boolean])
      throw new ValueConstraint.Violation(I18N.errors.get("org.nlogo.agent.BooleanConstraint.bool"))
  }

  def coerceValue(value: AnyRef): java.lang.Boolean =
    value match {
      case b: java.lang.Boolean => b
      case s: String => java.lang.Boolean.valueOf(s)  // ugh, is this ever used? - ST 5/6/13
      case _ => defaultValue
    }

}
