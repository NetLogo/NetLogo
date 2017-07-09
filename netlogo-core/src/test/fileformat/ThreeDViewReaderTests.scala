// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Femto, LiteralParser, UpdateMode, View }

import org.nlogo.api.WorldDimensions3D

import org.scalacheck.{ Arbitrary, Gen }

import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class ThreeDViewReaderTest extends FunSuite with GeneratorDrivenPropertyChecks {
  val dimensionsThreeD: Gen[WorldDimensions3D] = for {
    minPx     <- Arbitrary.arbInt.arbitrary
    maxPx     <- Arbitrary.arbInt.arbitrary.map(_.abs + minPx)
    minPy     <- Arbitrary.arbInt.arbitrary
    maxPy     <- Arbitrary.arbInt.arbitrary.map(_.abs + minPy)
    minPz     <- Arbitrary.arbInt.arbitrary
    maxPz     <- Arbitrary.arbInt.arbitrary.map(_.abs + minPz)
    patchSize <- Arbitrary.arbDouble.arbitrary.suchThat(_ > 0.0)
    wrapInX   <- Arbitrary.arbBool.arbitrary
    wrapInY   <- Arbitrary.arbBool.arbitrary
    wrapInZ   <- Arbitrary.arbBool.arbitrary
  } yield new WorldDimensions3D(
    minPx, maxPx, minPy, maxPy, minPz, maxPz, patchSize,
    wrapInX, wrapInY, wrapInZ)

  val threeDViewWidgets: Gen[View] = for {
    (x1, y1, x2, y2) <- Arbitrary.arbTuple4[Int, Int, Int, Int].arbitrary
    dims             <- dimensionsThreeD
    (fontSize: Int)  <- Gen.choose(1, 100)
    updateMode       <- Gen.oneOf(UpdateMode.TickBased, UpdateMode.Continuous)
    showTicks        <- Arbitrary.arbBool.arbitrary
    tickCounterLabel <- Gen.option(Gen.oneOf("ticks", ""))
    frameRate        <- Gen.choose(1, 60)
  } yield View(x1, y1, x2, y2, dims, fontSize, updateMode, showTicks, tickCounterLabel, frameRate)

  lazy val litParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  test("round-tripping works") {
    forAll(threeDViewWidgets) { (view: View) =>
      val serialized = ThreeDViewReader.format(view).lines.toList
      assert(ThreeDViewReader.validate(serialized), "serialized widget should be valid")
      val deserialized = ThreeDViewReader.parse(serialized, litParser)
      assert(view == deserialized, "round-trip must not change widget, written as: " + serialized)
    }
  }
}
