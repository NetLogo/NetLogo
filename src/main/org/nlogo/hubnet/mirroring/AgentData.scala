// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import java.io.DataOutputStream

abstract class AgentData extends Overridable {
  def xcor: Double
  def ycor: Double
  def spotlightSize: Double
  def wrapSpotlight: Boolean
  @throws(classOf[java.io.IOException])
  def serialize(out: DataOutputStream)
}
