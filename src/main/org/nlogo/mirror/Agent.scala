// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import org.nlogo.api
import java.io.{ DataInputStream, DataOutputStream, ByteArrayOutputStream }

class Agent private (val id: Long, val tyype: AgentType) extends Serializable {

  def this(id: Long, clazz: Class[_ <: api.Agent]) =
    this(id, AgentType.fromAgentClass(clazz))

  def this(is: DataInputStream) =
    this(is.readLong(), AgentType.fromInt(is.readInt()))

  def serialize(out: java.io.DataOutputStream) {
    out.writeLong(id)
    out.writeInt(tyype.toInt)
  }

  def toByteArray= {
    val bos = new java.io.ByteArrayOutputStream
    serialize(new DataOutputStream(bos))
    bos.toByteArray
  }

}
