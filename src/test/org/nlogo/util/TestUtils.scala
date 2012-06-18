// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import java.io.{ByteArrayInputStream, ObjectOutputStream, ByteArrayOutputStream}
import org.scalatest.FunSuite

// this object contains utility functions usable by other tests
object TestUtils {

  import org.scalatest.Assertions._

  class RoundTrip[T](t: T) {
    def writeThenRead: T = roundTripSerialization(t)
    def isSerializable = roundTripSerialization(t) === t
  }
  implicit def AnyToRoundTrip[T](t: T) = new RoundTrip(t)

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
