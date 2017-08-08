// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  java.net.URI

import
  java.nio.file.{ Files, Paths }

import
  org.nlogo.api.ComponentSerialization

import
  org.nlogo.{ api, core },
    core.{ Button, DummyCompilationEnvironment, DummyExtensionManager, model, Model, Shape, View, Widget },
      model.{ Attribute, Element, Node, Text, XmlShape },
      Shape.{ LinkShape, VectorShape },
    api.ModelSettings

import
  org.nlogo.core.model.DummyXML._

import
  org.scalatest.FunSuite

abstract class NLogoXFormatTest[A] extends ModelSectionTest[NLogoXFormat.Section, NLogoXFormat, A] {
  val extensionManager = new DummyExtensionManager()
  val compilationEnvironment = new DummyCompilationEnvironment()

  def nlogoXFormat = new NLogoXFormat(Factory)

  override def compareSerialized(sectionA: NLogoXFormat.Section, sectionB: NLogoXFormat.Section): Boolean = {
    sectionA == sectionB
  }

  override def displaySerialized(section: NLogoXFormat.Section): String = {
    section.toString
  }

  override def minimizeSerializedDiff(e1: NLogoXFormat.Section, e2: NLogoXFormat.Section): (NLogoXFormat.Section, NLogoXFormat.Section) = {
    def minimizeNodes(n1: Node, n2: Node): (Node, Node) =
      (n1, n2) match {
        case (e1: Element, e2: Element) => minimizeSerializedDiff(e1, e2)
        case (t1: Text, t2: Text) => (t1, t2)
        case _ => (n1, n2)
      }
    def normalizeAttributes(attrs: Seq[Attribute]): Seq[Attribute] =
      attrs.map(a => Attr(a.name, a.value))
    def isOnlyWhitespace(n: Node): Boolean =
      n match {
        case t: Text => t.text.trim.isEmpty
        case _ => false
      }
    lazy val normedAttrs1 = normalizeAttributes(e1.attributes)
    lazy val normedAttrs2 = normalizeAttributes(e2.attributes)
    if (e1.tag != e2.tag) (Elem(e1.tag, normedAttrs1, Seq()), Elem(e2.tag, normedAttrs2, Seq()))
    else if (e1.attributes != e2.attributes)
      (Elem(e1.tag, normedAttrs1, Seq()), Elem(e2.tag, normedAttrs2, Seq()))
    else {
      val (newChildren1, newChildren2) = (e1.children.filterNot(isOnlyWhitespace) zip e2.children.filterNot(isOnlyWhitespace)).filter {
        case (c1, c2) => c1 != c2
      }
        .map((minimizeNodes _).tupled)
        .unzip
      (Elem(e1.tag, normedAttrs1, newChildren1), Elem(e2.tag, normedAttrs2, newChildren2))
    }
  }
}

class NLogoXFormatIOTest extends FunSuite {
  val extensionManager = new DummyExtensionManager()
  val compilationEnvironment = new DummyCompilationEnvironment()
  val format = new NLogoXFormat(ScalaXmlElementFactory)

  val sampleUri = getClass.getResource("/fileformat/Sample.nlogox").toURI

  test("fails when reading in sections from a bad URI") {
    assert(format.sections(new URI("file:///not-a-real-file")).isFailure)
  }
  test("reads in sections from a given URI") {
    assert(format.sections(sampleUri).isSuccess)
    assert(format.sections(sampleUri).get.size == 6)
  }
  test("saves specified sections to a given URI") {
    val sections =
      format.sections(sampleUri).get
    val pathToWrite = Files.createTempFile("SampleCopy", ".nlogox")
    Files.delete(pathToWrite)
    val result = format.writeSections(sections, pathToWrite.toUri)
    assert(result.isSuccess)
    assert(Paths.get(result.get).toAbsolutePath == pathToWrite.toAbsolutePath)
    format.sections(result.get).get.foreach {
      case (k, v) if ! k.contains("info") => assert(v == sections(k)) // pretty-printing breaks info tab
      case _ =>
    }
    pending
    // TODO: This does need to pass long-term, but won't pass until our xml wrapper writes CDATA
    // :P
    // Check out DOM, JDOM, dom4j
    // assert(Files.readAllLines(Paths.get(result.get)) == Files.readAllLines(Paths.get(sampleUri)))
  }
  test("invalid nlogox file gives error about model") {
    pending
  }
  test("invalid non-XML file suggests that the file extension may be incorrect") {
    val modelsLibrary = System.getProperty("netlogo.models.dir", "models")
    val antsBenchmarkPath = Paths.get(modelsLibrary, "test", "benchmarks", "Ants Benchmark.nlogo")
    assert(format.sections(antsBenchmarkPath.toUri).isFailure)
    assert(format.sections(antsBenchmarkPath.toUri).failed.get.getMessage.contains("file extension"))
  }
}

class NLogoXCodeComponentTest extends NLogoXFormatTest[String] {
  def subject: ComponentSerialization[NLogoXFormat.Section, NLogoXFormat] =
    nlogoXFormat.codeComponent

