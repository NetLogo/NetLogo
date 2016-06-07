// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.FunSuite

import java.net.URI
import org.nlogo.core.{ Shape, Model, Widget }, Shape.{ LinkShape, VectorShape }
import org.nlogo.api.{ ComponentSerialization, ConfigurableModelLoader, ModelFormat, Version }

import scala.util.{ Failure, Success, Try }

class OpenModelTests extends FunSuite {
  val testURI = new URI("file:///foo.test")

  def testOpenModel(uri: URI = testURI,
    modelChanges: Model => Model = identity,
    userActions: MockController => MockController = identity,
    currentVersion: String = "NetLogo 6.0",
    error: Option[Exception] = None
    )(
    assertions: (Option[Model], MockController) => Unit): Unit = {
      val controller = userActions(new MockController())
      val format = new MockFormat(modelChanges(Model()), error)
      object VersionInfo extends Version {
        override def is3D = currentVersion.contains("3D")
        override def knownVersion(v: String) = {
          v == currentVersion || super.knownVersion(v)
        }
      }
      val loader = new ConfigurableModelLoader()
        .addFormat[String, MockFormat](format)
      val res = OpenModel(uri, controller, loader, VersionInfo)
      assertions(res, controller)
  }

  test("if asked to open a model will a null path, returns none and reports an invalid URI") {
    testOpenModel(uri = null) { (model, controller) =>
      assert(model.isEmpty)
      assert(controller.invalidURI == null)
    }
  }

  test("if the model doesn't match an available format, notifies the user it is invalid") {
    val uri = new URI("file://foo.jpg")
    testOpenModel(uri = uri) { (model, controller) =>
      assert(model.isEmpty)
      assertResult(uri)(controller.invalidURI)
    }
  }
  test("if the version doesn't start with NetLogo, notifies the user it is invalid") {
    testOpenModel(modelChanges = _.copy(version = "foobarbaz")) { (model, controller) =>
      assertResult(testURI)(controller.invalidURI)
      assertResult("foobarbaz")(controller.invalidVersion)
    }
  }
  test("if the model is 3D, but NetLogo is open in 2D mode, notifies the user") {
    testOpenModel(modelChanges = _.copy(version = "NetLogo 3D 6.0"),
      userActions = _.openModel(true)) { (model, controller) =>
        assert(controller.notifiedModelArity == 3)
        assert(controller.notifiedModelVersion == "NetLogo 3D 6.0")
        assert(model.isDefined)
      }
  }
  test("doesn't open different-arity model unless the user approves") {
    testOpenModel(modelChanges = _.copy(version = "NetLogo 3D 6.0"),
      userActions = _.openModel(false)) { (model, controller) =>
        assert(controller.notifiedModelArity == 3)
        assert(controller.notifiedModelVersion == "NetLogo 3D 6.0")
        assert(model.isEmpty)
      }
  }
  test("if the model is in 2D, but NetLogo is open in 3D, notifies the user") {
    testOpenModel(currentVersion = "NetLogo 3D 6.0",
      userActions = _.openModel(true)) { (model, controller) =>
        assert(controller.notifiedModelArity == 2)
        assert(controller.notifiedModelVersion == "NetLogo 6.0")
        assert(model.isDefined)
      }
  }
  test("if the model is not a known version, checks before opening") {
    testOpenModel(
      modelChanges = _.copy(version = "NetLogo 7.0"),
      userActions = _.openModel(true)) { (model, controller) =>
        assert(controller.notifiedModelVersion == "NetLogo 7.0")
        assert(model.isDefined)
      }
  }
  test("if the model is not a compatible version, checks before opening") {
    testOpenModel(
      modelChanges = _.copy(version = "NetLogo 4.0"),
      userActions = _.openModel(true)) { (model, controller) =>
        assert(controller.notifiedModelVersion == "NetLogo 4.0")
        assert(model.isDefined)
      }
  }
  test("returns the model if it checks out") {
    testOpenModel() { (model, controller) =>
      assertResult(Some(Model()))(model)
    }
  }
  test("notifies the user if an error occurs while loading the file") {
    val exception = new java.io.IOException("file ain't there")
    testOpenModel(error = Some(exception)) { (model, controller) =>
      assert(! model.isDefined)
      assert(controller.notifiedException == exception)
    }
  }
}

class MockController extends OpenModel.Controller {
  var invalidURI: URI = _
  var invalidVersion: String = _
  var notifiedModelArity: Int = 0
  var notifiedModelVersion: String = _
  var notifiedException: Exception = _
  var willOpenModel = false

  def openModel(willDo: Boolean): MockController = {
    willOpenModel = willDo
    this
  }

  def errorOpeningURI(uri: URI, exception: Exception): Unit = {
    invalidURI = uri
    notifiedException = exception
  }

  def invalidModel(uri: URI): Unit = { invalidURI = uri }
  def invalidModelVersion(uri: URI, version: String): Unit = {
    invalidURI = uri
    invalidVersion = version
  }
  def shouldOpenModelOfDifferingArity(arity: Int, version: String): Boolean = {
    notifiedModelArity = arity
    notifiedModelVersion = version
    willOpenModel
  }
  def shouldOpenModelOfUnknownVersion(version: String): Boolean = {
    notifiedModelVersion = version
    willOpenModel
  }
  def shouldOpenModelOfLegacyVersion(version: String): Boolean = {
    notifiedModelVersion = version
    willOpenModel
  }
}
