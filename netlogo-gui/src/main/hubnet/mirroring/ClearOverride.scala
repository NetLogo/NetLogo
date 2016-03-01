// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.mirroring

import org.nlogo.core.AgentKind
import java.io.{ Serializable => JSerializable }

@SerialVersionUID(0)
class ClearOverride(agentKind: AgentKind, varName:String, var agents: Seq[java.lang.Long])
        extends OverrideList(agentKind, varName) with JSerializable {

  @throws(classOf[java.io.IOException])
  private def writeObject(out:java.io.ObjectOutputStream){
    out.writeInt( agents.size )
    agents.foreach(a => out.writeLong(a.longValue))
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in:java.io.ObjectInputStream){
    val numOverrides = in.readInt()
    agents = (for(i <- 0 until numOverrides)
              yield Long.box(in.readLong()))
             .toSeq
  }
}
