// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import org.scalatest.funsuite.AnyFunSuite

import java.net.URI
import java.nio.file.{ Path, Paths }

import org.nlogo.core.{ Model, View, WorldDimensions, WorldDimensions3D }
import org.nlogo.fileformat.{ ConversionError, FailedConversionResult, FileFormat
                            , SuccessfulConversion, ErroredConversion, NLogoFormat, NLogoThreeDFormat }
import org.nlogo.fileformat.FileFormat.ModelConversion
import org.nlogo.api.{ ConfigurableModelLoader, Version }
import scala.util.{ Success, Try }

class OpenModelTests extends AnyFunSuite {
  val testURI = Paths.get("test/fileformat/non-nlogo-extension.txt").toUri

  trait OpenTest {
    val uri: URI = testURI
    def modelChanges: Model => Model = identity
    def currentVersion = "NetLogo 7.0.0"
    def autoconverter: ModelConversion = FileFormat.defaultConverter

    def userContinuesOpen() = controller.openModel(true)
    def userCancelsOpen() = controller.openModel(false)

    lazy val controller = new MockController()
    def format = new MockFormat(modelChanges(Model()), None)
    def nlogoformat = new NLogoFormat
    def nlogo3dformat = new NLogoThreeDFormat
    object VersionInfo extends Version {
      override def is3D = currentVersion.contains("3D")
      override def knownVersion(v: String) = v == currentVersion || super.knownVersion(v)
    }
    lazy val loader = new ConfigurableModelLoader().addFormat[String, MockFormat](format)
    lazy val openedModel = OpenModelFromURI(uri, controller, loader, autoconverter, VersionInfo)
  }

  test("if asked to open a model will a null path, returns none and reports an invalid URI") { new OpenTest {
    override val uri = null
    assert(openedModel.isEmpty)
    assert(controller.invalidURI == null)
  } }

  test("if the model doesn't exist, doesn't continue loading") { new OpenTest {
    override val uri = Paths.get("test/fileformat/does-not-exist.nlogox").toUri
    assert(openedModel.isEmpty)
  } }

  test("if the model doesn't match an available format, notifies the user it is invalid") { new OpenTest {
    override val uri = Paths.get("test/fileformat/WidgetSection.txt").toUri
    assert(!openedModel.isEmpty)
  } }

  test("if the version doesn't start with NetLogo, notifies the user it is invalid") { new OpenTest {
    override def modelChanges = _.copy(version = "foobarbaz")
    val _ = openedModel
    assertResult(testURI)(controller.invalidURI)
    assertResult("foobarbaz")(controller.invalidVersion)
  } }

  test("if the model is 3D, but NetLogo is open in 2D mode, notifies the user") { new OpenTest {
    override def modelChanges = _.copy(version = "NetLogo 3D 6.3")
    userContinuesOpen()
    assert(openedModel.isDefined)
    assert(controller.notifiedModelArity == 3)
    assert(controller.notifiedModelVersion == "NetLogo 3D 6.3")
    assert(! controller.notifiedVersionUnknown)
  } }

  test("doesn't open different-arity model unless the user approves") { new OpenTest {
    override def modelChanges = _.copy(version = "NetLogo 3D 6.3")
    userCancelsOpen()
    assert(openedModel.isEmpty)
    assert(controller.notifiedModelArity   == 3)
    assert(controller.notifiedModelVersion == "NetLogo 3D 6.3")
  } }

  test("if the model is in 2D, but NetLogo is open in 3D, notifies the user") { new OpenTest {
    override def currentVersion = "NetLogo 3D 7.0.0"
    userContinuesOpen()
    assert(openedModel.isDefined)
    assert(controller.notifiedModelArity   == 2)
    assert(controller.notifiedModelVersion == "NetLogo 7.0.0")
  } }

  test("if the model is not a known version, checks before opening") { new OpenTest {
    override def modelChanges = _.copy(version = "NetLogo 8.0")
    userContinuesOpen()
    assert(openedModel.isDefined)
    assert(controller.notifiedModelVersion == "NetLogo 8.0")
  } }

  test("if the model is not a compatible version, checks before opening") { new OpenTest {
    override def modelChanges = _.copy(version = "NetLogo 4.0")
    userContinuesOpen()
    assert(openedModel.isDefined)
    assert(controller.notifiedModelVersion == "NetLogo 4.0")
  } }

  test("returns the model if it checks out") { new OpenTest {
    assertResult(Some(Model()))(openedModel)
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
    assertResult(Some(Model()))(openedModel)
    assert(controller.notifiedException == exception)
  } }

  test("OpenFromSource opens the model properly") { new OpenTest {
    val modelFromSource = OpenModelFromSource(uri, "model source", controller, loader, autoconverter, VersionInfo)
    assertResult(Some(Model()))(modelFromSource)
  } }

  test("serializes various version in the model") { new OpenTest {
    assert(nlogoformat.version.serialize(new Model()) === Array[String]("NetLogo 7.0.0"))
    assert(nlogoformat.version.serialize(new Model(version = "NetLogo 3D 6.3")) ===
      Array[String]("NetLogo 3D 6.3"))
  } }

