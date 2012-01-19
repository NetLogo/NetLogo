// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.server

import org.nlogo.hubnet.protocol._
import org.nlogo.util.MockSuite
import org.nlogo.api.{LogoList, Version}
import org.nlogo.hubnet.connection.{ClientRole , ConnectionTypes , Streamable}
import org.nlogo.hubnet.connection.MessageEnvelope.{EnterMessageEnvelope, ActivityMessageEnvelope}

// tests for the hubnet session behavior on the server side.
class ServerSideConnectionTests extends MockSuite {

  val clientId = "test-user"

  mockTest("Server is initially in AwaitingVersionNumber state.") {
    val conn = newConnection()
    assert(conn.getCurrentState === ConnectionStates.AwaitingVersionNumber)
  }

  mockTest("Server transitions to AwaitingEnterMessage state upon receiving the correct version number from the client.") {
    val conn = newConnection()
    conn.receiveData(Version.version)
    assert(conn.getCurrentState === ConnectionStates.AwaitingEnterMessage)
  }

  mockTest("Server responds with version number after receiving the correct version number from the client"){
    val conn = newConnection()
    conn.receiveData(Version.version)
    assert(conn.nextOutgoingMessage === Version.version)
  }

  mockTest("When client/server versions don't match, server should respond with LoginFailure and go to Disconnected state"){
    val conn = newConnection()
    conn.receiveData("NetLogo 1.0")

    val error = "The version of the HubNet Client you are using does not " +
                "match the version of the server.  Please use the HubNet Client that comes with " + Version.version
    conn.nextOutgoingMessage match {
      case LoginFailure(reason) => assert(reason === error)
      case _ => fail("expected LoginFailure")
    }

    assert(conn.getCurrentState === ConnectionStates.Disconnected)
  }

  mockTest("send correct version, but invalid client type - server should still allow you to log in"){
    HubNetUtils.viewMirroring = false
    val server = mock[ConnectionManagerInterface]
    val conn = newConnection(server)

    expecting {
      one(server).finalizeConnection(arg(conn), arg(clientId)); willReturn(true)
      one(server).createHandshakeMessage(ConnectionTypes.COMP_CONNECTION); willReturn(HandshakeFromServer("test-model", LogoList()))
    }

    conn.receiveData(Version.version)

    conn.nextOutgoingMessage  // should be the version number, but that's tested elsewhere, so ignore

    conn.receiveData(EnterMessage(clientId, ConnectionTypes.COMP_CONNECTION,
      ClientRole.Participant))

    assert(conn.nextOutgoingMessage === HandshakeFromServer("test-model", LogoList()))
  }


  // the most important test here...
  // this tests makes sure that a if a client sends messages in the correct order
  // that things go smoothly.
  mockTest("complete session"){
    HubNetUtils.viewMirroring = true
    HubNetUtils.plotMirroring = false

    val server = mock[ConnectionManagerInterface]
    val conn = newConnection(server)

    expecting{
      // finalizeConnection will check that the user id is valid, and if so
      // register the connection with the ConnectionManager
      one(server).finalizeConnection(arg(conn), arg(clientId)); willReturn(true)
      // ServerSideConnection then asks the ConnectionManager to create the handshake
      // which it will send back to the client.
      one(server).createHandshakeMessage("COMPUTER"); willReturn(HandshakeFromServer("test-model", LogoList()))
      // after a successful login, ServerSideConnection asks the ConnectionManager
      // to do a fullViewUpdate, which sends a ViewUpdate to the client
      // (actually, all clients...but it shouldn't do that.)
      one(server).fullViewUpdate()
      // arguably, all the above calls are implementation details, and it stinks to
      // have to put them here. i don't see a clear workaround just yet. JC 1/3/11

      // this might simulate the user
      for(i<-1 to 10) {
        one(server).putClientData(ActivityMessageEnvelope(clientId, WidgetTypes.Button, "test-content", "test-tag"))
      }

      one(server).removeParticipantClient(clientId, notifyClient = false, "no-reason")

    }
    when{
      // the calls below represent the proper sequence that a client should follow
      // in order to have a complete session.

      // the first message is always the version of the client.
      conn.receiveData(Version.version)

      // followed by an enter message from the client
      // (assuming that the server sent a good version back)
      conn.receiveData(EnterMessage(clientId, ConnectionTypes.COMP_CONNECTION, ClientRole.Participant))

      // then a handful of activity messages.
      // these might represent a user clicking on buttons and things.
      for(i<-1 to 10)
        conn.receiveData(ActivityCommand(WidgetTypes.Button, "test-content", "test-tag"))

      // finally, the client exits.
      conn.receiveData(ExitMessage("no-reason"))
    }
  }

