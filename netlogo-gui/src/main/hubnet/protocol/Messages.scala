// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.protocol

import org.nlogo.api.{ PlotInterface, TwoDVersion }
import org.nlogo.api.HubNetInterface.ClientInterface
import java.io.{ObjectInputStream, ObjectOutputStream, DataOutputStream,
  ByteArrayOutputStream, ByteArrayInputStream, DataInputStream, IOException,
  Serializable => JSerializable }

// Message between hubnet clients and hubnet servers
@SerialVersionUID(0)
trait Message extends JSerializable

// Messages sent to establish handshake between client and server
@SerialVersionUID(0)
case class HandshakeFromClient(userId: String, clientType: String) extends Message
@SerialVersionUID(1)
case class HandshakeFromServer(activityName: String, clientInterface: ClientInterface) extends Message

// Message from server tells client that login has failed
@SerialVersionUID(0)
case class LoginFailure(content: String) extends Message

@SerialVersionUID(0)
case class OverrideMessage(data: Any, clear: Boolean) extends Message

// Message from server tells client the server has disconnected it,
// or from client tells server it has disconnected itself
@SerialVersionUID(0)
case class ExitMessage(reason:String) extends Message

// Message from client tells server the client has successfully connected
@SerialVersionUID(0)
case object EnterMessage extends Message

/**
 * Message from server to client when server changes value of client widget
 * @param content information to be handled by the indicated widget
 * @param tag the name of the widget
 */
@SerialVersionUID(0)
case class WidgetControl(content: AnyRef with JSerializable, tag: String) extends Message

// Message from server tells client to disable the view until the next update
@SerialVersionUID(0)
case object DisableView extends Message

@SerialVersionUID(0)
case object ClearOverrideMessage extends Message

// Message from client indicates user interaction with widget
@SerialVersionUID(0)
case class ActivityCommand(tag: String, content: AnyRef) extends Message

case class AgentPerspectiveMessage(bytes: Array[Byte]) extends Message

/**
 * Message from server tells client to update the view
 * @param worldData a serialized <code>ServerWorld</code>
 * @see org.nlogo.hubnet.mirroring.ServerWorld
 */
@SerialVersionUID(0)
case class ViewUpdate(worldData: Array[Byte]) extends Message

/**
 * Message from server tells client to update Plots
 */
@SerialVersionUID(0)
case class PlotUpdate(plotInterface: PlotInterface) extends Message {
  private val bytes = {
    val out = new ByteArrayOutputStream()
    new ObjectOutputStream(out).writeObject(plotInterface)
    out.toByteArray()
  }
  def plot =
    new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject().asInstanceOf[PlotInterface]
}

@SerialVersionUID(0)
case class PlotControl(content: AnyRef, plotName: String) extends Message

/**
 * Message from server tells client to display text
 */
object Text {
  sealed trait MessageType extends JSerializable
  object MessageType {
    case object USER extends MessageType
    case object TEXT extends MessageType
    case object CLEAR extends MessageType
  }
}
@SerialVersionUID(0)
case class Text(content: String, messageType: Text.MessageType) extends Message

object DiscoveryMessage {
  /**
   * Creates a new discovery message.
   * Called by the server.
   * @param uniqueId the unique id of the server
   * @param modelName the name of the current model
   * @param portNumber the port the server is listening on
   */
  def apply(uniqueId: String, modelName: String, portNumber: Int): DiscoveryMessage =
    new DiscoveryMessage(TwoDVersion.version, uniqueId, modelName, portNumber.toString, null)

  /**
   * Reconstructs a discovery message from serialized data.
   * Called by the client.
   * @param hostName name of the host sending the message
   * @param bytes packet data describing the message
   */
  @throws(classOf[IOException])
  def deserialize(hostName: String, bytes: Array[Byte]): DiscoveryMessage = {
    val dis = new DataInputStream(new ByteArrayInputStream(bytes))
    val v = dis.readUTF()
    if (v != TwoDVersion.version)
      throw new IOException("Cannot deserialize DiscoveryMessage: version mismatch")
    new DiscoveryMessage(v, dis.readUTF(), dis.readUTF(), dis.readUTF(), hostName)
  }
}

/**
 * A message broadcast by HubNet servers to announce their existence.
 * Sent by {@link org.nlogo.hubnet.server.DiscoveryAnnouncer} in the server and received by
 * org.nlogo.hubnet.client.DiscoveryListener in the client.
 * @param version - Version id of the server sending this message
 * @param uniqueId - User-defined uniqueId of the server sending this message
 * @param modelName - Name of the model running on the server sending this message
 * @param portNumber -Port on which the server senging this message is operating
 * @param hostName - The host name of the originating server. This is null on the server
 *        side and is filled in on the client side with information from the
 *        transmission packet.
 */
case class DiscoveryMessage(version: String, uniqueId: String, modelName: String, portNumber: String, hostName: String){
  // The time the message was received. Filled in on the client side.
  var creationTime: Long = 0
  def toByteArray = {
    val bos = new ByteArrayOutputStream()
    val dos = new DataOutputStream(bos)
    List(version, uniqueId, modelName, portNumber).foreach(dos.writeUTF)
    bos.toByteArray
  }
}
