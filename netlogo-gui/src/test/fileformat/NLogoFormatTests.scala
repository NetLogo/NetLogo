// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Arrays

import org.scalatest.FunSuite

import org.nlogo.api.{ ComponentSerialization, ConfigurableModelLoader, ModelLoader, NetLogoLegacyDialect, Version }
import org.nlogo.core.{ DummyCompilationEnvironment, DummyExtensionManager, Femto, LiteralParser, Model, Shape, Widget },
  Shape.{ LinkShape, VectorShape }

import scala.collection.JavaConverters._
import scala.util.Try

abstract class NLogoFormatTest[A] extends ModelSectionTest[Array[String], NLogoFormat, A] {
  val extensionManager = new DummyExtensionManager()
  val compilationEnvironment = new DummyCompilationEnvironment()

  def nlogoFormat = new NLogoFormat

  override def compareSerialized(a: Array[String], otherA: Array[String]): Boolean = {
    Arrays.deepEquals(a.asInstanceOf[Array[Object]], otherA.asInstanceOf[Array[Object]])
  }

  override def displaySerialized(a: Array[String]): String =
    a.mkString(s"Array[${a.length}](", ",\n", ")")
}

class NLogoFormatIOTest extends FunSuite {
  lazy val modelsLibrary = System.getProperty("netlogo.models.dir", "models")

  val extensionManager = new DummyExtensionManager()
  val compilationEnvironment = new DummyCompilationEnvironment()

  val format = new NLogoFormat

  lazy val antsBenchmarkPath = Paths.get(modelsLibrary, "test", "benchmarks", "Ants Benchmark.nlogo")
  // sanity checking, if these fail NetLogo will be pretty unusable
  test("fails when reading in sections from a bad URI") {
    assert(format.sections(new URI("file:///not-a-real-file")).isFailure)
  }
  test("reads in sections from a given URI") {
    assert(format.sections(antsBenchmarkPath.toUri).isSuccess)
  }
  test("saves specified sections to a given URI") {
    val sections =
      format.sections(antsBenchmarkPath.toUri).get
    val pathToWrite = Paths.get("tmp", "AntsCopy"+ System.currentTimeMillis +".nlogo")
    val result = format.writeSections(sections, pathToWrite.toUri)
    assert(result.isSuccess)
    assert(Paths.get(result.get).toAbsolutePath == pathToWrite.toAbsolutePath)
    assert(Files.readAllLines(Paths.get(result.get)).asScala.mkString("\n") == Files.readAllLines(antsBenchmarkPath).asScala.mkString("\n"))
  }

  test("Invalid NetLogo file gives an error about section count") {
    val xmlResult = format.sectionsFromSource("to foo end")
    assert(xmlResult.isFailure)
    assert(xmlResult.failed.get.getMessage.contains("sections"))
  }

  test("XML file gives an error suggesting invalid format") {
    val xmlResult = format.sectionsFromSource("""<?xml version="1.0">""")
    assert(xmlResult.isFailure)
    assert(xmlResult.failed.get.getMessage.contains("nlogo"))
  }
}

class NLogoFormatConversionTest extends FunSuite with ConversionHelper {
  import AutoConversionList.ConversionList

  def testLoader: ModelLoader =
    new ConfigurableModelLoader().addFormat[Array[String], NLogoFormat](new NLogoFormat)
      .addSerializer[Array[String], NLogoFormat](new NLogoLabFormat(literalParser))

  def tryReadAndConvertModel(m: Model, conversions: Seq[ConversionSet]): ConversionResult = {
    val loader = testLoader
    val readModel = loader.readModel(loader.sourceString(m, "nlogo").get, "nlogo").get
    tryConvert(readModel, conversions: _*)
  }

  def readAndConvertModel(m: Model, conversions: Seq[ConversionSet]): Model =
    tryReadAndConvertModel(m, conversions).model

  test("performs listed autoconversions") {
    val m = Model(code = "to foo fd 1 end")
    val conversions = Seq(conversion(codeTabConversions = Seq(_.replaceCommand("fd" -> "forward {0}")), targets = Seq("fd")))
    val rereadModel = readAndConvertModel(m, conversions)
    assertResult("to foo forward 1 end")(rereadModel.code)
  }

