// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import java.io.{Serializable, InterruptedIOException, IOException}
import org.nlogo.workspace.AbstractWorkspaceScala
import org.nlogo.hubnet.connection.MessageEnvelope.MessageEnvelope
import org.nlogo.plot.Plot
import org.nlogo.hubnet.protocol._
import org.nlogo.hubnet.mirroring.{AgentPerspective, ClearOverride, SendOverride, ServerWorld}
import org.nlogo.agent.AgentSet
import java.net.{BindException, ServerSocket}
import org.nlogo.api.{WorldPropertiesInterface, ModelReader, PlotInterface}
import org.nlogo.hubnet.connection.{Streamable, ConnectionTypes, Ports, HubNetException, ConnectionInterface}
import collection.JavaConverters._

// Connection Manager calls back to this when these events happen.
// HeadlessHNM uses it to simply print events.
// GUIHNM uses it to update the gui.
trait ClientEventListener {
  def addClient(clientId: String, remoteAddress: String)
  def clientDisconnect(clientId: String)
  def logMessage(message:String)
}

// ConnectionManager implements this so that we can pass this to ServerSideConnection
// so that ServerSideConnection isn't directly tied to ConnectionManager.
// this makes ServerSideConnection far easier to test.
trait ConnectionManagerInterface {
  def isSupportedClientType(clientType: String): Boolean
  def finalizeConnection(c: ServerSideConnection, desiredClientId: String): Boolean
  def createHandshakeMessage(clientType: String): HandshakeFromServer
  def fullViewUpdate()
  def putClientData(messageEnvelope: MessageEnvelope)
  def removeClient(userid: String, notifyClient: Boolean, reason: String): Boolean
  def logMessage(message:String)
  def sendPlots(clientId:String)
}

