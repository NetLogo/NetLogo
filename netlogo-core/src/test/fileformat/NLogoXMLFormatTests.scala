// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.{ FileIO, XMLReader }
import org.nlogo.core.{ Femto, LiteralParser, Model }

import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class XMLTester extends AnyFunSuite {
  protected val loader =
    new NLogoXMLLoader(false, Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities"), false)

  protected def loadString(path: String): String =
    Source.fromFile(path, "UTF-8").getLines().mkString("\n")

  protected def roundTripString(source: String): String =
    loader.readModel(source, "nlogox").map(loader.sourceString(_, "nlogox").get).get

  // checks if two XML sources semantically represent the same model
  protected def assertResultXML(a: String, b: String) =
    assertResult(XMLReader.read(a))(XMLReader.read(b))
}

class NLogoXMLFormatTests extends XMLTester {
  test("Empty model round trip remains the same") {
    val modelString = FileIO.getResourceAsString("/system/empty.nlogox")

    assertResultXML(modelString, roundTripString(modelString))
  }

  test("Rejects model with no view") {
    intercept[Model.InvalidModelError](loader.readModel(loadString("test/fileformat/No View.nlogox"), "nlogox").get)
    intercept[Model.InvalidModelError](loader.readModel(loadString("test/fileformat/No Widgets.nlogox"), "nlogox").get)
  }

  test("Sample model round trip remains the same") {
    val modelString = loadString("test/fileformat/Wolf Sheep Predation.nlogox")

    assertResultXML(modelString, roundTripString(modelString))
  }
}
