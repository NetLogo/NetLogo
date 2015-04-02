// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.hubnet.protocol._
import org.nlogo.hubnet.connection.MessageEnvelope._
import java.util.Date
import org.nlogo.api.Version
import org.nlogo.hubnet.connection.{Streamable, AbstractConnection}

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
class ServerSideConnection(connectionStreams:Streamable, val remoteAddress: String, server:ConnectionManagerInterface)
        extends AbstractConnection("ServerSideConnection", connectionStreams) {

  private var validClientVersion = false
  @volatile private var disconnecting = false
  var clientId: String = null

  override def receiveData(a:AnyRef) {
    a match {
      // note: if clientId is null, that means that we haven't yet had a successful login yet.
      // so, this string we received must be the version of the client.
      case s:String =>
        if(clientId == null) {
          if (s == Version.version) validClientVersion = true
          sendData(Version.version)
        }
        else {
          // TODO: handle the client sending another string after theyve already logged in
          // probably should just send them an ExitMessage and disconnect.
          // TODO: is disconnect the right call here?
          dieHard(reason="Received message out of order: " + a)
        }
      case m:Message => handleMessage(m)
      // TODO: is disconnect the right call here?
      case _ => dieHard(reason="Unknown message type: " + a.getClass)
    }
  }

  trait MessageHandler{
    def handleMessage(message:Message)
  }

  /**
   * I just realized that this stuff is quite broken.
   * Clients can just send enter messages, activity commands, and exit messages
   * without ever having sent a handshake message! thinks will break in
   * undesirable ways on the server. this happened to work when we wrote the only clients.
   * it wont work now that we want to allow other people to write them.
   * we ought to have a nice state machine here.
   * JC 1/1/11
   */
  private def handleMessage(message:Message) {
    message match {
      case HandshakeFromClient(userId, clientType) => {
        if(userId == null || userId.trim == ""){
          sendData(new LoginFailure("Server received empty username."))
        }
        else if (!validClientVersion) {
          sendData(new LoginFailure("The version of the HubNet Client you are using does not "
                  + "match the version of the server.\nPlease use the HubNet Client that comes with " + Version.version))
        }
        else if(!server.isSupportedClientType(clientType)){
          sendData(new LoginFailure("The HubNet model you are connected to does not support your client type: " + clientType))
        }
        else try output.synchronized {
          if (server.finalizeConnection(this, desiredClientId = userId)){
            clientId = userId
            sendData(server.createHandshakeMessage(clientType))
            // make sure clients get current view mirroring state.
            // ideally we'd only send this full update to the client that
            // just joined, rather than broadcasting it to everyone. - ST 12/5/09
            if (HubNetUtils.viewMirroring) server.fullViewUpdate() else sendData(DisableView)
            if (HubNetUtils.plotMirroring) server.sendPlots(userId)
          }
          else {
            sendData(new LoginFailure("\"" + userId + "\" is already taken by another user.\nPlease choose another name."))
          }
        }
        catch { case ex: RuntimeException => org.nlogo.util.Exceptions.handle(ex) }
      }
      // TODO
      // its possible that the server doesnt even know who this client is!
      // they might not have sent a proper handshake yet.
      case EnterMessage => server.putClientData(EnterMessageEnvelope(clientId))
      // TODO
      // its possible that the server doesnt even know who this client is!
      // they might not have sent a proper handshake yet.
      case m: ActivityCommand => server.putClientData(ActivityMessageEnvelope(clientId, m.tag, m.content))
      // my guess on notifyClient=false here is that the client is requesting the exit
      // so theres no reason to notify them. JC 1/3/11
      case ExitMessage(reason) => server.removeClient(clientId, notifyClient=false, reason)
      case _ =>
        // TODO
        // this is another argument for a proper state machine here.
        // if the server didnt know about the client (was unable to remove it),
        // then that means the never client hasnt yet sent a valid HandshakeFromClient
        // (and possibly not a version number either)
        // yet here, the client is sending something that we dont even know about.
        // we need to ask the server to remove it, and if so, we're done because it would have
        // been sent a message. if not...that means we should disconnect it ourselves here.
        // fortunately, this shouldn't happen. we control the main client (in hubnet.client)
        // and the android client we are building i have some control over.
        // but, this stuff still needs cleaning up
        // JC 1/6/11
        dieHard(reason="Unknown message type: " + message.getClass)
    }
  }

  // TODO...this absolutely HAS to be cleaned up before 4.2 final.
  // also, its not even really dying hard, its dying hard if it needs to.
  // but whatever, it will all be ripped out soon.
  private def dieHard(reason:String) {
    if(! server.removeClient(clientId, notifyClient=true, reason)){
      waitForSendData(ExitMessage(reason))
      stopWriting()
      // this call really goes to the super class, not to the disconnect method here.
      // i hate myself for having to do this. the disconnect stuff here desparately needs cleaning up.
      super.disconnect(reason)
    }
  }

  override def disconnect(reason:String) {
    if(!disconnecting) {
      disconnecting = true
      server.removeClient(clientId, false, reason)
    }
  }

  def disconnect(notifyClient:Boolean, reason:String) {
    disconnecting = true
    server.putClientData(ExitMessageEnvelope(clientId))
    if(notifyClient) sendData(ExitMessage(reason))
    stopWriting()
    super.disconnect(reason)
  }

  override def handleEx(e:Exception, sendingEx:Boolean) {
    if(!disconnecting) {
      disconnecting = true
      if(clientId != null) server.removeClient(clientId, ! sendingEx, e.toString)
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
