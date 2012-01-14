// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.nlogo.api

class OverrideList(agentClass: Class[_ <: api.Agent], varName: String)
extends java.io.Serializable {

  var `type`: AgentType = AgentType.fromAgentClass(agentClass)
  var variable: Int = Overridable.getOverrideIndex(`type`, varName)

  private def readObject(in: java.io.ObjectInputStream) {
    `type` = AgentType.fromInt(in.readInt())
    variable = in.readInt()
  }

  private def writeObject(out: java.io.ObjectOutputStream) {
    out.writeInt(`type`.toInt)
    out.writeInt(variable)
  }

}