  def modelComponent(model: Model): String = model.code
  def attachComponent(b: String): Model = Model(code = b)

  testDeserializes("empty code section to empty code element", namedText("code", ""), "")
  testSerializes("strips whitespace from end-of-line", "abc  \ndef", namedText("code", "abc\ndef"))
  testSerializes("strips whitespace from end-of-line, without deleting blank lines", "abc  \n\ndef", namedText("code", "abc\n\ndef"))
  testRoundTripsSerialForm("multiple code lines with whitespace lines between", namedText("code", "breed [foos foo]\n\nto baz show 1 end"))
  testRoundTripsSerialForm("single line of code", namedText("code", "breed [foos foo]"))
  testRoundTripsObjectForm("empty line code", "")
  testRoundTripsObjectForm("single line of code", "breed [ foos foo ]")
}

class NLogoXInfoComponentTest extends NLogoXFormatTest[String] {
  def subject = nlogoXFormat.infoComponent
  def modelComponent(model: Model): String = model.info
  def attachComponent(b: String): Model = Model(info = b)

  testDeserializes("empty info section to empty string", namedText("info", ""), "")
  testRoundTripsSerialForm("single line of info tab", namedText("info", "## About this model"))
  testRoundTripsObjectForm("empty info tab", "")
}

class NLogoXVersionComponentTest extends NLogoXFormatTest[String] {
  def subject = nlogoXFormat.version
  def modelComponent(model: Model): String = model.version
  def attachComponent(b: String): Model = Model(version = b)

  testRoundTripsObjectForm("normal version", "NetLogo 6.0.5")
  testRoundTripsObjectForm("3D version", "NetLogo 3D 6.0.5")
  testDeserializes("unknown version", namedText("version", "NetLogo 4D 8.9"), "NetLogo 4D 8.9")
}

class NLogoXInterfaceComponentTest extends NLogoXFormatTest[Seq[Widget]] {
  def subject = nlogoXFormat.interfaceComponent
  def modelComponent(model: Model): Seq[Widget] = model.widgets
  def attachComponent(widgets: Seq[Widget]): Model = Model(widgets = widgets)

  testErrorsOnDeserialization("empty widgets section", Elem("widgets", Seq(), Seq()), "Every model must have at least a view...")
  testErrorsOnDeserialization("invalid widgets", Elem("widgets", Seq(), Seq(Elem("view", Seq(), Seq()))),
    "view is missing required attributes 'left', 'top', 'right', 'bottom'")
  testRoundTripsObjectForm("default view", Seq(View()))
  testRoundTripsObjectForm("view and button", Seq(View(), Button(source = Some("abc"), 0, 0, 0, 0)))
}

class NLogoXShapeComponentTest extends NLogoXFormatTest[Seq[VectorShape]] {
  def subject = nlogoXFormat.shapesComponent
  def modelComponent(model: Model): Seq[VectorShape] = model.turtleShapes
  def attachComponent(shapes: Seq[VectorShape]): Model = Model(turtleShapes = shapes)

  testDeserializes("empty shapes to default shapes", Elem("shapes", Seq(), Seq()), Model.defaultShapes)
  val defaultShape = XmlShape.convertVectorShape(Model.defaultShapes.find(_.name == "default").get)
  testRoundTripsObjectForm("default-only shape list", Seq(defaultShape))
}

class NLogoXLinkShapeComponentTest extends NLogoXFormatTest[Seq[LinkShape]] {
  def subject = nlogoXFormat.linkShapesComponent
  def modelComponent(model: Model): Seq[LinkShape] = model.linkShapes
  def attachComponent(shapes: Seq[LinkShape]): Model = Model(linkShapes = shapes)

  testDeserializes("empty link shapes to default shapes", Elem("linkShapes", Seq(), Seq()), Model.defaultLinkShapes)
  val defaultShape = XmlShape.convertLinkShape(Model.defaultLinkShapes.find(_.name == "default").get)
  testRoundTripsObjectForm("default-only link shape list", Seq(defaultShape))
}

class NLogoXModelSettingsComponentTest extends NLogoXFormatTest[ModelSettings] {
  def subject = new NLogoXModelSettings(ScalaXmlElementFactory)
  def modelComponent(model: Model): ModelSettings =
    model.optionalSectionValue(NLogoModelSettings.componentName).get
  def attachComponent(settings: ModelSettings): Model =
    Model().withOptionalSection(NLogoModelSettings.componentName, Some(settings), ModelSettings(false))

  testDeserializes("snap-to-grid defaults to false", Elem("modelSettings", Seq(), Seq()), ModelSettings(false))
  testDeserializes("snap-to-grid set to true", Elem("modelSettings", Seq(Attr("snapToGrid", "true")), Seq()), ModelSettings(true))
  testRoundTripsObjectForm("snap-to-grid", ModelSettings(true))
  testRoundTripsObjectForm("non-snap-to-grid", ModelSettings(false))
}
