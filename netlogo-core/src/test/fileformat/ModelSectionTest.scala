// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.scalatest.FunSuite

import org.nlogo.core.Model

import org.nlogo.api.{ ComponentSerialization, ModelFormat }

import scala.util.{ Failure, Success }
import scala.reflect.ClassTag

trait ModelSectionTest[A, B <: ModelFormat[A, B], C] extends FunSuite {
  def subject: ComponentSerialization[A, B]

  def modelComponent(model: Model): C

  def compareSerialized(a: A, otherA: A): Boolean = a == otherA

  def displaySerialized(a: A): String = a.toString

  def minimizeSerializedDiff(a1: A, a2: A): (A, A) = (a1, a2)

  def attachComponent(b: C): Model

  def testDeserializationError[D <: Exception](description: String, serializedVersion: A)(implicit ct: ClassTag[D]): Unit = {
    test(s"errors when deserializing $description") {
      val s = subject
      s.deserialize(serializedVersion)(new Model()) match {
        case Failure(ct(ex)) =>
        case Success(_) => fail("expected exception, but deserialization succeeded")
        case Failure(wrong) => fail(s"expected exception of type ${ct.runtimeClass.getName}, but got one of type ${wrong.getClass.getName}")
      }
    }
  }

  def testDeserializes(description: String, serializedVersion: A, deserializedVersion: C, display: C => String = (_.toString)): Unit = {
    test(s"deserializes $description") {
      val s = subject
      val m = s.deserialize(serializedVersion)(new Model()).get
      val component = modelComponent(m)
      assert(deserializedVersion == component, display(deserializedVersion) + " did not equal " + display(component))
    }
  }

  def testSerializes(description: String, deserializedVersion: C, serializedVersion: A, display: C => String = (_.toString)): Unit = {
    test(s"serializes $description") {
      val s = subject
      val model = attachComponent(deserializedVersion)
      val serialized = s.serialize(model)
      val (min1, min2) = minimizeSerializedDiff(serialized, serializedVersion)
      assert(compareSerialized(serialized, serializedVersion), displaySerialized(serialized) + " did not equal " + displaySerialized(serializedVersion) + "\ndiff expected:\n" + displaySerialized(min2) + "\ndiff actual:\n" + displaySerialized(min1))
    }
  }

  def testErrorsOnDeserialization(description: String, serializedVersion: A, error: String): Unit = {
    test(s"errors when deserializing $description") {
      val s = subject
      val m = s.deserialize(serializedVersion)(new Model())
      assert(m.isFailure)
      assert(m.failed.get.getMessage.contains(error))
    }
  }

  def testRoundTripsSerialForm(description: String, serializedVersion: A): Unit = {
    test(s"round-trips $description from serial form to object and back") {
      val s = subject
      val m = s.deserialize(serializedVersion)(new Model()).get
      val reserialized = s.serialize(m)
      val (min1, min2) = minimizeSerializedDiff(reserialized, serializedVersion)
      assert(compareSerialized(serializedVersion, reserialized), displaySerialized(reserialized) + " was expected to equal " + displaySerialized(serializedVersion) + "\ndiff expected:\n" + displaySerialized(min1) + "\ndiff actual:\n" + displaySerialized(min2))
    }
  }

  def testRoundTripsObjectForm(description: String, deserializedVersion: C): Unit = {
    test(s"round-trips $description to serial form and back to object") {
      val s = subject
      val m = attachComponent(deserializedVersion)
      val serialized = s.serialize(m)
      val redeserialized = modelComponent(s.deserialize(serialized)(new Model()).get)
      assert(deserializedVersion == redeserialized)
    }
  }

  def testAltersObjectRepresentation(description: String, deserializedVersion: C, alteredVersion: C): Unit = {
    test(s"changes object by $description") {
      val s = subject
      val m = attachComponent(deserializedVersion)
      val serialized = s.serialize(m)
      val redeserialized = modelComponent(s.deserialize(serialized)(new Model()).get)
      assert(redeserialized == alteredVersion)
    }
  }
}
