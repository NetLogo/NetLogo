// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait HubNetInterface extends ViewInterface {
  /// getting messages
  def messageWaiting: Boolean
  def numberOfMessagesWaiting: Int
  def enterMessage: Boolean
  def exitMessage: Boolean
  def fetchMessage()
  def getMessage: AnyRef
  def getMessageSource: String
  def getMessageTag: String

  /// sending messages

  /**
   * Send a message to all clients
   */
  def broadcast(variableName: String, data: Any)
  def broadcast(data: Any)
  def sendText(nodes: Seq[String], text: String)
  def clearText(nodes: Seq[String])
  def broadcastClearText()
  def sendUserMessage(nodes: Seq[String], text: String)

  def broadcastUserMessage(text: String)

  /**
   * Send a message to each node (client) in the list for the given tag
   */
  def send(nodes: Seq[String], tag: String, message: Any)

  /**
   * Send message to a single client for the given tag
   */
  def send(node: String, tag: String, message: Any): Boolean

  /// connection management
  def disconnect()
  def reset()
  // list of all clients connected to the server (their names)
  def clients:Iterable[String]
  // wait until n clients are connected to hubnet.
  def waitForClients(numClientsToWaitFor:Int, timeoutMillis: Long): (Boolean, Int)
  // wait until n messages are queued up on the server.
  def waitForMessages(numMessagesToWaitFor:Int, timeoutMillis: Long): (Boolean, Int)
  def kick(clientName:String)
  def kickAll()
  def setViewMirroring(on:Boolean)

  /// clients
  def setClientInterface(clientType: String, interfaceInfo:Iterable[AnyRef])
  def newClient(isRobo: Boolean, waitTime: Int)
  def sendFromLocalClient(clientName:String, tag: String, content: AnyRef): Option[String]
  def isOverridable(agentType: Class[_ <: org.nlogo.api.Agent], varName: String): Boolean
  def sendOverrideList(client: String, agentType: Class[_ <: org.nlogo.api.Agent],
                       varName: String, overrides: Map[java.lang.Long, AnyRef])
  def clearOverride(client: String, agentType: Class[_ <: org.nlogo.api.Agent],
                    varName: String, overrides: Seq[java.lang.Long])
  def clearOverrideLists(client: String)
  def sendAgentPerspective(client: String, perspective: Int, agentType: Class[_ <: org.nlogo.api.Agent],
                           id: Long, radius: Double, serverMode: Boolean)
  /// view updates
  def incrementalUpdateFromEventThread()

  /// mirror drawing
  def sendLine(x0: Double, y0: Double, x1: Double, y1: Double,
               color: Any, size: Double, mode: String)

  def sendStamp(agent: Agent, erase: Boolean)
  def sendClear()

  /// control center
  def showControlCenter()

  /// network info
  def getOutQueueSize: Double
  def getInQueueSize: Int

  /// client editor
  def save(buf: StringBuilder)
  def closeClientEditor()
  def openClientEditor()
  def clientEditor: AnyRef
  def load(lines: Array[String], version: String)
  @throws(classOf[java.io.IOException])
  def importClientInterface(path: String, client: Boolean)
  def setTitle(title: String, directory: String, modelType: ModelType)
  def getInterfaceWidth: Int
  def getInterfaceHeight: Int

}