  test("serializes various dimensions in the model") { new OpenTest {
    assert((nlogoformat.interfaceComponent.serialize(new Model)) ===
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "1", "1", "1", "ticks", "30.0", ""))
    assert(nlogoformat.interfaceComponent.serialize(new Model(widgets =
      List(View(dimensions = new WorldDimensions(0,0,0,0,12.0,true,true))))) ===
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "1", "1", "1", "ticks", "30.0", ""))
    assert(nlogoformat.interfaceComponent.serialize(new Model(widgets =
      List(View(dimensions = new WorldDimensions3D(0,0,0,0,0,0,12.0,true,true))))) ===
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "1", "1", "1", "ticks", "30.0", ""))
  } }

  test("deserializes various dimensions for models") { new OpenTest {
    val tryModel: Try[Model] = (nlogoformat.interfaceComponent.deserialize(
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "1", "1", "1", "ticks", "30.0", ""))(new Model))
    val tryModel3d: Try[Model] = (nlogo3dformat.interfaceComponent.deserialize(
      Array("GRAPHICS-WINDOW", "0", "0", "5", "5", "-1", "-1", "12.0",
        "1", "13", "1", "1", "1", "0", "1", "1", "1", "0", "0", "0",
        "0", "0", "0", "1", "1", "1", "ticks", "30.0", ""))(new Model))

    /* Determines if view is correctly configured */
    assert(tryModel.get.widgets(0) === View())
    assert(tryModel.get.widgets(0) ===
      View(dimensions = new WorldDimensions(0,0,0,0,12.0,true,true)))
    assert(tryModel3d.get.widgets(0) ===
      View(dimensions = new WorldDimensions3D(0,0,0,0,0,0,12.0,true,true)))
  } }

  test("parses sections from provided string with 2d format") { new OpenTest {
    val sectionString =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val sectionStringWith3D =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 3D 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val section3dString =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 3D 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val section3dStringWith2D =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val model: Try[Model] = nlogoformat.load(sectionString,Seq())
    val model3d: Try[Model] = nlogoformat.load(section3dString,Seq())
    val modelWith3dHeader: Try[Model] = nlogoformat.load(sectionStringWith3D,Seq())
    val model3dWith2dHeader: Try[Model] = nlogoformat.load(section3dStringWith2D,Seq())

    /* Model's dimensions after loading */
    assert(model.flatMap(_ => Success(true)).getOrElse(false))
    assert(model.get.version == "NetLogo 6.0.4")
    assert(model.get.widgets(0) ===
      View(dimensions = new WorldDimensions(-16,16,-16,16,12.0,true,true)))
    assert(model3d.flatMap(_ => Success(false)).getOrElse(true))
    assert(model3dWith2dHeader.flatMap(_ => Success(false)).getOrElse(true))
    assert(modelWith3dHeader.flatMap(_ => Success(false)).getOrElse(true))
  } }

  test("parses sections from provided string with 3d format") { new OpenTest {
    val sectionString =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val sectionStringWith3D =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 3D 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val section3dString =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 3D 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val section3dStringWith2D =
      """@#$#@#$#@
      |GRAPHICS-WINDOW
      |0
      |0
      |5
      |5
      |-1
      |-1
      |12.0
      |1
      |13
      |1
      |1
      |1
      |0
      |1
      |1
      |1
      |-16
      |16
      |-16
      |16
      |-16
      |16
      |1
      |1
      |1
      |ticks
      |30.0
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |NetLogo 6.0.4
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      |@#$#@#$#@
      """.stripMargin
    val model: Try[Model] = nlogo3dformat.load(sectionString,Seq())
    val model3d: Try[Model] = nlogo3dformat.load(section3dString,Seq())
    val modelWith3dHeader: Try[Model] = nlogo3dformat.load(sectionStringWith3D,Seq())
    val model3dWith2dHeader: Try[Model] = nlogo3dformat.load(section3dStringWith2D,Seq())

    /* Model's dimensions after loading */
    assert(model3d.flatMap(_ => Success(true)).getOrElse(false))
    assert(model3d.get.version == "NetLogo 3D 6.0.4")
    assert(model3d.get.widgets(0) ===
      View(dimensions = new WorldDimensions3D(-16,16,-16,16,-16,16,12.0,true,true)))
    assert(model.flatMap(_ => Success(false)).getOrElse(true))
    assert(model3dWith2dHeader.flatMap(_ => Success(false)).getOrElse(true))
    assert(modelWith3dHeader.flatMap(_ => Success(false)).getOrElse(true))
  } }
}

class MockController extends OpenModel.Controller {
  var invalidURI: URI = new java.net.URI("")
  var invalidVersion: String = _
  var notifiedModelArity: Int = 0
  var notifiedModelVersion: String = _
  var notifiedException: Exception = _
  var notifiedVersionUnknown: Boolean = false
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
  def shouldOpenModelOfDifferingArity(arity: Int, version: String): Boolean = {
    notifiedModelArity = arity
    notifiedModelVersion = version
    willOpenModel
  }
  def shouldOpenModelOfUnknownVersion(version: String): Boolean = {
    notifiedModelVersion = version
    notifiedVersionUnknown = true
    willOpenModel
  }
  def shouldOpenModelOfLegacyVersion(version: String): Boolean = {
    notifiedModelVersion = version
    willOpenModel
  }
}
