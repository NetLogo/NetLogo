// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server.gui

import org.nlogo.api.{ ModelLoader, ModelType, ViewInterface }
import org.nlogo.api.HubNetInterface.ClientInterface
import org.nlogo.core.{ Femto, Model, Widget => CoreWidget }
import org.nlogo.hubnet.protocol.ComputerInterface
import org.nlogo.hubnet.connection.{ HubNetException, NetworkUtils }
import org.nlogo.hubnet.server.{HubNetManager, ClientEventListener, ConnectionManager}
import org.nlogo.fileformat.ModelConversion
import org.nlogo.awt.EventQueue.invokeLater
import org.nlogo.window._

import java.net.{ InetAddress, NetworkInterface }
import java.awt.Component
import javax.swing.JOptionPane

class GUIHubNetManager(workspace: GUIWorkspace,
                       linkParent: Component,
                       ifactory: InterfaceFactory,
                       menuFactory: MenuBarFactory,
                       loader: ModelLoader,
                       modelConverter: ModelConversion)
  extends HubNetManager(workspace, loader, modelConverter) with ViewInterface {

  private var _clientEditor: HubNetClientEditor = new HubNetClientEditor(workspace, workspace.modelTracker, linkParent, ifactory, menuFactory)
  // used in the discovery messages, and displayed in the control center.
  private var serverName: String = System.getProperty("org.nlogo.hubnet.server.name")
  private var serverInterface = Option.empty[(NetworkInterface, InetAddress)]

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
    clientApp.startup("", host.orNull, connectionManager.port, true,
      isRobo, waitTime, workspace.compiler)
  }

  /// client editor
  override def modelWidgets: Seq[CoreWidget] = _clientEditor.interfaceWidgets

  override def currentlyActiveInterface: ClientInterface =
    ComputerInterface(_clientEditor.interfaceWidgets, workspace.world.turtleShapeList.shapes, workspace.world.linkShapeList.shapes)

  def clientEditor: AnyRef = _clientEditor
  def getInterfaceWidth = _clientEditor.interfacePanel.getPreferredSize.width
  def getInterfaceHeight = _clientEditor.interfacePanel.getPreferredSize.height
  def load(model: Model) {
    model.optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient").foreach { hubNetWidgets =>
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
    if (controlCenter == null) {
      controlCenter =
        new ControlCenter(connectionManager, workspace.getFrame, serverName, workspace.modelNameForDisplay, serverInterface.map(_._2))
      controlCenter.pack()
    }
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
     _clientEditor = new HubNetClientEditor(workspace, workspace.modelTracker, linkParent, ifactory, menuFactory)
  }

  @throws(classOf[HubNetException])
  def reset() {
    if(connectionManager.isRunning) {
      connectionManager.shutdown()
      closeControlCenter()
    }
    while (serverName == null || serverName.isEmpty || serverInterface.isEmpty) {
      val (name, selectedNetwork) = getNameAndSelectedNetwork()
      serverName = name
      serverInterface = selectedNetwork
      if (serverInterface.isEmpty) {
        JOptionPane.showMessageDialog(workspace.getFrame,
          "Unable to find a suitable network connection for HubNet, please check your network connection")
      }
      else if (serverInterface.get._1.isLoopback)
        JOptionPane.showMessageDialog(workspace.getFrame,
          "Unable to find an external network connection, HubNet will be served locally")
    }
    serverInterface.foreach { nw =>
      connectionManager.startup(serverName, nw)
      showControlCenter()
    }
  }

  private def getNameAndSelectedNetwork(): (String, Option[(NetworkInterface, InetAddress)]) = {
    val networkChoices = NetworkUtils.findViableInterfaces
    val preferredNetworkInterface = NetworkUtils.recallNetworkInterface
    val preferredNetworkConnection = networkChoices.find(_._1 == preferredNetworkInterface)
    if (serverName == null || serverName.trim == "" || serverInterface.isEmpty) {
      val dialog =
        new StartupDialog(workspace.getFrame, networkChoices, preferredNetworkConnection) {
          setVisible(true)
        }
      dialog.selectedNetwork.foreach {
        case (nic, _) => NetworkUtils.rememberNetworkInterface(nic)
      }
      (dialog.getName, (dialog.selectedNetwork orElse networkChoices.headOption orElse NetworkUtils.loopbackInterface))
    } else {
      (serverName, serverInterface)
    }
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
