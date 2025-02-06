// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

class NLogoXMLFormatGuiTests extends XMLTester {
  test("Model with all features") {
    val modelString = loadString("test/fileformat/All.nlogox")

    assertResultXML(modelString, roundTripString(modelString))
  }

  test("Complex System Dynamics") {
    val modelString = loadString("test/fileformat/Wolf Sheep Predation (System Dynamics).nlogox")

    assertResultXML(modelString, roundTripString(modelString))
  }
}
