// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.hubnet.connection.{ConnectionInterface, HubNetException}
import collection.mutable.ListBuffer
import org.nlogo.api.PlotInterface
import org.nlogo.workspace.AbstractWorkspaceScala

import java.io.{ Serializable => JSerializable }

class MockControlCenter extends ClientEventListener() {
  def addClient(clientId: String, remoteAddress: String) {  }
  def clientDisconnect(clientId: String){  }
  def logMessage(message: String){ }
}

class MockConnectionManager(connection: ConnectionInterface, workspace: AbstractWorkspaceScala)
        extends ConnectionManager(connection, new MockControlCenter, workspace) {
  private val results = new ListBuffer[String]()
  var _nodesHaveView = false
  override def nodesHaveView = _nodesHaveView
  var validTag = false
  override def incrementalViewUpdate() {results+="UPDATE"}
  override def sendOverrideList(client: String, agentClass: Class[_ <: org.nlogo.api.Agent], varName: String,
                                overrides: Map[java.lang.Long, AnyRef]) = true
  override def clearOverride(client: String, agentClass: Class[_ <: org.nlogo.api.Agent], varName: String,
                    overrides: Seq[java.lang.Long]) = true
  override def clearOverrideLists(client:String){}
  override def sendAgentPerspective(client: String, perspective:Int, agentClass: Class[_ <: org.nlogo.api.Agent],
   id:Long, radius:Double, serverMode:Boolean){}
  override def run(){}
  override def isValidTag(tag:String) = validTag
  override def clientSendQueueSizes = null
  @throws(classOf[HubNetException])
  override def send(node:String, tag:String, message:JSerializable) =
    if (!validTag) throw new HubNetException(tag + " is an invalid tag") else true
  override def setClientInterface(interfaceType:String, interfaceInfo: Iterable[AnyRef]){}
  override def sendPlot(clientId:String, plot:PlotInterface){}
  override def sendTextMessage(node:String, text:String) = true
  override def sendClearTextMessage(node:String) = true
  override def broadcastClearTextMessage() {}
  override def sendUserMessage(node:String,text:String) = true
  override def broadcastUserMessage(text:String){}
  override def broadcast(msg:Any){}
  override def broadcastPlotControl(a:Any, plotName:String){}
  override def broadcast(tag: String, message: Any) {
    if (!validTag) throw new HubNetException(tag + " is an invalid tag")
  }
  def getResults = results.mkString("[", " ", "]")

  // have to override this so threads and things arent started up.
  // JC - 12/28/10
  override def startup(serverName:String): Boolean = {
    running = true
    workspace.hubNetRunning(true)
    true
  }
}
