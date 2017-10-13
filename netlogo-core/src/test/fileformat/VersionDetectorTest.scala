// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.scalatest.FunSuite

import org.nlogo.core.{ Model, View }
import org.nlogo.api.{ TwoDVersion, ThreeDVersion, WorldDimensions3D }

class VersionDetectorTest extends FunSuite {
  val loader = basicLoader
    .addFormat[Array[String], NLogoThreeDFormat](new NLogoThreeDFormat)

  val twoDModel = Model("").copy(version = TwoDVersion.version)
  val threeDModel = Model("").copy(
    version = ThreeDVersion.version,
    widgets = Seq(View(dimensions = WorldDimensions3D(0, 0, 0, 0, 0, 0, 12))))
  def modelAsString(m: Model, format: String) =
    loader.sourceString(m, format).get

  test("fromPath detects nlogo version to be 2D") {
    assertResult(TwoDVersion)(VersionDetector.fromPath("foo.nlogo", loader).get)
  }

  test("fromModelContents detects nlogo version to be 2D") {
    assertResult(TwoDVersion)(
      VersionDetector.fromModelContents(modelAsString(twoDModel, "nlogo"), loader).get)
  }

  test("fromPath detects nlogo3d version to be 3D") {
    assertResult(ThreeDVersion)(VersionDetector.fromPath("foo.nlogo3d", loader).get)
  }

  test("fromModelContents detects nlogo3d version to be 3D") {
    assertResult(ThreeDVersion)(
      VersionDetector.fromModelContents(modelAsString(threeDModel, "nlogo3d"), loader).get)
  }

  test("detects nlogox version 3D when it stores a 3D model") {
    pending
  }

  test("fromModelContents dectects nlogox version to be 2D when it stores a 2D model") {
    assertResult(TwoDVersion)(
      VersionDetector.fromModelContents(modelAsString(twoDModel, "nlogox"), loader).get)
  }

  test("fromPath detects nlogox version to be 2D when it stores a 2D model") {
    // TODO: Once we support 3D nlogox, we will have to write this file
    assertResult(TwoDVersion)(VersionDetector.fromPath("foo.nlogox", loader).get)
  }

  test("detects nlogo string suffix to be .nlogo") {
    val modelString = modelAsString(twoDModel, "nlogo")
    assert(VersionDetector.findSuffix(modelString).get == "nlogo")
  }

  test("detects nlogo3d string suffix to be .nlogo3d") {
    val modelString = modelAsString(threeDModel, "nlogo3d")
    assert(VersionDetector.findSuffix(modelString).get == "nlogo3d")
  }

  test("detects nlogox string suffix to be .nlogox") {
    val modelString = modelAsString(twoDModel, "nlogox")
    assert(VersionDetector.findSuffix(modelString).get == "nlogox")
  }

  test("doesn't detect a suffix for a bad model") {
    assert(VersionDetector.findSuffix("I'm just a string") == None)
  }
}
