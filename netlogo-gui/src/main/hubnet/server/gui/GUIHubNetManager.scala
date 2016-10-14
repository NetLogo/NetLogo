// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import org.nlogo.api.{ ModelLoader, ModelType, ViewInterface }
import org.nlogo.api.HubNetInterface.ClientInterface
import org.nlogo.core.{ Femto, FileMode, Model, Widget => CoreWidget }
import org.nlogo.core.model.WidgetReader
import org.nlogo.hubnet.protocol.ComputerInterface
import org.nlogo.hubnet.connection.HubNetException
import org.nlogo.hubnet.server.{HubNetManager, ClientEventListener, ConnectionManager}
import org.nlogo.fileformat.ModelConversion
import org.nlogo.nvm.DefaultCompilerServices
import org.nlogo.util.Utils, Utils.reader2String
import org.nlogo.awt.EventQueue.invokeLater
import org.nlogo.swing.Implicits._
import org.nlogo.window._

import java.net.InetAddress
import java.awt.Component

class GUIHubNetManager(workspace: GUIWorkspace,
                       linkParent: Component,
                       editorFactory: EditorFactory,
                       ifactory: InterfaceFactory,
                       menuFactory: MenuBarFactory,
                       loader: ModelLoader,
                       modelConverter: ModelConversion)
  extends HubNetManager(workspace, loader, modelConverter) with ViewInterface {

  private var _clientEditor: HubNetClientEditor = new HubNetClientEditor(workspace, linkParent, ifactory, menuFactory)
  // used in the discovery messages, and displayed in the control center.
  private var serverName: String = System.getProperty("org.nlogo.hubnet.server.name")

  private val listener = new ClientEventListener() {
    def addClient(clientId: String, remoteAddress: String) {
      invokeLater(() => controlCenter.addClient(clientId, remoteAddress))
    }
    def clientDisconnect(clientId: String) {
      invokeLater(() => controlCenter.clientDisconnect(clientId))
    }
    def logMessage(message:String) {
      invokeLater(() => controlCenter.logMessage(message))
    }
  }
  val connectionManager = new ConnectionManager(this, listener, this.workspace)
  var controlCenter: ControlCenter = null // created in showControlCenter

  /// view mirroring / view interface

  /**
   * Launch a local computer client, if there is a session open connect to it.
   */
  override def newClient(isRobo: Boolean, waitTime: Int) {
    val clientApp = Femto.get[ClientAppInterface]("org.nlogo.hubnet.client.ClientApp")
    val host = try Some(InetAddress.getLocalHost.getHostAddress.toString)
    catch {case ex: java.net.UnknownHostException => None}
    // TODO: this seems like a bunch of bugs waiting to happen
    clientApp.startup(editorFactory, "", host.orNull, connectionManager.port, true,
      isRobo, waitTime, new DefaultCompilerServices(workspace.compiler))
  }

  /// client editor
  override def modelWidgets: Seq[CoreWidget] = _clientEditor.interfaceWidgets

  override def currentlyActiveInterface: ClientInterface =
    ComputerInterface(_clientEditor.interfaceWidgets, workspace.world.turtleShapeList.shapes, workspace.world.linkShapeList.shapes)

  def clientEditor: AnyRef = _clientEditor
  def getInterfaceWidth = _clientEditor.interfacePanel.getPreferredSize.width
  def getInterfaceHeight = _clientEditor.interfacePanel.getPreferredSize.height
  def load(model: Model) {
    val hubNetWidgets = model.optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient").foreach { hubNetWidgets =>
      _clientEditor.load(hubNetWidgets)
    }
  }

  override def updateModel(m: Model): Model = {
    m.withOptionalSection("org.nlogo.modelsection.hubnetclient", Some(interfaceWidgets), Seq())
  }

  def interfaceWidgets: Seq[CoreWidget] = _clientEditor.interfaceWidgets

  type Component       = Seq[CoreWidget]
  def getComponent     = _clientEditor.interfaceWidgets
  def defaultComponent = Seq()

  @throws(classOf[java.io.IOException])
  def importClientInterface(model: Model, client: Boolean) {
    _clientEditor.close()
    val widgets: Seq[CoreWidget] =
      if (client)
        model.optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient").getOrElse(Seq())
      else
        model.widgets
    _clientEditor.load(widgets)
    openClientEditor()
  }

  def setTitle(name: String, dir: String, modelType: ModelType) {
    _clientEditor.setTitle(name, dir, modelType)
  }

  def showControlCenter() {
    controlCenter =
      new ControlCenter(connectionManager, workspace.getFrame, serverName, workspace.modelNameForDisplay)
    controlCenter.pack()
    controlCenter.setVisible(true)
  }

  private def closeControlCenter(){
    // the hubnet manager is created in AbstractWorkspace, but the control center
    // isn't started unless showControlCenter is called.
    if(controlCenter != null) controlCenter.dispose()
  }

  /// client editor
  def openClientEditor() {
    org.nlogo.awt.Positioning.moveNextTo(_clientEditor, linkParent)
    _clientEditor.setVisible(true)
    _clientEditor.setSize(_clientEditor.getPreferredSize)
  }

  def closeClientEditor() {
     _clientEditor.close()
     _clientEditor.dispose()
     _clientEditor = new HubNetClientEditor(workspace, linkParent, ifactory, menuFactory)
  }

  @throws(classOf[HubNetException])
  def reset() {
    if(connectionManager.isRunning) {
      connectionManager.shutdown()
      closeControlCenter()
    }
    if (serverName == null || serverName.trim == "")
      serverName = new StartupDialog(workspace.getFrame) { setVisible(true) }.getName()
    connectionManager.startup(serverName)
    showControlCenter()
  }

  def disconnect() {
    connectionManager.shutdown()
    closeControlCenter()
    messagesList.clear()
  }

  // just for testing in headless, for now.
  // eventually i would like to make this work for GUI too.
  // JC - 3/31/11
  def sendFromLocalClient(clientName:String, tag: String, content: AnyRef) = {
    Some("unimplemented")
  }
}
