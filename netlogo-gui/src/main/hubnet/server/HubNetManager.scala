// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.core.{ AgentKind, Model, Widget => CoreWidget }
import org.nlogo.core.model.WidgetReader
import org.nlogo.api.{ HubNetInterface, ModelLoader, Version }, HubNetInterface.ClientInterface
import org.nlogo.hubnet.mirroring
import org.nlogo.hubnet.mirroring.{ HubNetLinkStamp, HubNetDrawingMessage, HubNetTurtleStamp, HubNetLine }
import org.nlogo.hubnet.connection.{ HubNetException, ConnectionInterface }
import org.nlogo.hubnet.connection.MessageEnvelope._
import org.nlogo.hubnet.connection.MessageEnvelope.MessageEnvelope
import org.nlogo.hubnet.protocol.{ CalculatorInterface, ComputerInterface }
import org.nlogo.workspace.{ AbstractWorkspaceScala, OpenModel }
import org.nlogo.agent.{Link, Turtle}
import org.nlogo.fileformat.ModelConversion
import org.nlogo.util.Utils, Utils.reader2String

import java.nio.file.Paths
import java.net.URI
import java.io.{ Serializable => JSerializable }
import java.util.concurrent.LinkedBlockingQueue

abstract class HubNetManager(workspace: AbstractWorkspaceScala, modelLoader: ModelLoader, modelConverter: ModelConversion)
  extends HubNetInterface
  with ConnectionInterface {

  val connectionManager: ConnectionManager

  val NO_DATA_WAITING =
    "There is no data waiting.  Always check for data using " +
            "HUBNET-MESSAGE-WAITING? before fetching data with " +
            "HUBNET-FETCH-MESSAGE."
  val NO_DATA_FETCHED =
    "The data has not been fetched.  Always fetch the data " +
            "with HUBNET-FETCH-MESSAGE before you try to access the data with " +
            "HUBNET-MESSAGE or HUBNET-MESSAGE-SOURCE or HUBNET-MESSAGE-TAG"

  val NOT_LOGGED_IN = "Not logged in.  Please use the hubnet-reset command."

  protected val messagesList = new LinkedBlockingQueue[MessageEnvelope]()
  private var messageEnvelope: MessageEnvelope = null

  /// messages
  def enqueueMessage(message: MessageEnvelope) { messagesList.put(message) }

  def messageWaiting: Boolean = {
    if (!messagesList.isEmpty) true
    else {
      // if no message is waiting, we don't want the activity's
      // forever button to go around at breakneck speed or it'll
      // chew all the CPU, so let's sleep for a little while
      // - ST 3/29/05
      try messagesList.synchronized{ messagesList.wait( 50 ) }
      catch{ case ex:InterruptedException => org.nlogo.api.Exceptions.ignore( ex ) }
      !messagesList.isEmpty
    }
  }

  def numberOfMessagesWaiting = messagesList.size

  @throws(classOf[HubNetException])
  def enterMessage: Boolean = {
    checkRunningStatus()
    getMessageEnvelope.isEnterMessage
  }

  @throws(classOf[HubNetException])
  def exitMessage: Boolean = {
    checkRunningStatus()
    getMessageEnvelope.isExitMessage
  }

  @throws(classOf[HubNetException])
  def fetchMessage() {
    checkRunningStatus()
    messageEnvelope = null
    if (!messagesList.isEmpty) messageEnvelope = messagesList.take()
    else throw new HubNetException(NO_DATA_WAITING)
  }

  @throws(classOf[HubNetException])
  def getMessage: Object = getMessageEnvelope.getMessage.asInstanceOf[AnyRef]
  @throws(classOf[HubNetException])
  def getMessageSource: String = getMessageEnvelope.getSource
  @throws(classOf[HubNetException])
  def getMessageTag: String = getMessageEnvelope.getTag
  @throws(classOf[HubNetException])
  private def getMessageEnvelope = {
    checkRunningStatus()
    if (messageEnvelope == null) throw new HubNetException(NO_DATA_FETCHED)
    messageEnvelope
  }

  /// sending messages to nodes

  /**
   * sends message to tag on each node in nodes. nodes should be list of
   * string node ids.
   */
  @throws(classOf[HubNetException])
  def send(nodes: Seq[String], tag: String, message: JSerializable with AnyRef) {
    checkRunningStatus()
    for (node <- nodes) if (!send(node, tag, message)) { simulateFailedExitMessage(node) }
  }

  private def simulateFailedExitMessage(clientId: String) {
    enqueueMessage(ExitMessageEnvelope(clientId))
  }

  /**
   * sends a message to a specific node (by String ID).
   */
  @throws(classOf[HubNetException])
  override def send(node: String, tag: String, message: JSerializable with AnyRef): Boolean =
    connectionManager.send(node, tag, message)

  def sendUserMessage(nodes: Seq[String], text: String) {
    for (node <- nodes) if (!connectionManager.sendUserMessage(node, text)) simulateFailedExitMessage(node)
  }

  def broadcastUserMessage(msg: String) {
    if(connectionManager.isRunning) connectionManager.broadcastUserMessage(msg)
  }

  def sendText(nodes: Seq[String], text:String) {
    for (node <- nodes) if (!connectionManager.sendTextMessage(node, text)) simulateFailedExitMessage(node)
  }

  def clearText(nodes: Seq[String]) {
    for (node <- nodes) if (!connectionManager.sendClearTextMessage(node)) simulateFailedExitMessage(node)
  }

  @throws(classOf[HubNetException])
  def broadcast(tag: String, message: Any) {
    if(connectionManager.isRunning) connectionManager.broadcast(tag, message)
  }

  @throws(classOf[HubNetException])
  def broadcast(msg:Any){ if(connectionManager.isRunning) connectionManager.broadcast(msg) }

  def broadcastClearText() { if(connectionManager.isRunning) connectionManager.broadcastClearTextMessage() }

  /**
   * @throws HubNetException exception if the connectionManager is not currently running.
   */
  @throws(classOf[HubNetException])
  private def checkRunningStatus() {
    if(!connectionManager.isRunning) throw new HubNetException(NOT_LOGGED_IN)
  }

  /// clients
  @throws(classOf[HubNetException])
  def setClientInterface(interfaceType:String, interfaceInfo: Iterable[ClientInterface]){
    connectionManager.setClientInterface(interfaceType, interfaceInfo)
    resetPlotManager()
  }

  /// Individualized client views

  def isOverridable(agentKind: AgentKind, varName: String): Boolean =
    mirroring.OverrideList.getOverrideIndex(
      mirroring.Agent.AgentType.fromAgentKind(agentKind),
      varName) != -1

  def sendOverrideList(client: String, agentKind: AgentKind,
                       varName: String, overrides: Map[java.lang.Long, AnyRef]) {
    connectionManager.sendOverrideList(client, agentKind, varName, overrides)
  }
  def clearOverride(client: String, agentKind: AgentKind,
                    varName: String, overrides: Seq[java.lang.Long]) {
    connectionManager.clearOverride(client, agentKind, varName, overrides)
  }
  def clearOverrideLists(client: String) {
    connectionManager.clearOverrideLists(client)
  }
  def sendAgentPerspective(client: String, perspective: Int, agentKind: AgentKind,
                           id: Long, radius: Double, serverMode: Boolean) {
    connectionManager.sendAgentPerspective(client, perspective, agentKind, id, radius, serverMode)
  }

  /// mirror drawing

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
  // (this same comment is in ConnectionManager)
  // JC - 2/26/10
  protected def someNodesHaveView: Boolean = connectionManager.nodesHaveView && HubNetUtils.viewMirroring
  private def broadcastViewMessage(obj: Any) {
    if (connectionManager.isRunning && someNodesHaveView) broadcast("VIEW", obj)
  }

  def sendLine(x0: Double, y0: Double, x1: Double, y1: Double, color: Any, size: Double, mode: String) {
    broadcastViewMessage(new HubNetLine(x0, y0, x1, y1, color, size, mode))
  }

  def sendStamp(agent: org.nlogo.api.Agent, erase: Boolean) {
    agent match {
      case t: Turtle => broadcastViewMessage(new HubNetTurtleStamp(t, erase))
      case l: Link => broadcastViewMessage(new HubNetLinkStamp(l, erase))
      case _ =>
    }
  }

  def sendClear() {broadcastViewMessage(new HubNetDrawingMessage(HubNetDrawingMessage.Type.CLEAR))}

  /// network info
  @throws(classOf[HubNetException])
  def getOutQueueSize: Double = {
    checkRunningStatus()
    val queueSizes = connectionManager.clientSendQueueSizes
    val totalClients = queueSizes.size
    val totalQueueSize = queueSizes.sum
    if (totalClients == 0) 0 else (totalQueueSize / totalClients)
  }

  @throws(classOf[HubNetException])
  def getInQueueSize: Int = {
    checkRunningStatus()
    messagesList.size()
  }

  def clients:Iterable[String] = connectionManager.clients.keys
  def kick(userId:String){ connectionManager.removeClient(userId, true, "Kicked out.") }
  def kickAll(){ connectionManager.removeAllClients() }
  def setViewMirroring(onOff:Boolean){ HubNetUtils.viewMirroring = onOff }
  def setPlotMirroring(onOff:Boolean){ HubNetUtils.plotMirroring = onOff }

  def waitForClients(numClientsToWaitFor:Int, timeoutMillis: Long): (Boolean, Int) = {
    waitForEvents(numClientsToWaitFor, timeoutMillis)(workspace.getHubNetManager.map(_.clients.size).get)
  }

  def waitForMessages(numMessagesToWaitFor:Int, timeoutMillis: Long): (Boolean, Int) = {
    waitForEvents(numMessagesToWaitFor, timeoutMillis)(workspace.getHubNetManager.map(_.getInQueueSize).get)
  }

  // this is called from __hubnet-wait-for-clients and __hubnet-wait-for-messages.
  private def waitForEvents(numEventsToWaitFor:Int, timeoutMillis: Long)
                           (currentNumEvents: => Int): (Boolean, Int) = {
    val start = System.currentTimeMillis
    def timedOut = System.currentTimeMillis - start > timeoutMillis
    // we need to release the world lock here.
    // this is because we hold it, but we need to go to sleep until some events happen
    // (like clients connecting or sending messages)
    // but clients cant fully connect without the world lock!
    // this is because they need to do a fullViewUpdate which requires it.
    // so, we would be waiting for them to connect, while preventing them from doing so!
    // we release it here before sleeping (with wait),
    // allowing clients to get it, we check our condition, and continue (return properly or timeout)
    // JC - 3/31/11
    workspace.world.synchronized{
      while (true) {
        val numNow = currentNumEvents
        if (numNow >= numEventsToWaitFor) return (true, numNow)
        if (timedOut) return (false, numNow)
        workspace.world.wait(25)
      }
    }
    throw new IllegalStateException("unreachable")
  }

  /// plots
  private def plotManager = connectionManager.plotManager
  private def resetPlotManager() {plotManager.initPlotListeners()}
  def addNarrowcastPlot(plotName: String) = plotManager.addNarrowcastPlot(plotName)
  def plot(clientId: String, y: Double) {plotManager.narrowcastPlot(clientId, y)}
  def plot(clientId: String, x: Double, y: Double) {plotManager.narrowcastPlot(clientId, x, y)}
  def clearPlot(clientId: String) {plotManager.narrowcastClear(clientId)}
  def plotPenDown(clientId: String, penDown: Boolean) {plotManager.narrowcastPenDown(clientId, penDown)}
  def setPlotPenMode(clientId: String, plotPenMode: Int) {plotManager.narrowcastPlotPenMode(clientId, plotPenMode)}
  def setHistogramNumBars(clientId: String, num: Int) {plotManager.narrowcastSetHistogramNumBars(clientId, num)}
  def setPlotPenInterval(clientId: String, interval: Double) {plotManager.narrowcastSetInterval(clientId, interval)}

  // gui related

  /**
   * determines whether any connectionManagers have nodes with views.
   * if so, we generally need to do updates.
   * NOTE: this must be a method, rather than a cached field, and it
   * must be freshly determined for each update, since it can change any
   * time during the course of a run (e.g., by the user turning on or off
   * View mirroring).
   */
   def viewIsVisible: Boolean = someNodesHaveView
   private var _framesSkipped = true
   def framesSkipped() {_framesSkipped = true}
   def isDead = false
   def paintImmediately(force: Boolean) { if (force || _framesSkipped) incrementalUpdateFromEventThread() }

   def incrementalUpdateFromEventThread() {
     // HeadlessWorkspace will call this function on a tick or display.
     // this is why we now check isRunning here. this is probably good from GUIWorkspace too.
     // also, for some yet unknown reason, not having the check was causing
     // https://trac.assembla.com/nlogo/ticket/1128, as something was getting into an infinite
     // loop in the mirroring code. since this change makes that problem go away,
     // im not going to dig into that problem. however, its possible that the problem might
     // come back up, which is my i comment it here.
     // - JC 1/7/11
     if (someNodesHaveView && connectionManager.isRunning) {connectionManager.incrementalViewUpdate()}
     _framesSkipped = false
   }
   def repaint() {}

  // since mouseInside is always false all the other values don't matter.
  // all this is silly, but its here because we have to extend ViewInterface.
  // unfortunately theres just no easy way around that right now. JC - 12/28/10
  def mouseInside = false
  def mouseXCor = 0
  def mouseYCor = 0
  def mouseDown = false
  def resetMouseCors{}
  // we could implement these to send messages on these events.
  def shapeChanged(shape:org.nlogo.core.Shape){}
  def applyNewFontSize(fontSize:Int, zoom:Int) {}

  def calculatorInterface(activity: String,tags: Seq[String]): ClientInterface =
    CalculatorInterface(activity, tags)

  def fileInterface(path: String): Option[ClientInterface] = {
    val uri = Paths.get(path).toUri
    OpenModel(uri, HubNetLoadController, modelLoader, modelConverter, Version)
      .flatMap { model =>
        model.optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient")
          .map(widgets => ComputerInterface(widgets, model.turtleShapes, model.linkShapes))
      }
  }

  object HubNetLoadController extends OpenModel.Controller {
    // empty implementations of the following three methods will cause
    // OpenModel to return None, which is fine
    def errorOpeningURI(uri: URI,exception: Exception): Unit = { }
    def invalidModel(uri: URI): Unit = { }
    def invalidModelVersion(uri: java.net.URI,version: String): Unit = { }
    def errorAutoconvertingModel(res: org.nlogo.fileformat.FailedConversionResult): Boolean = true
    def shouldOpenModelOfDifferingArity(arity: Int,version: String): Boolean = true
    def shouldOpenModelOfLegacyVersion(version: String): Boolean = true
    def shouldOpenModelOfUnknownVersion(version: String): Boolean = true
  }
}
