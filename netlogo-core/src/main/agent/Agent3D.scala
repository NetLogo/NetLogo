// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.api.AgentException

trait Agent3D {
  @throws(classOf[AgentException])
  def getPatchAtOffsets(dx: Double, dy: Double, dz: Double): Patch3D
}
