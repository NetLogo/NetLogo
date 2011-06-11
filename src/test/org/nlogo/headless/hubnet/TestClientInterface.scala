package org.nlogo.headless.hubnet

import java.io._
import org.nlogo.api.{LocalFile, ModelSection, ModelReader, WidgetIO}
import org.nlogo.util.JCL._
import org.nlogo.util.TestUtils._
import org.nlogo.headless.TestUsingWorkspace
import org.nlogo.hubnet.protocol.ClientInterface

class TestClientInterface extends TestUsingWorkspace {

  testUsingWorkspace("empty ClientInterface is serializable"){ workspace =>
    val ci = new ClientInterface(Nil, Nil, Nil)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  testUsingWorkspace("legit ClientInterface is serialiazble"){ workspace =>
    val model = "./models/HubNet Activities/Code Examples/Template.nlogo"
    val widgets = WidgetIO.parseWidgets(getClientWidgets(model))
    val ci = new ClientInterface(widgets.toList,
      toScalaSeq(workspace.world.turtleShapeList.getShapes).toList,
      toScalaSeq(workspace.world.linkShapeList.getShapes).toList)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  private def getClientWidgets(modelFilePath: String) = {
    ModelReader.parseModel(LocalFile.readFile(new File(modelFilePath))).get(ModelSection.CLIENT)
  }

  test("test roundTripSerialization method"){
    assert("s" == roundTripSerialization("s"))
    assert(7.asInstanceOf[AnyRef] == roundTripSerialization(7))
  }
}
