// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless.hubnet

import org.nlogo.core.{ DummyCompilationEnvironment, DummyExtensionManager, LiteralParser, Widget => CoreWidget }
import org.nlogo.core.model.WidgetReader
import org.nlogo.api.{ FileIO, ModelSection }
import org.nlogo.fileformat
import org.nlogo.hubnet.protocol.ComputerInterface
import org.nlogo.headless.TestUsingWorkspace

import java.io.{ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import java.nio.file.Paths

import org.nlogo.util.ClassLoaderObjectInputStream

import TestUtils._

import org.scalatest.FunSuite

class TestClientInterface extends TestUsingWorkspace {

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

  testUsingWorkspace("empty ComputerInterface is serializable"){ workspace =>
    val ci = new ComputerInterface(Nil, Nil, Nil)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  testUsingWorkspace("legit ComputerInterface is serializable"){ workspace =>
    import collection.JavaConverters._
    val model = "test/hubnet/client-interface.nlogo"
    val parsedWidgets = getClientWidgets(model, workspace)
    val ci = new ComputerInterface(parsedWidgets,
                                 workspace.world.turtleShapeList.shapes,
                                 workspace.world.linkShapeList.shapes)
    assert(ci.toString === roundTripSerialization(ci).toString)
  }

  private def getClientWidgets(modelFilePath: String, workspace: LiteralParser): Seq[CoreWidget] = {
    fileformat.standardLoader(workspace, new DummyExtensionManager(), new DummyCompilationEnvironment())
      .readModel(Paths.get(modelFilePath).toUri)
      .get
      .optionalSectionValue[Seq[CoreWidget]]("org.nlogo.modelsection.hubnetclient")
      .get
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