  test("carries out conversions on behaviorspace operations") {
    import org.nlogo.api.LabProtocol
    val protocol = new LabProtocol("foo", "", "movie-grab-view", "", 0, true, false, 0, "", List(), List())
    val m = Model(code = "to foo end", version = "NetLogo 5.2.1")
      .withOptionalSection("org.nlogo.modelsection.behaviorspace", Some(Seq(protocol)), Seq())

    val conversions = AutoConversionList.conversions.map(_._2)
    val rereadModel = readAndConvertModel(m, conversions)
    assertResult("vid:record-view")(
      rereadModel.optionalSectionValue[Seq[LabProtocol]]("org.nlogo.modelsection.behaviorspace").get.head.goCommands)
    assert(rereadModel.code.contains("extensions [vid]"))
  }

  test("returns a failure for a model needing conversion which doesn't compile") {
    val m = Model(code = "to foo hsb")
    val conversions = AutoConversionList.conversions.map(_._2)
    val rereadModel = tryReadAndConvertModel(m, conversions)
    assert(rereadModel.hasErrors)
  }
}

class CodeComponentTest extends NLogoFormatTest[String] {
  def subject: ComponentSerialization[Array[String], NLogoFormat] =
    nlogoFormat.CodeComponent

  def modelComponent(model: Model): String = model.code

  def attachComponent(b: String): Model = Model(code = b)

  testDeserializes("empty code section to empty string", Array[String](), "")
  testRoundTripsSerialForm("single line of code", Array[String]("breed [foos foo]"))
  testRoundTripsObjectForm("empty line code", "")
  testRoundTripsObjectForm("single line of code", "breed [ foos foo ]")
}

class InfoComponentTest extends NLogoFormatTest[String] {
  def subject = nlogoFormat.InfoComponent
  def modelComponent(model: Model): String = model.info
  def attachComponent(b: String): Model = Model(info = b)

  testDeserializes("empty info section to empty string", Array[String](), "")
  testRoundTripsSerialForm("single line of info tab", Array[String]("## About this model"))
  testRoundTripsObjectForm("empty info tab", "")
}

class VersionComponentTest extends NLogoFormatTest[String] {
  def subject = nlogoFormat.VersionComponent
  def modelComponent(model: Model): String = model.version
  def attachComponent(b: String): Model = Model(version = b)

  // this test assumes the 2D Format
  val correctArityFormat = Version.version.replaceAll(" 3D", "")
  val wrongArityVersion = Version.version.replaceAll("NetLogo", "NetLogo 3D")

  testErrorsOnDeserialization("wrong arity", Array[String](wrongArityVersion), wrongArityVersion)
  testErrorsOnDeserialization("empty version section to empty version", Array[String](), "")
  // up to the other components to error if they detect a problem
  testDeserializes("unknown version", Array[String]("NetLogo 4D 8.9"), "NetLogo 4D 8.9")
  testDeserializes("multiple version lines to correct version", Array[String]("", correctArityFormat), correctArityFormat)
}

class InterfaceComponentTest extends NLogoFormatTest[Seq[Widget]] {
  import org.nlogo.core.{ View, Button }

  def subject = nlogoFormat.InterfaceComponent
  def modelComponent(model: Model): Seq[Widget] = model.widgets
  def attachComponent(widgets: Seq[Widget]): Model = Model(widgets = widgets)

  def sampleWidgetSection(filename: String): Array[String] =
    scala.io.Source.fromFile(s"test/fileformat/$filename").mkString.lines.toArray

  testErrorsOnDeserialization("empty widgets section", Array[String](), "Every model must have at least a view...")
  testRoundTripsObjectForm("default view", Seq(View()))
  testRoundTripsObjectForm("view and button", Seq(View(), Button(source = Some("abc"), 0, 0, 0, 0)))
}

class VectorShapesComponentTest extends NLogoFormatTest[Seq[VectorShape]] {
  def subject = nlogoFormat.VectorShapesComponent
  def modelComponent(model: Model): Seq[VectorShape] = model.turtleShapes
  def attachComponent(shapes: Seq[VectorShape]): Model = Model(turtleShapes = shapes)

  testDeserializes("empty shapes to default shapes", Array[String](), Model.defaultShapes)
  val defaultShape = Model.defaultShapes.find(_.name == "default").get
  testRoundTripsObjectForm("default-only shape list", Seq(defaultShape))
}

class LinkShapesComponentTest extends NLogoFormatTest[Seq[LinkShape]] {
  def subject = nlogoFormat.LinkShapesComponent
  def modelComponent(model: Model): Seq[LinkShape] = model.linkShapes
  def attachComponent(shapes: Seq[LinkShape]): Model = Model(linkShapes = shapes)

  testDeserializes("empty link shapes to default shapes", Array[String](), Model.defaultLinkShapes)
  val defaultShape = Model.defaultLinkShapes.find(_.name == "default").get
  testRoundTripsObjectForm("default-only link shape list", Seq(defaultShape))
}
