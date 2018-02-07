// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.core.AgentKind
import org.nlogo.hubnet.connection.{ConnectionInterface, HubNetException}
import collection.mutable.ListBuffer
import org.nlogo.api.{ HubNetInterface, PlotInterface }, HubNetInterface.ClientInterface
import org.nlogo.workspace.AbstractWorkspace

import java.net.{ InetAddress, NetworkInterface }
import java.io.{ Serializable => JSerializable }

class MockControlCenter extends ClientEventListener() {
  def addClient(clientId: String, remoteAddress: String) {  }
  def clientDisconnect(clientId: String){  }
  def logMessage(message: String){ }
}

class MockConnectionManager(connection: ConnectionInterface, workspace: AbstractWorkspace)
        extends ConnectionManager(connection, new MockControlCenter, workspace) {
  private val results = new ListBuffer[String]()
  var _nodesHaveView = false
  override def nodesHaveView = _nodesHaveView
  var validTag = false
  override def incrementalViewUpdate() {results+="UPDATE"}
  override def sendOverrideList(client: String, agentClass: AgentKind, varName: String,
                                overrides: Map[java.lang.Long, AnyRef]) = true
  override def clearOverride(client: String, agentClass: AgentKind, varName: String,
                    overrides: Seq[java.lang.Long]) = true
  override def clearOverrideLists(client:String){}
  override def sendAgentPerspective(client: String, perspective:Int, agentClass: AgentKind,
   id:Long, radius:Double, serverMode:Boolean){}
  override def run(){}
  override def isValidTag(tag:String) = validTag
  override def clientSendQueueSizes = null
  @throws(classOf[HubNetException])
  override def send(node:String, tag:String, message:JSerializable with AnyRef) =
    if (!validTag) throw new HubNetException(tag + " is an invalid tag") else true
  override def setClientInterface(interfaceType:String, interfaceInfo: Iterable[ClientInterface]){}
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
  override def startup(serverName: String, selectedNetwork: (NetworkInterface, InetAddress)): Boolean = {
    running = true
    workspace.hubNetRunning = true
    true
  }
}
