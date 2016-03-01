// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api, api.ValueConstraint

trait Constraints extends Agent {
  private var constraints: Array[ValueConstraint] = null
  def clearConstraints() {
    constraints = new Array[ValueConstraint](
      world.program.globals.size)
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
