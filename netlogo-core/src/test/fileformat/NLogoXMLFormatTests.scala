// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.api.FileIO
import org.nlogo.core.{ Femto, LiteralParser, Model }

import org.scalatest.funsuite.AnyFunSuite

import scala.io.Source

class NLogoXMLFormatTests extends AnyFunSuite {
  private val loader = new NLogoXMLLoader(Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities"), false)

  private def loadString(path: String): String =
    Source.fromFile(path, "UTF-8").getLines.mkString("\n")

  private def roundTripString(source: String): String =
    loader.readModel(source, "nlogox").map(loader.sourceString(_, "nlogox").get).get

  // checks if two XML sources semantically represent the same model
  private def assertResultXML(a: String, b: String) =
    assertResult(XMLReader.read(a))(XMLReader.read(b))

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

  test("Model with all features") {
    val modelString = loadString("test/fileformat/All.nlogox")

    assertResultXML(modelString, roundTripString(modelString))
  }

  test("Complex System Dynamics") {
    val modelString = loadString("test/fileformat/Wolf Sheep Predation (System Dynamics).nlogox")

    assertResultXML(modelString, roundTripString(modelString))
  }
}
