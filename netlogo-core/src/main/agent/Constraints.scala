// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api, api.ValueConstraint

trait Constraints extends Agent {
  private var constraints: Array[ValueConstraint] = null
  def clearConstraints(): Unit = {
    constraints = new Array[ValueConstraint](
      world.program.globals.size)
  }
  def constraint(vn: Int) = constraints(vn)
  def setConstraint(vn: Int, con: ValueConstraint): Unit = {
    constraints(vn) = con
  }
  @throws(classOf[api.AgentException])
  def assertConstraint(vn: Int, value: AnyRef): Unit = {
    val con = constraint(vn)
    if (con != null)
      con.assertConstraint(value)
  }
}
