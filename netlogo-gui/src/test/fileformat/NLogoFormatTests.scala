// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import java.net.URI
import java.nio.file.{ Files, Paths }
import java.util.Arrays

import org.scalatest.FunSuite

import org.nlogo.api.ComponentSerialization

import org.nlogo.core.{ Model, Shape, Widget }, Shape.{ LinkShape, VectorShape }

import scala.collection.JavaConversions._

abstract class NLogoFormatTest[A] extends ModelSectionTest[Array[String], NLogoFormat, A] {
  def autoConvert(v: String)(c: String): String =
    c.replaceAllLiterally("z", "zz")

  def nlogoFormat = new NLogoFormat(autoConvert _)

  override def compareSerialized(a: Array[String], otherA: Array[String]): Boolean = {
    Arrays.deepEquals(a.asInstanceOf[Array[Object]], otherA.asInstanceOf[Array[Object]])
  }

  override def displaySerialized(a: Array[String]): String =
    a.mkString(s"Array[${a.length}](", ",\n", ")")
}

class NLogoFormatIOTest extends FunSuite {
  lazy val modelsLibrary = System.getProperty("netlogo.models.dir", "models")

  val format = new NLogoFormat(_ => identity)

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
    assert(Files.readAllLines(Paths.get(result.get)).mkString("\n") == Files.readAllLines(antsBenchmarkPath).mkString("\n"))
  }
}

class CodeComponentTest extends NLogoFormatTest[String] {
  def subject: ComponentSerialization[Array[String], NLogoFormat] =
    nlogoFormat.CodeComponent

  def modelComponent(model: Model): String = model.code

  def attachComponent(b: String): Model = Model(code = b)

  testDeserializes("empty code section to empty string", Array[String](), "")
  testDeserializes("code section and performs auto conversion", Array[String]("to foo ask zs [fd 1] end"), "to foo ask zzs [fd 1] end")
  testAltersObjectRepresentation("stripping trailing whitespace from code", "to foo end        ", "to foo end")
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

  testDeserializes("empty version section to empty version", Array[String](), "")
  testDeserializes("multiple version lines to correct version", Array[String]("", "NetLogo 6.0", ""), "NetLogo 6.0")
}

class InterfaceComponentTest extends NLogoFormatTest[Seq[Widget]] {
  import org.nlogo.core.{ View, Button }

  def subject = nlogoFormat.InterfaceComponent
  def modelComponent(model: Model): Seq[Widget] = model.widgets
  def attachComponent(widgets: Seq[Widget]): Model = Model(widgets = widgets)

  def sampleWidgetSection(filename: String): Array[String] =
    scala.io.Source.fromFile(s"test/fileformat/$filename").mkString.lines.toArray

  testDeserializationError[Model.InvalidModelError]("empty widgets section", Array[String]())
  testDeserializes("button and auto converts source", sampleWidgetSection("WidgetSection.txt"), Seq(View(), Button(source = Some("setupzz"), 0, 0, 0, 0)))
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
