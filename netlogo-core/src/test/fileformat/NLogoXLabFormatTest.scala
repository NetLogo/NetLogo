// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  org.nlogo.core.{ LogoList, Model, model },
    model.{ DummyXML, Element },
      DummyXML._

import
  org.nlogo.api.{ LabProtocol, RefEnumeratedValueSet, SteppedValueSet }

import scala.xml.XML

class NLogoXLabFormatTest extends NLogoXFormatTest[Seq[LabProtocol]] {
  def subject = new NLogoXLabFormat(ScalaXmlElementFactory)
  def modelComponent(model: Model): Seq[LabProtocol] =
    model.optionalSectionValue[Seq[LabProtocol]]("org.nlogo.modelsection.behaviorspace").getOrElse(Seq())
  def attachComponent(protocols: Seq[LabProtocol]): Model =
    Model().withOptionalSection("org.nlogo.modelsection.behaviorspace", Some(protocols), Seq())
  def testBehaviorSpaceXml(fileName: String): Element = {
    val text = scala.io.Source.fromFile(s"test/fileformat/$fileName").mkString
    val scalaXml = XML.loadString(text)
    new ScalaXmlElement(scalaXml)
  }
  val sampleElem = testBehaviorSpaceXml("NLogoXBehaviorSpaceExample.xml")

  val expectedProto = LabProtocol("experiment", "setup", "go", "", 10, true, true, 0, "not any? fires", List("burned-trees"), List(new RefEnumeratedValueSet("density", List(Double.box(40d), Double.box(0.1d), Double.box(70d)))))

  val steppedProto = LabProtocol("steppedExperiment", "setup", "go", "", 10, true, true, 0, "not any? fires", List("burned-trees"), List(new SteppedValueSet("density", BigDecimal(0), BigDecimal(1), BigDecimal(100))))

  val enumeratedListProto = LabProtocol("listProtocol", "setup", "go", "", 10, true, true, 0, "not any? fires",
    List("burned-trees"),
    List(new RefEnumeratedValueSet("tree-list",
      List(LogoList(Double.box(0), Double.box(1)), LogoList(Double.box(3)), "abc", Boolean.box(false)))))

  testDeserializes("empty section", Elem("experiments", Seq(), Seq()), Seq[LabProtocol]())
  testDeserializes("non-empty section", sampleElem, Seq(expectedProto))
  testRoundTripsObjectForm("empty list of experiment protocols", Seq())
  testRoundTripsObjectForm("a simple experiment protocol", Seq(expectedProto))
  testRoundTripsObjectForm("a stepped experiment protocol", Seq(steppedProto))
  testRoundTripsSerialForm("a multi-experiment protocol", testBehaviorSpaceXml("NLogoXBehaviorSpaceMultiples.xml"))
  testRoundTripsSerialForm("all test protocols", new ScalaXmlElement(XML.loadFile("test/lab/protocols-nlogox.xml")))
}
