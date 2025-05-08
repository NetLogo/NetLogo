// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.core.AgentKind
import org.nlogo.hubnet.connection.{ConnectionInterface, HubNetException}
import collection.mutable.ListBuffer
import org.nlogo.api.{ HubNetInterface, PlotInterface }, HubNetInterface.ClientInterface
import org.nlogo.workspace.AbstractWorkspaceScala

import java.net.{ InetAddress, NetworkInterface }
import java.io.{ Serializable => JSerializable }

class MockControlCenter extends ClientEventListener() {
  def addClient(clientId: String, remoteAddress: String): Unit = {  }
  def clientDisconnect(clientId: String): Unit ={  }
  def logMessage(message: String): Unit ={ }
}

class MockConnectionManager(connection: ConnectionInterface, workspace: AbstractWorkspaceScala)
        extends ConnectionManager(connection, new MockControlCenter, workspace) {
  private val results = new ListBuffer[String]()
  var _nodesHaveView = false
  override def nodesHaveView = _nodesHaveView
  var validTag = false
  override def incrementalViewUpdate(): Unit = {results+="UPDATE"}
  override def sendOverrideList(client: String, agentClass: AgentKind, varName: String,
                                overrides: Map[java.lang.Long, AnyRef]) = true
  override def clearOverride(client: String, agentClass: AgentKind, varName: String,
                    overrides: Seq[java.lang.Long]) = true
  override def clearOverrideLists(client:String): Unit ={}
  override def sendAgentPerspective(client: String, perspective:Int, agentClass: AgentKind,
   id:Long, radius:Double, serverMode:Boolean): Unit ={}
  override def run(): Unit ={}
  override def isValidTag(tag:String) = validTag
  override def clientSendQueueSizes = null
  @throws(classOf[HubNetException])
  override def send(node:String, tag:String, message:JSerializable & AnyRef) =
    if (!validTag) throw new HubNetException(tag + " is an invalid tag") else true
  override def setClientInterface(interfaceType:String, interfaceInfo: Iterable[ClientInterface]): Unit ={}
  override def sendPlot(clientId:String, plot:PlotInterface): Unit ={}
  override def sendTextMessage(node:String, text:String) = true
  override def sendClearTextMessage(node:String) = true
  override def broadcastClearTextMessage(): Unit = {}
  override def sendUserMessage(node:String,text:String) = true
  override def broadcastUserMessage(text:String): Unit ={}
  override def broadcast(msg:Any): Unit ={}
  override def broadcastPlotControl(a:Any, plotName:String): Unit ={}
  override def broadcast(tag: String, message: Any): Unit = {
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
