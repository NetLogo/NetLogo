// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.hubnet

import java.io.{ ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream }
import java.nio.file.Paths

import org.nlogo.core.{ LiteralParser, Widget => CoreWidget }
import org.nlogo.fileformat.{ FileFormat, NLogoFormat, NLogoHubNetFormat }
import org.nlogo.headless.TestUsingWorkspace
import org.nlogo.hubnet.protocol.ComputerInterface

import org.nlogo.util.{ AnyFunSuiteEx, ClassLoaderObjectInputStream }

class TestClientInterface extends TestUsingWorkspace {

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

  testUsingWorkspace("empty ComputerInterface is serializable"){ workspace =>
    val ci = new ComputerInterface(Nil, Nil, Nil)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  testUsingWorkspace("legit ComputerInterface is serializable"){ workspace =>
    val model = "test/hubnet/client-interface.nlogo"
    val parsedWidgets = getClientWidgets(model, workspace)
    val ci = new ComputerInterface(parsedWidgets,
                                 workspace.world.turtleShapeList.shapes,
                                 workspace.world.linkShapeList.shapes)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  private def getClientWidgets(modelFilePath: String, workspace: LiteralParser): Seq[CoreWidget] = {
    FileFormat.basicLoader
      .addSerializer[Array[String], NLogoFormat](new NLogoHubNetFormat(workspace))
      .readModel(Paths.get(modelFilePath).toUri)
      .get
      .optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient")
      .get
  }

  test("test roundTripSerialization method"){
    assert("s" == roundTripSerialization("s"))
    assert(7 == roundTripSerialization(7))
  }
}

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
class TestUtilsTests extends AnyFunSuiteEx {
  import TestUtils._

  // fails in sbt unless we use ClassLoaderObjectInputStream instead of a regular ObjectInputStream
  // in the roundTripSerialization.
  // we think its because ObjectInputStream is using the bootstrap classloader. JC - 9/8/10
  test("roundTripSerialization") {
    assert("hello".isSerializable)
    assert(7.isSerializable)
  }
}
