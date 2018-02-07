// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.FunSuite

import java.net.URI
import java.nio.file.Path

import org.nlogo.core.Model
import org.nlogo.fileformat.{ defaultConverter, ConversionError, FailedConversionResult, ModelConversion,
  SuccessfulConversion, ErroredConversion }
import org.nlogo.api.{ ConfigurableModelLoader, ThreeDVersion, TwoDVersion, Version }
import OpenModel.{ CancelOpening, OpenAsSaved, OpenInCurrentVersion, VersionResponse }

class OpenModelTests extends FunSuite {
  val testURI = new URI("file:///foo.test")

  trait OpenTest {
    val uri: URI = testURI
    def modelChanges: Model => Model = identity
    def currentVersion = TwoDVersion.version
    def oldVersion: String = "NetLogo 6.0.2"
    def autoconverter: ModelConversion = defaultConverter

    def userContinuesOpen() = controller.openModel(true)
    def userCancelsOpen() = controller.openModel(false)
    def userGivesVersionResponse(vr: VersionResponse) =
      controller.willGiveVersionResponse = vr

    lazy val controller = new MockController()
    val oldModel = Model(version = currentVersion, turtleShapes = Nil, linkShapes = Nil)
    val newModel = modelChanges(oldModel)
    def format = new MockFormat(newModel, None)
    object VersionInfo extends Version {
      def noVersion: String = currentVersion + " (no version)"
      def version: String = currentVersion
      override def is3D = currentVersion.contains("3D")
      override def knownVersion(v: String) = v == currentVersion || super.knownVersion(v)
      protected def additionalKnownVersions: Seq[String] = Seq()
    }
    lazy val loader = new ConfigurableModelLoader().addFormat[String, MockFormat](format)
    lazy val openedModel = OpenModelFromURI(uri, controller, loader, autoconverter, VersionInfo)
  }

  test("if asked to open a model will a null path, returns none and reports an invalid URI") { new OpenTest {
    override val uri = null
    assert(openedModel.isEmpty)
    assert(controller.invalidURI == null)
  } }

  test("if the model doesn't match an available format, notifies the user it is invalid") { new OpenTest {
    override val uri = new URI("file://foo.jpg")
    assert(openedModel.isEmpty)
    assertResult(uri)(controller.invalidURI)
  } }

  test("if the version doesn't start with NetLogo, notifies the user it is invalid") { new OpenTest {
    override def modelChanges = _.copy(version = "foobarbaz")
    val _ = openedModel
    assertResult(testURI)(controller.invalidURI)
    assertResult("foobarbaz")(controller.invalidVersion)
  } }

  test("if the model is 3D, but NetLogo is open in 2D mode, the user may cancel opening") { new OpenTest {
    override def modelChanges = _.copy(version = ThreeDVersion.version)
    userGivesVersionResponse(CancelOpening)
    assert(openedModel.isEmpty)
  } }

  test("if the model is 3D, but NetLogo is open in 2D mode, notifies the user") { new OpenTest {
    override def modelChanges = _.copy(version = ThreeDVersion.version)
    userGivesVersionResponse(OpenAsSaved)
    assert(openedModel.isDefined)
    assert(controller.notifiedModelArity == 3)
    assert(controller.notifiedModelVersion == ThreeDVersion.version)
    assert(! controller.notifiedVersionUnknown)
    assert(openedModel.get.version == ThreeDVersion.version)
  } }

  test("when in 2D Mode, choosing to open a 3D model using current version adjusts the version") { new OpenTest {
    override def modelChanges = _.copy(version = ThreeDVersion.version)
    userGivesVersionResponse(OpenInCurrentVersion)
    assert(openedModel.isDefined)
    assert(openedModel.get.version == TwoDVersion.version)
  } }

  test("doesn't open different-arity model unless the user approves") { new OpenTest {
    override def modelChanges = _.copy(version = ThreeDVersion.version)
    userCancelsOpen()
    assert(openedModel.isEmpty)
    assert(controller.notifiedModelArity   == 3)
    assert(controller.notifiedModelVersion == ThreeDVersion.version)
  } }