  mockTest("Controller client logging in") {
    HubNetUtils.viewMirroring = true
    HubNetUtils.plotMirroring = false

    val server = mock[ConnectionManagerInterface]
    val conn = newConnection(server)

    expecting {
      one(server).finalizeControllerClientConnection(arg(conn))
      one(server).createControllerClientHandshakeMessage; willReturn(HandshakeFromServer("test-model", LogoList()))
      one(server).fullViewUpdate()
    }
    when {
      conn.receiveData(Version.version)
      conn.receiveData(EnterMessage(clientId, ConnectionTypes.COMP_CONNECTION,
        ClientRole.Controller))
    }

    conn.nextOutgoingMessage  // should be the version number, but that's tested elsewhere, so ignore

    assert(conn.nextOutgoingMessage === HandshakeFromServer("test-model", LogoList()))
  }

  // error: a mock with name streamable already exists
  mockTest("Multiple controller clients logging in") {
    HubNetUtils.viewMirroring = true
    HubNetUtils.plotMirroring = false

    val server = mock[ConnectionManagerInterface]

    // Let's try 7 controller clients
    val numControllers = 7
    val conns = for( i <- 1 to numControllers ) yield newConnection(server)
    val clientIds = for( i <- 1 to numControllers ) yield "controller" + i.toString

    expecting {
      conns.foreach(conn => one(server).finalizeControllerClientConnection(arg(conn)))
      exactly(numControllers).of(server).createControllerClientHandshakeMessage;
        willReturn(HandshakeFromServer("test-model", LogoList()))
      exactly(numControllers).of(server).fullViewUpdate()
    }

    conns.foreach(conn => conn.receiveData(Version.version))

    conns.foreach(conn => conn.nextOutgoingMessage)  // These should all be version numbers, but ignore them here

    for( i <- 0 to (numControllers-1) ) {
      conns(i).receiveData(EnterMessage(clientIds(i), ConnectionTypes.COMP_CONNECTION,
        ClientRole.Controller))
      assert(conns(i).nextOutgoingMessage === HandshakeFromServer("test-model", LogoList()))
    }
  }



  // TODO: Here are all the things we haven't tested yet in here, but probably should.
  //
  // Receiving & processing messages from multiple teacher clients. (There's a test for multiple teacher
  // clients logging in at once, but that's probably not enough.) Examples:
  // 1. Teacher sends a slider message - test that the proper interface global changed on the server.
  //
  // Changes on the server should be echoed back to the teacher client (e.g., slider changed on the server ->
  // teacher client should receive a WidgetControl message with the new value).
  //
  // Test proper disconnect at every state in ServerSideConnection. Where appropriate, make sure:
  // 1. Client gets removed from clients list (participant and controller)
  // 2. Input/output streams are closed.
  // 3. Server properly transitions to Disconnected state.
  //
  // Bogus messages from teacher client should not cause server to crash. Also, server should continue
  // to receive and process messages from teacher client after the bogus message. (Also, maybe there
  // should be some sort of notification sent to teacher client).
  //
  // Make sure regular clients and participant clients can coexist and their messages get properly
  // distinguished.


  def newConnection(server:ConnectionManagerInterface=mock[ConnectionManagerInterface]) = {
    val streamable=mock[Streamable]

    ignoring (streamable).close()

    expecting {
      one(streamable).getOutputStream
      one(streamable).getInputStream
    }

    new ServerSideConnection(streamable, "test:4242", server){
      def nextOutgoingMessage = {
        val m = this.writeQueue.poll()
        if(m==null) throw new IllegalStateException("expected message, but got none!")
        m
      }
    }
  }
}
