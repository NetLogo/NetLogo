// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.nlogo.core.AgentKind
import java.io.{ Serializable => JSerializable }

@SerialVersionUID(0)
class SendOverride(agentKind: AgentKind, varName:String, var overrides:Map[java.lang.Long, AnyRef])
        extends OverrideList(agentKind, varName) with JSerializable {

  @throws(classOf[java.io.IOException])
  private def writeObject(out:java.io.ObjectOutputStream){
    out.writeInt( overrides.size )
    for((id,a)<-overrides) { out.writeLong(id); out.writeObject(a) }
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in:java.io.ObjectInputStream){
    val numOverrides = in.readInt()
    overrides = (for(i <- 0 until numOverrides)
                 yield (Long.box(in.readLong()), in.readObject()))
                .toMap
  }
}
