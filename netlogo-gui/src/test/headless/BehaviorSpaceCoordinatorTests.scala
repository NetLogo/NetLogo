// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import java.nio.file.{ Files, Paths }

import org.nlogo.api.{ LabProtocol, RefEnumeratedValueSet, SteppedValueSet, TwoDVersion }
import org.nlogo.core.{ Femto, LiteralParser }
import org.nlogo.nvm.LabInterface.Settings
import org.nlogo.fileformat
import org.nlogo.xmllib.{ ScalaXmlElement, ScalaXmlElementFactory }
import org.nlogo.util.Using

import org.scalatest.FunSuite

import scala.io.Source
import scala.xml.XML

class BehaviorSpaceCoordinatorTests extends FunSuite {
  private val literalParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  private lazy val nlogoFormat =
    new fileformat.NLogoLabFormat(literalParser)

  private lazy val nlogoxFormat =
    new fileformat.NLogoXLabFormat(ScalaXmlElementFactory)

  val emptySettings = Settings(
    "empty.nlogox", getClass.getResource("/system/empty.nlogox").toURI,
    Some("testBehaviorSpace1"), None, None, None, None, 2, false, TwoDVersion)

  test("opens XML setup file written in the old format") {
    Using[HeadlessWorkspace, Unit](HeadlessWorkspace.newInstance(false), _.dispose) { ws =>
      val settings = emptySettings.copy(externalXMLFile = Some(Paths.get("test/lab/protocols.xml").toUri))

      val Some((proto, _, converted)) = BehaviorSpaceCoordinator.selectProtocol(settings, ws)
      assertResult(expectedProtocol)(proto)
      assert(converted.nonEmpty)
    }
  }

  test("opens XML setup file written in the new format") {
    Using[HeadlessWorkspace, Unit](HeadlessWorkspace.newInstance(false), _.dispose) { ws =>
      val settings = emptySettings.copy(externalXMLFile = Some(Paths.get("test/lab/protocols-nlogox.xml").toUri))

      val Some((proto, _, converted)) = BehaviorSpaceCoordinator.selectProtocol(settings, ws)
      assertResult(expectedProtocol)(proto)
      assert(converted.isEmpty)
    }
  }

  test("converts a file written in the old format to use the new format") {
    val tmpFile = Files.createTempFile("converted", ".xml")
    val oldPath = Paths.get(TestBehaviorSpace.TestNLogoProtocolsFilePath)
    BehaviorSpaceCoordinator.convertToNewFormat(oldPath, tmpFile)
    val expectedContents = nlogoFormat.load(Source.fromFile(TestBehaviorSpace.TestNLogoProtocolsFilePath).getLines.toArray, None)
    val fileContents = nlogoxFormat.load(new ScalaXmlElement(XML.loadFile(tmpFile.toFile)), None)
    assertResult(expectedContents)(fileContents)
  }

  private val expectedProtocol =
    LabProtocol.fromValueSets(
      "testBehaviorSpace1",
      "set counter 0",
      "repeat param1 [ repeat param2 [ set counter counter + 1 ] ]",
      "",
      2,
      true,
      true,
      2,
      "counter > param2",
      List("counter"),
      List(
        RefEnumeratedValueSet("param1", List(Double.box(1), Double.box(2), Double.box(3))),
        SteppedValueSet("param2", BigDecimal(1), BigDecimal(1), BigDecimal(5)))
      )

}
