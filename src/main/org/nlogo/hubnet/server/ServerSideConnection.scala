package org.nlogo.hubnet.server

import org.nlogo.hubnet.protocol._
import org.nlogo.hubnet.connection.MessageEnvelope._
import java.util.Date
import org.nlogo.api.{I18N, Version}
import org.nlogo.hubnet.connection.{ClientRole , Streamable , AbstractConnection}

// TODO remove die hard. fix ALL of this business.
// the multiple disconnect methds
// the calling up to the super classes disconnect
// clients sending messages out of order
// its all handled horribly. JC 1/6/11

// TODO figure out the difference between the disconnect methods and server.remove client
// understand when to call each, and go back and adjust serveral call sites in here.
// the disconncting flag is heavily involved here.
// one of the disconnect methods needs to set the currentHandler to LoggingIn
// or maybe we need some DisconnectedHandler?

// these are in order of life cycle:
// version exchange
// handshake
// enter message
// activity commands
// exit message

object ConnectionStates extends Enumeration {
  val Disconnected, AwaitingVersionNumber, AwaitingEnterMessage,
      ControllerClientLoggedOn, ParticipantClientLoggedOn = Value
}

class ServerSideConnection(connectionStreams:Streamable, val remoteAddress: String, server:ConnectionManagerInterface)
        extends AbstractConnection("ServerSideConnection", connectionStreams) {

  import ConnectionStates._

  // When a new client connects, they immediately go into the AwaitingVersionNumber state.
  private var currentConnectionState = AwaitingVersionNumber
  def getCurrentState : ConnectionStates.Value = currentConnectionState

  @volatile private var disconnecting = false
  var clientId: String = null


  override def receiveData(message:AnyRef) {

    currentConnectionState = (currentConnectionState, message) match {

      case (Disconnected, _) =>
        // TODO: Not really sure what to do here.
        Disconnected

      case (AwaitingVersionNumber, message:String) =>
        if(message == Version.version) {
          sendData(Version.version)
          AwaitingEnterMessage
        } else {
          sendData(new LoginFailure("The version of the HubNet Client you are using does not "
                  + "match the version of the server.  Please use the HubNet Client that comes with " + Version.version))
          disconnect(false, "")
          Disconnected
        }

      case (AwaitingVersionNumber, _) =>
        sendData(InvalidMessage("expected version number, but didnt get it.", message))
        disconnect(notifyClient=true, reason="Received invalid message from client: " + message)
        Disconnected

      case (AwaitingEnterMessage, EnterMessage(userId, clientType, clientRole)) =>
        clientRole match {
          case ClientRole.Participant =>
            try output.synchronized {
              if (server.finalizeConnection(this, desiredClientId = userId)){
                clientId = userId
                sendData(server.createHandshakeMessage(clientType))

                // make sure clients get current view mirroring state.
                // ideally we'd only send this full update to the client that
                // just joined, rather than broadcasting it to everyone. - ST 12/5/09
                if (HubNetUtils.viewMirroring) server.fullViewUpdate() else sendData(DisableView)
                if (HubNetUtils.plotMirroring) server.sendPlots(userId)
                ParticipantClientLoggedOn
              }
              else {
                sendData(new LoginFailure("\"" + clientId + "\" is already taken by another user. Please choose another name."))
                disconnect(notifyClient=false, reason="")
                Disconnected
              }
            }
            catch {
              case ex: RuntimeException =>
                org.nlogo.util.Exceptions.handle(ex)
                Disconnected
            }
          case ClientRole.Controller =>
            try output.synchronized {
              server.finalizeControllerClientConnection(this)
              sendData(server.createControllerClientHandshakeMessage)
              if (HubNetUtils.viewMirroring) server.fullViewUpdate() else sendData(DisableView)
              ControllerClientLoggedOn
            }
            catch {
              case ex: RuntimeException =>
                org.nlogo.util.Exceptions.handle(ex)
                Disconnected
            }
          case _ =>
            disconnect(true, "Invalid client role: " + clientRole)
            Disconnected
        }

      case (AwaitingEnterMessage, _) =>
        sendData(InvalidMessage("expected enter message, but didnt get it.", message))
        disconnect(false, "")
        Disconnected

      case (ParticipantClientLoggedOn, message: ActivityCommand) =>
        server.putClientData(ActivityMessageEnvelope(clientId, message.widget, message.tag, message.content))
        ParticipantClientLoggedOn

      case (ParticipantClientLoggedOn, ExitMessage(reason)) =>
        disconnect(false, reason)
        Disconnected

      case (ParticipantClientLoggedOn, _) =>
        sendData(InvalidMessage("unexpected message", message))
        disconnect(false, "")
        Disconnected

      case (ParticipantClientLoggedOn, _) =>
        sendData(InvalidMessage("unexpected message", message))
        disconnect(false, "")
        Disconnected

      case (ControllerClientLoggedOn, message: ActivityCommand) => {
        server.handleControllerClientMessage(this, message)
        ControllerClientLoggedOn
      }

      case (ControllerClientLoggedOn, ExitMessage(reason)) =>
        disconnect(false, reason)
        Disconnected

      case _ =>
        disconnect(true, reason="Unknown connection state or message type. Connection state: "
                + currentConnectionState.toString
                + ". Message type: " + message.getClass.getCanonicalName
                + ". Message: " + message.toString)
        Disconnected
    }
  }

  override def disconnect(reason:String) {
    val notifyClient = reason != null && reason != ""
    disconnect(notifyClient, reason)
  }

  def disconnect(notifyClient:Boolean, reason:String) {
    currentConnectionState match {
      case Disconnected =>
      case AwaitingVersionNumber | AwaitingEnterMessage =>
        finalizeDisconnect(notifyClient, reason)
      case ParticipantClientLoggedOn =>
        server.removeParticipantClient(clientId, notifyClient=false, reason=reason)
        // Note: server.removeParticipantClient() is responsible for calling finalizeDisconnect()
      case ControllerClientLoggedOn =>
        server.removeControllerClient(this, notifyClient, reason)
        // Note: server.removeControllerClient() is responsible for calling finalizeDisconnect()
    }
  }

  def finalizeDisconnect(notifyClient:Boolean, reason:String) {
    if(notifyClient) sendData(ExitMessage(reason))
    stopWriting()
    currentConnectionState = Disconnected
    super.disconnect(reason)
  }

  override def handleEx(e:Exception, sendingEx:Boolean) {
    if(!disconnecting) {
      disconnecting = true
      if(clientId != null) {
        disconnect(!sendingEx, e.toString)
      }
      else{
        stopWriting()
        super.disconnect(e.toString)
      }
      if(e.isInstanceOf[ClassNotFoundException]) {
        val message =
          "An incompatible version of the HubNet Client tried logging in.\n" +
          "Please ensure that everyone is using the version of the HubNet Client that  came with this release. " +
          Version.version
        org.nlogo.util.Exceptions.handle(new Exception(message, e))
      } else {
        if (!sendingEx) org.nlogo.util.Exceptions.handle(e)
        else{
          // sending exceptions we just print to standard err
          System.err.println("@ " + new Date() + " : ")
          System.err.println( "sending exception:\n" + e )
          server.logMessage( e.getMessage )
        }
      }
    }
  }
}
