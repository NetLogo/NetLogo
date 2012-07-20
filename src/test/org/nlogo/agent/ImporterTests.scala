// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.scalatest.{ FunSuite, OneInstancePerTest }
import org.nlogo.api.{ ImporterUser, WorldDimensions }

class ImporterTests extends FunSuite with OneInstancePerTest {
  val IGNORE_ERROR_HANDLER =
    new ImporterJ.ErrorHandler() {
      def showError(title: String, errorDetails: String, fatalError: Boolean) =
        // don't do anything and we always want to continue, so return true.
        true
    }
  val IMPORTER_USER =
    new ImporterUser() {
      def setDimensions(d: WorldDimensions) {
        world.createPatches(d.minPxcor, d.maxPxcor, d.minPycor, d.maxPycor)
      }
      def setDimensions(d: WorldDimensions, patchSize: Double) {
        world.createPatches(d.minPxcor, d.maxPxcor, d.minPycor, d.maxPycor)
        world.patchSize(patchSize)
      }
      def patchSize(patchSize: Double) {
        world.patchSize(patchSize)
      }
      def setOutputAreaContents(text: String) { }
      def resizeView() { }
      def currentPlot(plot: String) { }
      def getPlot(plot: String): org.nlogo.api.PlotInterface = null
      def importExtensionData(name: String, data: java.util.List[Array[String]], handler: org.nlogo.api.ImportErrorHandler) { }
      def isExtensionName(name: String) = false
    }
  class StringReaderTest extends ImporterJ.StringReader {
    def readFromString(s: String): AnyRef = {
      try Int.box(s.toInt)
      catch { case ex: NumberFormatException =>
        throw new ImporterJ.StringReaderException("invalid integer")
      }
    }
  }
  val world = new World()
  world.createPatches(-10, 10, -10, 10)
  world.realloc()
  val importer = new Importer(IGNORE_ERROR_HANDLER, world,
                              IMPORTER_USER, new StringReaderTest())
  def testGetTokenValue(testString: String, turtleBreedVar: Boolean, expected: AnyRef) {
    val result = importer.getTokenValue(testString, turtleBreedVar, false)
    expect(expected)(result)
  }
  def testInvalidGetTokenValue(testString: String, turtleBreedVar: Boolean) {
    val result = importer.getTokenValue(testString, turtleBreedVar, false)
    if(turtleBreedVar)
      expect(world.turtles)(result)
    else
      assert(result.isInstanceOf[Importer#Junk])
  }

  test("Jikes115BugTest1") {
    testGetTokenValue("5", false, Int.box(5))
  }
  test("Jikes115BugTest2") {
    testGetTokenValue("5", true, Int.box(5))
  }
  test("Jikes115BugInvalidTest1") {
    testInvalidGetTokenValue("23fish", false)
  }
  test("Jikes115BugInvalidTest2") {
    testInvalidGetTokenValue("23fish", true)
  }

}
