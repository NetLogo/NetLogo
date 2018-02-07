// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.hubnet.connection.Streamable
import org.nlogo.hubnet.protocol._
import org.nlogo.util.{ MockSuite, TaggedFunSuite, TwoDTag }
import org.nlogo.api.TwoDVersion

// tests for the hubnet session behavior on the server side.
class ServerSideConnectionTests extends TaggedFunSuite(TwoDTag) with MockSuite {

  val serverVersion = TwoDVersion.version

  val clientId = "test-user"

  mockTest("send version, get version back"){
    val conn = newConnection()
    conn.receiveData(serverVersion)
    assert(conn.nextOutgoingMessage === serverVersion)
  }

  // these next tests are a little strange. i guess this behavior allows clients to
  // fake that they are really an up to date version.
  // i wonder if we should just boot them if they send an old version.
  mockTest("send old version, get current version back"){
    val conn = newConnection()
    conn.receiveData("NetLogo 1.0")
    assert(conn.nextOutgoingMessage === serverVersion)
  }

  mockTest("send old version, get current version back, send current version"){
    val conn = newConnection()
    conn.receiveData("NetLogo 1.0")
    assert(conn.nextOutgoingMessage === serverVersion)
    conn.receiveData(serverVersion)
    assert(conn.nextOutgoingMessage === serverVersion)
  }

  mockTest("send old version, then handshake, get login failure back"){
    val conn = newConnection()
    conn.receiveData("NetLogo 1.0")
    assert(conn.nextOutgoingMessage === serverVersion)
    conn.receiveData(HandshakeFromClient(clientId, "COMPUTER"))
    // this was lifted right from ServerSideConnection.scala
    val error = "The version of the HubNet Client you are using does not " +
                "match the version of the server.\nPlease use the HubNet Client that comes with " + serverVersion
    conn.nextOutgoingMessage match {
      case LoginFailure(reason) => assert(reason === error)
      case _ => fail("expected LogonFailure")
    }
  }

  mockTest("send correct version, but invalid client type, get login failure back"){
    val server = mock[ConnectionManagerInterface]
    val conn = newConnection(server)
    conn.receiveData(serverVersion)
    assert(conn.nextOutgoingMessage === serverVersion)

    expecting{ one(server).isSupportedClientType(arg("INVALID")); willReturn(false) }
    when{ conn.receiveData(HandshakeFromClient(clientId, "INVALID")) }

    // this was lifted right from ServerSideConnection.scala
    val error = "The HubNet model you are connected to does not support your client type: INVALID"
    conn.nextOutgoingMessage match {
      case LoginFailure(reason) => assert(reason === error)
      case _ => fail("expected LogonFailure")
    }
  }

  mockTest("sending EnterMessage before handshake does not propogate an error message") { new LoginFailure {
    conn.receiveData(serverVersion)
    assert(conn.nextOutgoingMessage === serverVersion)

    conn.receiveData(EnterMessage)
    assert(conn.nextOutgoingMessage === ExitMessage("Client login failed, please try logging in again"))
  } }

  mockTest("sending ActivityMessage before handshake does not propogate that message") { new LoginFailure {
    conn.receiveData(serverVersion)
    assert(conn.nextOutgoingMessage === serverVersion)

    conn.receiveData(ActivityCommand("Selection", "two"))
    assert(conn.nextOutgoingMessage === ExitMessage("Client login failed, please try logging in again"))
  } }

  mockTest("sending ExitMessage before handshake does not propogate an exit message") { new LoginFailure {
    conn.receiveData(serverVersion)
    assert(conn.nextOutgoingMessage === serverVersion)

    conn.receiveData(ExitMessage("Bye"))
  } }

/*
  commented out because it fails intermittently - ST 10/26/12

  // the most important test here...
  // this tests makes sure that a if a client sends messages in the correct order
  // that things go smoothly.
  mockTest("complete session"){
    HubNetUtils.viewMirroring = true
    HubNetUtils.plotMirroring = false

    val server = mock[ConnectionManagerInterface]
    val conn = newConnection(server)

    expecting{
      // if the clientType is invalid, we would get a LoginFailure back.
      // in this case, we are testing that it is valid
      // COMPUTER is always a valid type
      one(server).isSupportedClientType(arg("COMPUTER")); willReturn(true)
      // finalizeConnection will check that the user id is valid, and if so
      // register the connection with the ConnectionManager
      one(server).finalizeConnection(arg(conn), arg(clientId)); willReturn(true)
      // ServerSideConnection then asks the ConnectionManager to create the handshake
      // which it will send back to the client.
      one(server).createHandshakeMessage("COMPUTER"); willReturn(HandshakeFromServer("test-model", List()))
      // after a successful login, ServerSideConnection asks the ConnectionManager
      // to do a fullViewUpdate, which sends a ViewUpdate to the client
      // (actually, all clients...but it shouldn't do that.)
      one(server).fullViewUpdate()
      // arguably, all the above calls are implementation details, and it stinks to
      // have to put them here. i don't see a clear workaround just yet. JC 1/3/11

      one(server).putClientData(EnterMessageEnvelope(clientId))

      // this might simulate the user
      for(i<-1 to 10) {
        one(server).putClientData(ActivityMessageEnvelope(clientId, "test-content", "test-tag"))
      }

      one(server).removeClient(clientId, notifyClient = false, "no-reason")

    }
    when{
      // the calls below represent the proper sequence that a client should follow
      // in order to have a complete session.

      // the first message is always the version of the client.
      conn.receiveData(serverVersion)

      // followed by a handshake from the client
      // (assuming that the server sent a good version back)
      conn.receiveData(HandshakeFromClient(clientId, "COMPUTER"))

      // followed by an EnterMessage
      // (arguably, the EnterMessage is not part of the handshake, its just that it lives somewhere in between.
      // i like to think that the EnterMessage are the last step in the handshake
      // and that EnterMessages are finally given to the server to be consumed by model code.
      conn.receiveData(EnterMessage)

      // then a handful of activity messages.
      // these might represent a user clicking on buttons and things.
      for(i<-1 to 10)
        conn.receiveData(ActivityCommand("test-content", "test-tag"))

      // finally, the client exits.
      conn.receiveData(ExitMessage("no-reason"))
    }
  }
*/

  class TestableConnection(streamable: Streamable, server: ConnectionManagerInterface)
    extends ServerSideConnection(streamable, "test:4242", server) {
      override def waitForSendData(a: Any) {
        writeQueue.add(a)
      }
      def nextOutgoingMessage = {
        val m = this.writeQueue.poll()
        if(m==null) throw new IllegalStateException("expected message, but got none!")
        m
      }
      override def disconnect(reason: String): Unit = {
        stopWriting()
      }
  }
  def newConnection(server:ConnectionManagerInterface=mock[ConnectionManagerInterface]) = {
    val streamable=mock[Streamable]
    expecting {
      atLeast(1).of(streamable).getOutputStream
      atLeast(1).of(streamable).getInputStream
      atMost(1).of(streamable).close()
    }
    new TestableConnection(streamable, server)
  }

  trait LoginFailure {
    val server = mock[ConnectionManagerInterface]
    val conn = newConnection(server)

    expecting {
      one(server).removeClient(null, true, "Client login failed, please try logging in again")
      willReturn(false)
    }
  }
}
