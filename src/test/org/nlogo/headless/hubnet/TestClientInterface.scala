// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.hubnet

import org.nlogo.api.{LocalFile, ModelSection, ModelReader, WidgetIO}
import org.nlogo.util.TestUtils._
import org.nlogo.headless.TestUsingWorkspace
import org.nlogo.hubnet.protocol.ClientInterface

class TestClientInterface extends TestUsingWorkspace {

  testUsingWorkspace("empty ClientInterface is serializable"){ workspace =>
    val ci = new ClientInterface(Nil, Nil, Nil)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  testUsingWorkspace("legit ClientInterface is serialiazble"){ workspace =>
    import collection.JavaConverters._
    val model = "test/hubnet/client-interface.nlogo"
    val widgets = WidgetIO.parseWidgets(getClientWidgets(model))
    val ci = new ClientInterface(widgets.toList,
      workspace.world.turtleShapeList.getShapes.asScala.toList,
      workspace.world.linkShapeList.getShapes.asScala.toList)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  private def getClientWidgets(modelFilePath: String) = {
    ModelReader.parseModel(new LocalFile(modelFilePath).readFile()).get(ModelSection.HubNetClient)
  }

  test("test roundTripSerialization method"){
    assert("s" == roundTripSerialization("s"))
    assert(7.asInstanceOf[AnyRef] == roundTripSerialization(7))
  }
}
