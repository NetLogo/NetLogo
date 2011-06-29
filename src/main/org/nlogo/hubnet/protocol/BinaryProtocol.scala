package org.nlogo.hubnet.protocol

import org.nlogo.hubnet.connection.ClientRoles
import org.nlogo.api.LogoList
import java.io._

case class BinaryProtocol(in:InputStream, out:OutputStream) extends Protocol {
  val name = "Binary"
  val dataIn = new DataInputStream(in)
  val dataOut = new DataOutputStream(out)
  def readMessage(): Message = BinaryProtocol.readMessage(dataIn)
  def writeMessage(m:Message) { BinaryProtocol.writeMessage(m, dataOut) }
}

object BinaryProtocol {
  val HANDSHAKE = 0
  val LOGIN_FAILURE = 1
  val EXIT = 2
  val WIDGET_CONTROL = 3
  val DISABLE_VIEW = 4
  val VIEW_UPDATE = 5
  val PLOT_CONTROL = 6
  val PLOT_UPDATE = 7
  val OVERRIDE = 8
  val CLEAR_OVERRIDE = 9
  val AGENT_PERSPECTIVE = 10
  val TEXT = 11
  val ENTER = 12
  val ACTIVITY_COMMAND = 13
  val INVALID = 14
  val VERSION = 15

  def readMessage(in:DataInputStream): Message = {
    def readString(): String = {
      val length = in.readInt()
      val bytes = new Array[Byte](length)
      in.read(bytes)
      new String(bytes)
    }
    def readAny(): AnyRef = {
      val contentType = in.readInt()
      contentType match {
        case 0 => readString()
        case 1 => in.readDouble().asInstanceOf[AnyRef]
        case 2 => in.readBoolean().asInstanceOf[AnyRef]
      }
    }
    val messageId = in.readInt()
    messageId match {
      case HANDSHAKE => HandshakeFromServer(readString(), LogoList())
      case LOGIN_FAILURE => LoginFailure(readString())
      case EXIT => ExitMessage(readString())
      case WIDGET_CONTROL => WidgetControl(readAny(), readString())
      case DISABLE_VIEW => DisableView
      case VIEW_UPDATE => sys.error("implement me")
      case PLOT_CONTROL => sys.error("implement me")
      case PLOT_UPDATE => sys.error("implement me")
      case OVERRIDE => sys.error("implement me")
      case CLEAR_OVERRIDE => ClearOverrideMessage
      case AGENT_PERSPECTIVE => sys.error("implement me")
      case TEXT => Text(readString(), in.readInt() match {
        case 0 => Text.MessageType.TEXT
        case 1 => Text.MessageType.USER
        case 2 => Text.MessageType.CLEAR
      })
      // making a choice here to not support the teacher client throuh this.
      // always using Participant
      case ENTER => EnterMessage(readString(), readString(), ClientRoles.Participant)
      case ACTIVITY_COMMAND => ActivityCommand(readString(), readString(), readAny())
      case INVALID => InvalidMessage(readString(), readMessage(in))
      case VERSION =>
        VersionMessage(readString())
    }
  }

  def writeMessage(message:Message, out: DataOutputStream) {
    def writeString(s:String) = {
      out.writeInt(s.length)
      out.write(s.getBytes)
    }
    def writeAny(content:Any){
      if (content.isInstanceOf[String]) {
        out.writeInt(0)
        writeString(content.toString)
      }
      else if (content.isInstanceOf[Double]) {
        out.writeInt(1)
        out.writeDouble(content.asInstanceOf[Double])
      }
      else if (content.isInstanceOf[Boolean]) {
        out.writeInt(2)
        out.writeBoolean(content.asInstanceOf[Boolean])
      }
    }
    // first write the message id
    out.writeInt(getMessageId(message))
    // the write the fields for the message
    message match {
      case HandshakeFromServer(model, interface) => writeString(model)
      case LoginFailure(content) => writeString(content)
      case ExitMessage(reason) => writeString(reason)
      case WidgetControl(content, tag) =>
        writeAny(content)
        writeString(tag)
      case DisableView => // no fields! done.
      case ViewUpdate(worldData) => sys.error("implement me")// TODO: worldData is tough
      case PlotControl(content, plotName) => sys.error("implement me")
      case PlotUpdate(plot) => sys.error("implement me")
      case OverrideMessage(data, clear) => sys.error("implement me")
      case ClearOverrideMessage => // no fields! done.
      case AgentPerspectiveMessage(bytes) => sys.error("implement me") // TODO: no idea what these bytes are
      case Text(content, messageType) =>
        writeString(content)
        out.writeInt(messageType match {
          case Text.MessageType.TEXT => 0
          case Text.MessageType.USER => 1
          case Text.MessageType.CLEAR => 2
        })
      case EnterMessage(userId, clientType, clientRole) =>
        writeString(userId)
        writeString(clientType)
      case ActivityCommand(wType, tag, content) =>
        writeString(wType)
        writeString(tag)
        writeAny(content)
      case InvalidMessage(s, orig) =>
        writeString(s)
        writeMessage(orig, out)
      case VersionMessage(s) =>
        writeString(s)
    }
  }

  def getMessageId(message:Message): Int = message match {
    case m: HandshakeFromServer => HANDSHAKE
    case m: LoginFailure => LOGIN_FAILURE
    case m: ExitMessage => EXIT
    case m: WidgetControl => WIDGET_CONTROL
    case DisableView => DISABLE_VIEW
    case m: ViewUpdate => VIEW_UPDATE
    case m: PlotControl => PLOT_CONTROL
    case m: PlotUpdate => PLOT_UPDATE
    case m: OverrideMessage => OVERRIDE
    case ClearOverrideMessage => CLEAR_OVERRIDE
    case m: AgentPerspectiveMessage => AGENT_PERSPECTIVE
    case m: Text => TEXT
    case m: EnterMessage => ENTER
    case m: ActivityCommand => ACTIVITY_COMMAND
    case m: InvalidMessage => INVALID
    case m: VersionMessage => VERSION
  }
}
