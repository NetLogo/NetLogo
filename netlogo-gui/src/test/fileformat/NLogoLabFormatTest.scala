// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Femto, LiteralParser, Model }
import org.nlogo.api.{ EnumeratedValueSet, LabProtocol }

class NLogoLabFormatTest extends NLogoFormatTest[Seq[LabProtocol]] {
  private val litParser =
  Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
  def subject = new NLogoLabFormat({ _ => identity }, litParser)
  def modelComponent(model: Model): Seq[LabProtocol] =
    model.optionalSectionValue[Seq[LabProtocol]]("org.nlogo.modelsection.behaviorspace").getOrElse(Seq())
  def attachComponent(protocols: Seq[LabProtocol]): Model =
    Model().withOptionalSection("org.nlogo.modelsection.behaviorspace", Some(protocols), Seq())
  def testBehaviorSpaceXml(fileName: String): Array[String] =
    scala.io.Source.fromFile(s"test/fileformat/$fileName")
      .mkString.lines.toArray
  val sampleLines = testBehaviorSpaceXml("BehaviorSpaceExample.xml")

  val expectedProto = LabProtocol("experiment", "setup", "go", "", 10, true, 0, "not any? fires", List("burned-trees"), List(new EnumeratedValueSet("density", List(40d, 0.1d, 70d))))

  testDeserializes("empty section", Array[String](), Seq[LabProtocol]())
  testDeserializes("non-empty section", sampleLines, Seq(expectedProto))
  testRoundTripsObjectForm("a simple experiment protocol", Seq(expectedProto))
  testRoundTripsSerialForm("a multi-experiment protocol", testBehaviorSpaceXml("BehaviorSpaceMultiples.xml"))
}
