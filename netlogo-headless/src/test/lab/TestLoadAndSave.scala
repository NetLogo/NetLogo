// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lab

import org.nlogo.api.FileIO
import org.nlogo.core
import org.nlogo.core.Femto
import org.scalatest.FunSuite
import org.nlogo.agent.AgentParserCreator
import org.xml.sax.SAXException
import java.io.{ File, FileNotFoundException }

class TestLoadAndSave extends FunSuite {
  val loader = new ProtocolLoader(
      Femto.scalaSingleton("org.nlogo.parse.CompilerUtilities"))
  test("bad XML 1") {
    val xml = FileIO.file2String("test/lab/protocols.xml")
      .replaceFirst("^<\\?xml.*\\n", "")
      .replaceFirst("<!DOCTYPE.*\\n", "")
    val badXml = xml.replaceFirst("</metric>", "</mertic>")
    assert(xml != badXml)
    val ex = intercept[SAXException] {
      loader.loadAll(badXml)
    }
    // error message may vary on different VM's, so use a regex
    assert(Set("Expected \"</metric>\" to terminate element starting on line 11.",
               "The element type \"metric\" must be terminated by the matching end-tag \"</metric>\".",
               "XML declaration may only begin entities.",
               "The processing instruction target matching \"[xX][mM][lL]\" is not allowed.")
           .contains(ex.getMessage))
  }
  test("bad XML 2") {
    val xml = FileIO.file2String("test/lab/protocols.xml")
      .replaceFirst("^<\\?xml.*\\n", "")
      .replaceFirst("<!DOCTYPE.*\\n", "")
    val badXml = xml.replaceAll("metric", "mertic")
    assert(xml != badXml)
    val ex = intercept[SAXException] {
      loader.loadAll(badXml)
    }
    // error message may vary on different VM's, so use a regex
    assert(Set("Element \"experiment\" does not allow \"mertic\" here.",
               "Element type \"mertic\" must be declared.",
               "XML declaration may only begin entities.",
               "The processing instruction target matching \"[xX][mM][lL]\" is not allowed.")
           .contains(ex.getMessage))
  }
  test("bad XML 3") {
    val ex = intercept[FileNotFoundException] {
      loader.loadAll(new File("test/lab/bad.xml"))
    }
    assert(ex.getMessage().matches(".*bespaviorhace\\.dtd.*"))
  }
}
