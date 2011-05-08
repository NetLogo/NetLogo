package org.nlogo.hubnet.mirroring

@serializable @SerialVersionUID(0)
@throws(classOf[org.nlogo.api.LogoException])
class SendOverride(agentClass: Class[_ <: org.nlogo.api.Agent], varName:String, var overrides:Map[Long,Any])
        extends OverrideList(agentClass, varName){

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
                 yield (in.readLong(), in.readObject()))
                .toMap
  }
}
