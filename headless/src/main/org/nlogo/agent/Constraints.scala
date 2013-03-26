// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api, api.ValueConstraint

trait Constraints extends Agent {
  private var constraints: Array[ValueConstraint] = null
  def clearConstraints() {
    constraints = new Array[ValueConstraint](
      world.getVariablesArraySize(null: api.Observer)) // yuck - ST 3/27/13
  }
  def constraint(vn: Int) = constraints(vn)
  def setConstraint(vn: Int, con: ValueConstraint) {
    constraints(vn) = con
  }
  @throws(classOf[api.AgentException])
  def assertConstraint(vn: Int, value: AnyRef) {
    val con = constraint(vn)
    if (con != null)
      con.assertConstraint(value)
  }
}
