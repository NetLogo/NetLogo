package org.nlogo.hubnet.mirroring 

@serializable @SerialVersionUID(0)
@throws(classOf[org.nlogo.api.LogoException])
class ClearOverride(agentClass: Class[_ <: org.nlogo.api.Agent], varName:String, var agents: Seq[Long])
        extends OverrideList(agentClass, varName) {

  @throws(classOf[java.io.IOException])
  private def writeObject(out:java.io.ObjectOutputStream){
    out.writeInt( agents.size )
    agents.foreach(out.writeLong)
  }

  @throws(classOf[java.io.IOException])
  @throws(classOf[ClassNotFoundException])
  private def readObject(in:java.io.ObjectInputStream){
    val numOverrides = in.readInt()
    agents = (for(i <- 0 until numOverrides)
              yield in.readLong())
             .toSeq
  }
}
