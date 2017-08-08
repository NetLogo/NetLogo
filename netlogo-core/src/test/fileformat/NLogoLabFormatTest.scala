// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Femto, LiteralParser, Model }
import org.nlogo.api.{ RefEnumeratedValueSet, FileIO, LabProtocol }

class NLogoLabFormatTest extends NLogoFormatTest[Seq[LabProtocol]] {
  private val litParser =
  Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
  def subject = new NLogoLabFormat(litParser)
  def modelComponent(model: Model): Seq[LabProtocol] =
    model.optionalSectionValue[Seq[LabProtocol]]("org.nlogo.modelsection.behaviorspace").getOrElse(Seq())
  def attachComponent(protocols: Seq[LabProtocol]): Model =
    Model().withOptionalSection("org.nlogo.modelsection.behaviorspace", Some(protocols), Seq())
  def testBehaviorSpaceXml(fileName: String): Array[String] =
    scala.io.Source.fromFile(s"test/fileformat/$fileName")
      .mkString.lines.toArray
  val sampleLines = testBehaviorSpaceXml("NLogoBehaviorSpaceExample.xml")

  val expectedProto = LabProtocol("experiment", "setup", "go", "", 10, true, true, 0, "not any? fires", List("burned-trees"), List(new RefEnumeratedValueSet("density", List(Double.box(40d), Double.box(0.1d), Double.box(70d)))))

  testDeserializes("empty section", Array[String](), Seq[LabProtocol]())
  testDeserializes("non-empty section", sampleLines, Seq(expectedProto))
  testRoundTripsObjectForm("empty list of experiment protocols", Seq())
  testRoundTripsObjectForm("a simple experiment protocol", Seq(expectedProto))
  testRoundTripsSerialForm("a multi-experiment protocol", testBehaviorSpaceXml("BehaviorSpaceMultiples.xml"))
  testRoundTripsSerialForm("all test protocols", FileIO.fileToString("test/lab/protocols.xml").lines.toArray)
}