class ConnectionManager(val connection: ConnectionInterface,
                        val clientEventListener: ClientEventListener,
                        workspace: AbstractWorkspaceScala) extends ConnectionManagerInterface with Runnable {
  val VALID_SEND_TYPES_MESSAGE =
    "You can only send strings, booleans (true or false), numbers, and lists of these types."

  private val world = workspace.world
  private var worldBuffer = new ServerWorld(worldProps)
  private def worldProps =
    if(workspace.getPropertiesInterface != null) workspace.getPropertiesInterface
    else new WorldPropertiesInterface { def fontSize = 10 } // TODO BAD HACK! JC 12/28/10

  // instantiated in startup
  var nodeThread: Thread = null
  private var announcer: DiscoveryAnnouncer = null
  @volatile private var socket: ServerSocket = null
  private var _port = -1
  def port = _port
  private def port_=(p:Int){ _port = p }
  // the run method polls this to know when to stop.
  // set to true in startup, false in shutdown.
  @volatile private var serverOn: Boolean = false

  protected var running = false
  val clients = collection.mutable.HashMap[String, ServerSideConnection]()
  val plotManager = new ServerPlotManager(workspace, this,
    // these two arguments are by name params,
    // as they need be evaluated each time.
    // i wanted to avoid giving the entire plot manager to ServerPlotManager
    // JC - 12/20/10
    workspace.plotManager.plots, workspace.plotManager.currentPlot.get) {
    workspace.plotManager.subscribe(this)
  }

  private type ClientType = String
  private val clientInterfaceMap = collection.mutable.HashMap[ClientType, Iterable[AnyRef]]()
  private def clientInterfaceSpec: ClientInterface = {
    clientInterfaceMap(ConnectionTypes.COMP_CONNECTION).head.asInstanceOf[ClientInterface]
  }
  // this business needs to get cleaned up
  // for different client types. im leaving this hack in for now,
  // but i expect it to come out when the hubnet-teacher-client branch gets merged.
  // someNodesHaveView currently only takes into account the regular clients.
  // but, its possible that its different for the teacher client or the android client.
  // without the connectionManager.nodesHaveView check, broadcastViewMessage fails
  // in the Function model when you click setup. this is because isValidTag is called
  // from broadcast, and the clients don't have a VIEW.
  // I believe this would be best cleaned up if clients could register for
  // the message types they are interestd in, and also what tags are valid.
  // JC - 2/26/10
  def nodesHaveView = clientInterfaceMap.nonEmpty && clientInterfaceSpec.containsViewWidget
  def isRunning = running

  /**
   * NetLogo calls this method when starting the ConnectionManager.
   * Creates a new Thread for <code>nodeThread</code> and starts it.
   * This is called when NetLogo executes the <code>hubnet-reset</code> primitive.
   * @return true if startup was succesful
   */
  def startup(serverName:String): Boolean = {
    workspace.hubNetRunning(true)
    running = true
    // we set this when hubnet-reset is called now, instead
    // of forcing users to call hubnet-set-client-interface "COMPUTER" []
    clientInterfaceMap(ConnectionTypes.COMP_CONNECTION) = List(createClientInterfaceSpec)

    // try every port from DEFAULT_PORT_NUMBER to MAX_PORT_NUMBER until
    // we find one that works
    def createSocket(portToTry: Int): (Int, ServerSocket) = {
      if (portToTry > Ports.MAX_PORT_NUMBER) throw new BindException("port: " + portToTry)
      else
        try { (portToTry, new ServerSocket(portToTry) { setSoTimeout(250) }) }
        catch {case bex: BindException => createSocket(portToTry + 1) }
    }
    try {
      val (port, socket) = createSocket(Ports.DEFAULT_PORT_NUMBER)
      this.port = port
      this.socket = socket
      serverOn = true

      announcer = new DiscoveryAnnouncer(serverName, workspace.modelNameForDisplay, port)
      announcer.start()

      nodeThread = new Thread(this) {setName("org.nlogo.hubnet.server.ConnectionManager")}
      nodeThread.start()
      true
    }
    catch {
      case ex: BindException =>
        val message = "Could not start the HubNet server. No ports are available."
        org.nlogo.util.Exceptions.handle(new Exception(message, ex))
      false
    }
  }

  /**
   * @return true if the AbtractConnectionManager shuts down succesfully
   */
  def shutdown(): Boolean = {
    // in headless, its possible that the connection manager was never started
    // yet it will always try to shut it down when disposed. JC - 12/18/10
    if (nodeThread != null && nodeThread.isAlive) {
      serverOn = false
      while (socket != null) {
        try Thread.sleep(50)
        catch {
          // we don't care if we are Interrupted
          case ie: InterruptedException => org.nlogo.util.Exceptions.ignore(ie)
        }
      }
      clients.synchronized {
        for (conn <- clients.values) {disconnectClient(conn, true, "Shutting Down.")}
        clients.clear()
      }
    }
    workspace.hubNetRunning(false)
    running = false
    true // why do we need this? we never return false...
  }

  /**
   * Places a <code>MessageEnvelope</code> received from a client on the queue to
   * be accessed by the NetLogo code when <code>hubnet-fetch-message</code>
   * and <code>hubnet-message-waiting?</code> called.
   */
  def enqueueMessage(message:MessageEnvelope) { connection.enqueueMessage(message) }

  def run() {
    try {
      while (serverOn) {
        try waitForConnection()
        catch {
          // accept timed out
          case e: InterruptedIOException => // do nothing.
          case e: IOException => org.nlogo.util.Exceptions.handle(e)
          case e: RuntimeException => org.nlogo.util.Exceptions.handle(e)
        }
      }
      announcer.shutdown()
      socket.close()
      this.socket = null
    }
    catch {
      case e: IOException => org.nlogo.util.Exceptions.handle(e)
    }
  }

  @throws(classOf[IOException])
  private def waitForConnection(): Unit = {
    val newSocket = socket.accept()
    newSocket.setSoTimeout(0)
    /**
     * do not uncomment the following line to enable tcp_no_delay.
     * in theory, it should get our messages out to clients slightly faster.
     * in practice, it was creating hundreds of extra tiny packets, consuming bandwidth.
     * this caused clients to skip and pause erratically and rendered them unusable.
     * this typically went unnoticed in small simulations, or simulations sending
     * only a few messages. --josh 11/19/09
     */
    // newSocket.setTcpNoDelay( true )
    new ServerSideConnection(Streamable(newSocket), newSocket.getRemoteSocketAddress.toString, this).start()
  }

  /// client code

  /**
   * Completes the login process for a client. Called by ServerSideConnection.
   * @return true if the client was succesfully logged on,
   *         false if the desired client id is already taken.
   */
  def finalizeConnection(c: ServerSideConnection, desiredClientId: String): Boolean = {
    clients.synchronized {
      if (clients.contains(desiredClientId)) false
      else {
        clients.put(desiredClientId, c)
        clientEventListener.addClient(desiredClientId, c.remoteAddress)
        true
      }
    }
  }

  /// client Interface code
  def reloadClientInterface() {
    setClientInterface(ConnectionTypes.COMP_CONNECTION, List())
  }

  def setClientInterface(interfaceType: ClientType, interfaceInfo: Iterable[AnyRef]) {
    // we set this when hubnet-reset is called now, instead
    // of forcing users to call hubnet-set-client-interface "COMPUTER" []
    // however, if they still want to call it, we should just update it here anyway.
    // its usually assumed that a call to hubnet-reset will happen right after this call
    // but, it doesn't hurt to keep this here. JC 12/28/10
    clientInterfaceMap(interfaceType) =
      if(interfaceType == ConnectionTypes.COMP_CONNECTION)
        List(createClientInterfaceSpec)
      else
        interfaceInfo
  }

  private def createClientInterfaceSpec: ClientInterface = {
    val widgetDescriptions = connection.getClientInterface
    val widgets = ModelReader.parseWidgets(widgetDescriptions)
    val clientInterfaceSpec = new ClientInterface(widgets, widgetDescriptions.toList,
      world.turtleShapeList.getShapes.asScala,
      world.linkShapeList.getShapes.asScala, workspace)
    clientInterfaceSpec
  }

  /**
   * Enqueues a message from the client to the manager.
   */
  def putClientData(messageEnvelope:MessageEnvelope) { enqueueMessage(messageEnvelope) }

  /**
   * Returns a handshake message containing the current Interface specification.
   * Called by ServerSideConnection.
   */
  def createHandshakeMessage(clientType:ClientType) = {
    new HandshakeFromServer(workspace.modelNameForDisplay, clientInterfaceMap(clientType))
  }

  def isSupportedClientType(clientType:String): Boolean =
    clientInterfaceMap.isDefinedAt(clientType)

  def isValidTag(tag:String) = clientInterfaceSpec.containsWidget(tag)

  @throws(classOf[HubNetException])
  def broadcast(tag:String, message:Any) {
    if (!isValidTag(tag)) throw new HubNetException(tag + " is not a valid tag on the client.")
    if (!(message.isInstanceOf[Serializable])) throw new HubNetException(VALID_SEND_TYPES_MESSAGE)
    broadcastMessage(new WidgetControl(message.asInstanceOf[AnyRef with Serializable], tag))
  }

  /**
   * Sends a message to a client.
   * Specified by AbstractConnectionManager.
   * @return true if the message was sent
   */
  @throws(classOf[HubNetException])
  def send (userId:String, tag:String, message:Any) = {
    if (!isValidTag(tag)) throw new HubNetException(tag + " is not a valid tag on the client.")
    sendUserMessage(userId, new WidgetControl(message.asInstanceOf[AnyRef with Serializable], tag))
  }

  @throws(classOf[HubNetException])
  def broadcast(obj: Any) {
    if (obj.isInstanceOf[String]) broadcastMessage(new Text(obj.toString, Text.MessageType.TEXT))
    else if (obj.isInstanceOf[Plot]) broadcastMessage(new PlotUpdate(obj.asInstanceOf[Plot]))
    else throw new HubNetException(VALID_SEND_TYPES_MESSAGE)
  }

  @throws(classOf[HubNetException])
  def broadcastPlotControl(a:Any, plotName:String){
    broadcastMessage(new PlotControl(a.asInstanceOf[AnyRef], plotName))
  }

  @throws(classOf[HubNetException])
  def sendPlotControl(userId: String, a:Any, plotName:String){
    sendUserMessage(userId, new PlotControl(a.asInstanceOf[AnyRef], plotName))
  }

  def broadcastClearTextMessage() { broadcastMessage(new Text(null, Text.MessageType.CLEAR)) }

  /**
   * Broadcasts a message to all clients.
   */
  private def broadcastMessage(msg:Message) {
    clients.synchronized { for (connection <- clients.values) { connection.sendData(msg) } }
  }

  def sendTextMessage(node: String, text: String): Boolean =
    sendUserMessage(node, new Text(text, Text.MessageType.TEXT))
  def sendClearTextMessage (node:String): Boolean =
    sendUserMessage(node, new Text(null, Text.MessageType.CLEAR))
  def sendUserMessage(node: String, text: String): Boolean =
    sendUserMessage(node, new Text(text, Text.MessageType.USER))

  private def sendUserMessage(userid:String, message:Message): Boolean = {
    val c = clients.get(userid)
    c.foreach(_.sendData(message))
    c.isDefined
  }

  def broadcastUserMessage(text:String) { broadcastMessage(new Text(text, Text.MessageType.USER)) }

  // called from control center
  def removeAllClients() {
    clients.synchronized {
      for(conn <- clients.values){
        disconnectClient(conn, true, "Kicked from Control Center.")
      }
      clients.clear()
    }
  }

  /**
   * Removes a client. Deletes the client from the client map and disconnects it.
   */
  def removeClient(userid: String, notifyClient: Boolean, reason:String): Boolean = {
    // only synchronize when we are removing from clients since we
    // could get stuck for a long time disconnecting -- mag 12/4/02
    val c = clients.synchronized { clients.remove(userid) }
    c match {
      case Some(client) =>
        disconnectClient(client, notifyClient, reason);
        true
      case _ => false // false means there was no client to disconnect
    }
  }

  /**
   * Disconnects the client and removes it from the control center.
   */
  private def disconnectClient(c:ServerSideConnection, notifyClient:Boolean, reason:String) {
    if (c != null) {
      c.disconnect(notifyClient, reason)
      clientEventListener.clientDisconnect(c.clientId)
    }
  }

  /// view stuff
  def sendOverrideList (client:String, agentClass: Class[_ <: org.nlogo.api.Agent],
                                 varName: String, overrides:Map[java.lang.Long, AnyRef]) = {
    sendUserMessage(client, new OverrideMessage(new SendOverride(agentClass, varName, overrides), false))
  }

  def clearOverride (client:String, agentClass: Class[_ <: org.nlogo.api.Agent],
                              varName:String, overrides:Seq[java.lang.Long]) = {
    sendUserMessage(client, new OverrideMessage(new ClearOverride(agentClass, varName, overrides), true))
  }

  def clearOverrideLists(client:String) { sendUserMessage(client, ClearOverrideMessage) }

  def sendAgentPerspective(client:String, perspective:Int, agentClass: Class[_ <: org.nlogo.api.Agent],
                                    id: Long, radius: Double, serverMode: Boolean) {
    sendUserMessage(client, new AgentPerspectiveMessage(
      new AgentPerspective(agentClass, id, perspective, radius, serverMode).toByteArray))
  }

  private var lastPatches: AgentSet = null

  def fullViewUpdate() {
    doViewUpdate(true) /* reset the world before sending the update */
  }

  def incrementalViewUpdate() {
    // update the entire world if the patches have changed (do to a world resizing)
    doViewUpdate(lastPatches != world.patches())
  }

  private def doViewUpdate(resetWorld:Boolean) {
    if (resetWorld) {
      // create a new world buffer, which will force a full update.
      worldBuffer = new ServerWorld(worldProps)
      lastPatches = world.patches()
    }
    val buf = worldBuffer.updateWorld(world, resetWorld)
    if (!buf.isEmpty) broadcastMessage(new ViewUpdate(buf.toByteArray))
  }

  def setViewEnabled(mirror:Boolean) {
    if (mirror) incrementalViewUpdate() else broadcastMessage(DisableView)
  }

  def sendPlot(clientId:String, plot:PlotInterface) {
    val c = clients.get(clientId)
    if (c.isDefined) c.get.sendData(new PlotUpdate(plot))
  }

  def sendPlots(clientId:String){ plotManager.sendPlots(clientId) }

  def clientSendQueueSizes: Iterable[Int] = clients.synchronized{ clients.values.map(_.getSendQueueSize)}

  def logMessage(message:String) = clientEventListener.logMessage(message)
}
