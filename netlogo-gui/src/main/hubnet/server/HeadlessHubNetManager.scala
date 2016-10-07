// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.hubnet.connection.HubNetException
import org.nlogo.workspace.AbstractWorkspaceScala
import org.nlogo.core.{ Model, Widget => CoreWidget }
import org.nlogo.core.model.WidgetReader
import org.nlogo.api.{ ModelType, ModelLoader }
import org.nlogo.hubnet.protocol.{ ComputerInterface, TestClient }
import org.nlogo.fileformat.ModelConversion
import collection.mutable.ListBuffer
import java.util.concurrent.{Executors, ExecutorService}

// TODO: we really need to do something about the printlns in this class.
// but what?
class HeadlessHubNetManager(workspace: AbstractWorkspaceScala, loader: ModelLoader, modelConverter: ModelConversion)
  extends HubNetManager(workspace, loader, modelConverter) {
  // since the server is headless, the clients cant be, or no one would have a view.
  // so, set this to true by default. JC 12/28/10
  HubNetUtils.viewMirroring = true

  // there is no client editor, but someone could have created a hubnet model in the gui
  // and is now running it in headless.
  // load is called from HeadlessModelOpener
  // save should never be called.
  var widgets: Seq[CoreWidget] = Seq()
  def load(m: Model) {
    m.optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient").foreach { ws =>
      widgets = ws
    }
  }
  override def updateModel(m: Model): Model = {
    m.withOptionalSection("org.nlogo.modelsection.hubnetclient", Some(widgets), Seq())
  }
  override def modelWidgets: Seq[CoreWidget] = widgets
  override def currentlyActiveInterface =
    ComputerInterface(widgets, workspace.world.turtleShapeList.shapes, workspace.world.linkShapeList.shapes)
  def interfaceWidgets = Seq()

  // should we be logging or doing something else here besides just println? JC - 12/28/10
  private val listener = new ClientEventListener() {
    def addClient(clientId: String, remoteAddress: String) {
      //println("added client: " + clientId + " on: " + remoteAddress)
    }
    def clientDisconnect(clientId: String) {
      //println("client disconnected: " + clientId)
    }
    def logMessage(message:String) {
      //println(message)
    }
  }
  val connectionManager = new ConnectionManager(this, listener, this.workspace)

  /// connection management
  def disconnect() {
    connectionManager.shutdown()
    messagesList.clear()
  }

  @throws(classOf[HubNetException])
  def reset() {
    if (connectionManager.isRunning) {
      connectionManager.shutdown()
      // when we reset, make sure to get rid of any locally connected clients.
      // shutdown will make sure they are already disconnected.
      // JC 3/31/11
      clientsAddedViaNewClient.clear()
    }
    // we could demand that this property is set, and throw a HubNetException here.
    // for now, I've decided to just fall back on the user name. JC - 12/18/10
    var serverName: String = System.getProperty("org.nlogo.hubnet.server.name")
    if(serverName == null || serverName.trim == "") serverName = System.getProperty("user.name", "")
    connectionManager.startup(serverName)
    // println("started HubNet server on port " + connectionManager.port)
  }

  // there is no client editor in headless. this stuff shouldn't be called i think.
  // ideally, we wouldnt even have to implement these with no ops
  // but i believe that would take fundamental changes all the way into AbstractWorkspace.
  def clientEditor: AnyRef = null
  def getInterfaceWidth = 0
  def getInterfaceHeight = 0
  def openClientEditor() {}
  def closeClientEditor() {}

  // other no-op gui related stuff
  def setTitle(name: String, dir: String, modelType: ModelType) {}
  def importClientInterface(model: Model, client: Boolean) {} // only called from the File menu.

  private val clientIds = Iterator.from(0)
  private val clientsAddedViaNewClient = ListBuffer[TestClient]()
  lazy val executor: ExecutorService = Executors.newCachedThreadPool()

  // creates a new local client. this method should be renamed.
  def newClient(isRobo: Boolean, waitTime: Int) {
    // login happens automatically in the constructor.
    // its debateable that i should do this, but ok for now.
    // JC - 3/30/11
    executor.submit(new Runnable(){
      override def run = {
        val client =
          new TestClient(userId="Local " + clientIds.next(), port=connectionManager.port)
        clientsAddedViaNewClient.synchronized(clientsAddedViaNewClient += client)
      }
    })
  }

  // send a message from a local client to the server
  def sendFromLocalClient(clientName:String, tag: String, content: AnyRef) = {
    // first, remove any dead clients, just for safety
    clientsAddedViaNewClient.synchronized {
      for(c<-clientsAddedViaNewClient; if c.dead) clientsAddedViaNewClient -= c
    }
    val theClient = clientsAddedViaNewClient.find(_.userId == clientName)
    theClient match {
      case Some(c) => c.sendActivityCommand(tag, content); None
      case _ => Some("client not found: " + clientName)
    }
  }

  def showControlCenter() {} // maybe we could log a message here.
}
