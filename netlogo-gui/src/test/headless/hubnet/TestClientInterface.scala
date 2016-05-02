// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.hubnet

import org.nlogo.api.{ FileIO, LocalFile, ModelSection, ModelReader}
import org.nlogo.headless.TestUsingWorkspace
import org.nlogo.hubnet.protocol.ClientInterface
import org.nlogo.util.ClassLoaderObjectInputStream

import TestUtils._

import org.scalatest.FunSuite

class TestClientInterface extends TestUsingWorkspace {

  testUsingWorkspace("empty ClientInterface is serializable"){ workspace =>
    val ci = new ClientInterface(Nil, Nil, Nil, Nil, workspace)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  testUsingWorkspace("legit ClientInterface is serialiazble"){ workspace =>
    import collection.JavaConverters._
    val model = "test/hubnet/client-interface.nlogo"
    val unparsedWidgets = getClientWidgets(model)
    val parsedWidgets = ModelReader.parseWidgets(unparsedWidgets).asScala.map(_.asScala.toList).toList
    val ci = new ClientInterface(parsedWidgets, unparsedWidgets.toList,
                                 workspace.world.turtleShapeList.shapes,
                                 workspace.world.linkShapeList.shapes,
                                 workspace)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  private def getClientWidgets(modelFilePath: String) = {
    ModelReader.parseModel(FileIO.file2String(modelFilePath)).get(ModelSection.HubNetClient)
  }

  test("test roundTripSerialization method"){
    assert("s" == roundTripSerialization("s"))
    assert(7.asInstanceOf[AnyRef] == roundTripSerialization(7))
  }
}

import java.io.{ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}

// this object contains utility functions usable by other tests
object TestUtils {

  import org.scalatest.Assertions._

  implicit class RoundTrip[T](t: T) {
    def writeThenRead: T = roundTripSerialization(t)
    def isSerializable = roundTripSerialization(t) === t
  }

  def roundTripSerialization[T](t: T) = {
    val bytes = new ByteArrayOutputStream()
    val out = new ObjectOutputStream(bytes)
    out.writeObject(t)
    out.flush()
    val in = ClassLoaderObjectInputStream(
      Thread.currentThread.getContextClassLoader,
      new ByteArrayInputStream(bytes.toByteArray))
    in.readObject().asInstanceOf[T]
  }
}

// this class tests the utility functions in TestUtils.
class TestUtilsTests extends FunSuite {
  import TestUtils._

  // fails in sbt unless we use ClassLoaderObjectInputStream instead of a regular ObjectInputStream
  // in the roundTripSerialization.
  // we think its because ObjectInputStream is using the bootstrap classloader. JC - 9/8/10
  test("roundTripSerialization") {
    assert("hello".isSerializable)
    assert(7.isSerializable)
  }
}
