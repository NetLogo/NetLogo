// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import org.nlogo.api

class AgentPerspective private (val agent: Agent, val perspective: Int, val radius: Double, val serverMode: Boolean) {

  def this(agentClass: Class[_ <: api.Agent], id: Long, perspective: Int, radius: Double, serverMode: Boolean) =
    this(new Agent(id, agentClass), perspective, radius, serverMode)

  def this(a: api.Agent, p: api.Perspective, radius: Double, serverMode: Boolean) =
    this(if (a == null)
           new Agent(0, classOf[api.Observer])
         else
           new Agent(a.id, a.getClass),
         p.export, radius, serverMode)

  def this(in: java.io.DataInputStream) =
    this(new Agent(in), in.readInt(), in.readDouble(), in.readBoolean())

  def serialize(out: java.io.DataOutputStream) {
    agent.serialize(out)
    out.writeInt(perspective)
    out.writeDouble(radius)
    out.writeBoolean(serverMode)
  }

  def toByteArray: Array[Byte] = {
    val out = new java.io.ByteArrayOutputStream
    serialize(new java.io.DataOutputStream(out))
    out.toByteArray
  }

  def equals(a: api.Agent, p: api.Perspective) =
    if(a == null)
      agent.tyype == AgentType.Observer && agent.id == 0 && perspective == 0 && radius == -1
    else
      agent.id == a.id &&
      agent.tyype == AgentType.fromAgentClass(a.getClass) &&
      p.export == perspective &&
      radius == -1

}
