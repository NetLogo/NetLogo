// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.scalatest.FunSuite

import org.nlogo.core.Model

import org.nlogo.api.{ ComponentSerialization, ModelFormat, ModelSection, Version, VersionHistory }

trait ModelSectionTest[A, B <: ModelFormat[A, _], C] extends FunSuite {
  def subject: ComponentSerialization[A, B]

  def modelComponent(model: Model): C

  def compareSerialized(a: A, otherA: A): Boolean = a == otherA

  def displaySerialized(a: A): String = a.toString

  def attachComponent(b: C): Model

  def testDeserializationError[D <: Exception](description: String, serializedVersion: A)(implicit exceptionManifest: Manifest[D]): Unit = {
    test(s"errors when deserializing $description") {
      intercept[D] {
        val s = subject
        s.deserialize(serializedVersion)(new Model())
      }
    }
  }

  def testDeserializes(description: String, serializedVersion: A, deserializedVersion: C): Unit = {
    test(s"deserializes $description") {
      val s = subject
      val m = s.deserialize(serializedVersion)(new Model())
      val component = modelComponent(m)
      assert(deserializedVersion == component)
    }
  }

  def testRoundTripsSerialForm(description: String, serializedVersion: A): Unit = {
    test(s"round-trips $description from serial form to object and back") {
      val s = subject
      val m = s.deserialize(serializedVersion)(new Model())
      val deserialized = modelComponent(m)
      val reserialized = s.serialize(m)
      assert(compareSerialized(serializedVersion, reserialized), s"${displaySerialized(reserialized)} was expected to equal ${displaySerialized(serializedVersion)}")
    }
  }

  def testRoundTripsObjectForm(description: String, deserializedVersion: C): Unit = {
    test(s"round-trips $description to serial form and back to object") {
      val s = subject
      val m = attachComponent(deserializedVersion)
      val serialized = s.serialize(m)
      val redeserialized = modelComponent(s.deserialize(serialized)(new Model()))
      assert(deserializedVersion == redeserialized)
    }
  }
}