  test("if the model is in 2D, but NetLogo is open in 3D, notifies the user") { new OpenTest {
    override def currentVersion = ThreeDVersion.version
    override def modelChanges = _.copy(version = TwoDVersion.version)
    userGivesVersionResponse(OpenAsSaved)
    assert(openedModel.isDefined)
    assert(controller.notifiedModelArity   == 2)
    assert(controller.notifiedModelVersion == TwoDVersion.version)
    assert(openedModel.get.version         == TwoDVersion.version)
  } }

  test("if the model is not a known version, checks before opening") { new OpenTest {
    override def modelChanges = _.copy(version = "NetLogo 7.0")
    userContinuesOpen()
    assert(openedModel.isDefined)
    assert(controller.notifiedModelVersion == "NetLogo 7.0")
  } }

  test("if the model is not a compatible version, checks before opening") { new OpenTest {
    override def modelChanges = _.copy(version = "NetLogo 4.0")
    userContinuesOpen()
    assert(openedModel.isDefined)
    assert(controller.notifiedModelVersion == "NetLogo 4.0")
  } }

  test("returns the model if it checks out") { new OpenTest {
    assertResult(Some(newModel))(openedModel)
  } }

  test("notifies the user if an error occurs while loading the file") { new OpenTest {
    val exception = new java.io.IOException("file ain't there")
    override def format = new MockFormat(modelChanges(Model()), Some(exception))
    assert(! openedModel.isDefined)
    assert(controller.notifiedException == exception)
  } }

  test("runs the autoconversion on the model before returning it") { new OpenTest {
    override def autoconverter = { (m: Model, p: Path) => SuccessfulConversion(m, m.copy(code = "to foo end")) }
    assert(openedModel.get.code == "to foo end")
  } }

  test("notifies the controller if the autoconversion fails") { new OpenTest {
    val exception = new Exception("problem autoconverting")
    override def autoconverter = { (m: Model, p: Path) => ErroredConversion(m, ConversionError(exception, "", "")) }
    userContinuesOpen()
    assert(openedModel.isDefined)
    assertResult(Some(oldModel))(openedModel)
    assert(controller.notifiedException == exception)
  } }

  test("OpenFromSource opens the model properly") { new OpenTest {
    val modelFromSource = OpenModelFromSource(uri, "model source", controller, loader, autoconverter, VersionInfo)
    assertResult(Some(newModel))(modelFromSource)
  } }
}

class MockController extends OpenModel.Controller {
  var invalidURI: URI = new java.net.URI("")
  var invalidVersion: String = _
  var notifiedModelArity: Int = 0
  var notifiedModelVersion: String = _
  var notifiedException: Exception = _
  var notifiedVersionUnknown: Boolean = false
  var willGiveVersionResponse: VersionResponse = OpenModel.CancelOpening
  var willOpenModel = false

  def openModel(willDo: Boolean): MockController = {
    willOpenModel = willDo
    this
  }

  def errorOpeningURI(uri: URI, exception: Exception): Unit = {
    invalidURI = uri
    notifiedException = exception
  }

  def errorAutoconvertingModel(res: FailedConversionResult): Option[Model] = {
    notifiedException = res.errors.head.errors.head
    if (willOpenModel) Some(res.model)
    else None
  }

  def invalidModel(uri: URI): Unit = { invalidURI = uri }
  def invalidModelVersion(uri: URI, version: String): Unit = {
    invalidURI = uri
    invalidVersion = version
  }
  def shouldOpenModelOfDifferingArity(arity: Int, version: String): VersionResponse = {
    notifiedModelArity = arity
    notifiedModelVersion = version
    willGiveVersionResponse
  }
  def shouldOpenModelOfUnknownVersion(currentVersion: String, openVersion: String): Boolean = {
    notifiedModelVersion = openVersion
    notifiedVersionUnknown = true
    willOpenModel
  }
  def shouldOpenModelOfLegacyVersion(currentVersion: String, openVersion: String): Boolean = {
    notifiedModelVersion = openVersion
    willOpenModel
  }
}
