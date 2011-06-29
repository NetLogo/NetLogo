package org.nlogo.hubnet.protocol

import org.scalatest.FunSuite
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, DataOutputStream, DataInputStream}
import org.nlogo.api.LogoList
import org.nlogo.hubnet.connection.ClientRoles

/**
 todo:
  val VIEW_UPDATE = 5
  val PLOT_CONTROL = 6
  val PLOT_UPDATE = 7
  val OVERRIDE = 8
  val AGENT_PERSPECTIVE = 10
 */
class BinaryProtocolTests extends FunSuite {

  import BinaryProtocol._

  testMessage(HandshakeFromServer("test.nlogo", LogoList()))
  testMessage(VersionMessage("NetLogo 5.1"))

  testMessage(ActivityCommand("Button", "go", false.asInstanceOf[AnyRef]))
  testMessage(ActivityCommand("Slider", "sl", 2.0.asInstanceOf[AnyRef]))
  testMessage(ActivityCommand("Switch", "sw", true.asInstanceOf[AnyRef]))
  testMessage(ActivityCommand("Chooser", "c", "hey!"))

  testMessage(WidgetControl(2.0.asInstanceOf[AnyRef], "sl"))
  testMessage(WidgetControl(true.asInstanceOf[AnyRef], "sw"))
  testMessage(WidgetControl("hey!", "c"))

  testMessage(EnterMessage("userId", "clientType", ClientRoles.Participant))

  testMessage(LoginFailure("The server doesn't like you."))
  testMessage(ExitMessage("Well I don't like the server."))

  testMessage(InvalidMessage("wrong answer, pal", WidgetControl("hey!", "c")))

  testMessage(Text("hi", Text.MessageType.USER))
  testMessage(Text("ih", Text.MessageType.TEXT))
  testMessage(Text("oo", Text.MessageType.CLEAR))

  testMessage(DisableView)
  testMessage(ClearOverrideMessage)

  def testMessage(messageGoingOut:Message) {
    test(messageGoingOut.toString){
      val bytes = new ByteArrayOutputStream()
      writeMessage(messageGoingOut, new DataOutputStream(bytes))
      val messageReadIn = readMessage(new DataInputStream(new ByteArrayInputStream(bytes.toByteArray)))
      //println(messageGoingOut + "\n" + messageReadIn)
      assert(messageGoingOut === messageReadIn)
    }
  }
}


object BinaryProtocolTests {
  def main(args:Array[String]){
    val ss = new java.net.ServerSocket(9999)
    val s = ss.accept()
    val in = new DataInputStream(s.getInputStream)
    val protocol = {
      val protocolSize = in.readInt()
      val bytes = new Array[Byte](protocolSize)
      in.read(bytes)
      new String(bytes).trim
    }
    assert(protocol == "binary")
    while(true){
      println(BinaryProtocol.readMessage(in))
    }
  }
}