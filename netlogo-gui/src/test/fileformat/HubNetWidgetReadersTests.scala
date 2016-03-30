// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Button, Chooser, Chooseable, Femto, Horizontal,
  ChooseableDouble, ChooseableString, ChooseableBoolean, ChooseableList,
  LiteralParser, LogoList, Monitor, Slider, Switch, Vertical, View, Widget,
  WorldDimensions }

import org.nlogo.compiler.Compiler

import org.nlogo.core.model.{ SimpleLiteralParser, WidgetReader }

import org.scalacheck.{ Arbitrary, Gen }

import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.reflect.ClassTag

class HubNetWidgetReadersTest extends FunSuite with GeneratorDrivenPropertyChecks {

  val genPos = Arbitrary.arbTuple4[Int, Int, Int, Int].arbitrary

  val genNameString = Gen.listOf(Gen.identifier).map(_.mkString(" "))

  val optionalOneChar = Gen.option(Gen.alphaNumChar)

  val optionalNameString =
    Gen.option(genNameString).map(_.filter(s => s != "" && s != "NIL"))

  def escapableChars: Gen[String] = Gen.oneOf("\n", "\\", "\"")

  // adds in escapable characters sometimes, for fun!
  def escapable(base: Gen[String]): Gen[String] =
    Gen.oneOf(base,
      for {
        g         <- base
        escapable <- escapableChars
      } yield escapable + g + escapable)

  def escapableOpt(base: Gen[Option[String]]): Gen[Option[String]] =
    Gen.oneOf(base,
      for {
        stringOpt <- base
        escapable <- escapableChars
        string <- stringOpt
      } yield escapable + string + escapable)

  val buttonWidget: Gen[Button] = for {
    display   <- optionalNameString
    pos       <- genPos
    actionKey <- optionalOneChar
  } yield Button(display = display,
    left  = pos._1, top    = pos._2,
    right = pos._3, bottom = pos._4,
    source = None, forever = false, actionKey = actionKey)

  val chooseableList: Gen[List[Chooseable]] =
    Gen.listOf(
    Gen.oneOf(
      Arbitrary.arbDouble.arbitrary.map(d => ChooseableDouble(Double.box(d))),
        Arbitrary.arbBool.arbitrary.map(b => ChooseableBoolean(Boolean.box(b))),
      Arbitrary.arbString.arbitrary.map(ChooseableString),
      Gen.listOf(Gen.identifier).map(l => ChooseableList(LogoList(l: _*)))))

  val chooserWidget: Gen[Chooser] = for {
    pos           <- genPos
    varName       <- escapableOpt(optionalNameString)
    choices       <- chooseableList
    currentChoice <- Gen.choose(0, choices.length)
  } yield Chooser(display = varName,
    left  = pos._1, top    = pos._2,
    right = pos._3, bottom = pos._4,
    variable = varName, choices = choices,
    currentChoice = currentChoice)

  val monitorWidget: Gen[Monitor] =
    for {
      display   <- optionalNameString
      pos       <- genPos
      precision <- Gen.choose(0, 17)
    } yield Monitor(display = display,
      left = pos._1, top = pos._2,
      right = pos._3, bottom = pos._4,
      source = None, precision = precision, fontSize = 11)

  val sliderWidget: Gen[Slider] =
    for {
      pos       <- genPos
      name      <- escapableOpt(optionalNameString)
      min       <- Arbitrary.arbDouble.arbitrary
      max       <- Arbitrary.arbDouble.arbitrary.map(_ + min)
      value     <- Gen.choose(min, max)
      inc       <- Arbitrary.arbDouble.arbitrary
      units     <- optionalNameString
      direction <- Gen.oneOf(Horizontal, Vertical)
    } yield {
      Slider(display = name,
        left = pos._1,       top = pos._2,
        right = pos._3,      bottom = pos._4,
        min = min.toString,  max = max.toString,
        variable = name, default = value,
        step = inc.toString, units = units,
        direction = direction)
    }

  val switchWidget: Gen[Switch] = for {
    pos     <- genPos
    name    <- escapableOpt(optionalNameString)
    isOn    <- Arbitrary.arbBool.arbitrary
  } yield {
    Switch(display = name,
      left = pos._1,       top = pos._2,
      right = pos._3,      bottom = pos._4,
      variable = name, on = isOn)
  }

  val viewWidget: Gen[View] = for {
    pos   <- genPos
    minPx <- Arbitrary.arbInt.arbitrary
    maxPx <- Arbitrary.arbInt.arbitrary.map(_ + minPx)
    minPy <- Arbitrary.arbInt.arbitrary
    maxPy <- Arbitrary.arbInt.arbitrary.map(_ + minPy)
  } yield View(
      left  = pos._1, top    = pos._2,
      right = pos._3, bottom = pos._4,
      dimensions = WorldDimensions(minPx, maxPx, minPy, maxPy))

  val hubNetWidgets: Gen[Widget] = Gen.oneOf(buttonWidget, chooserWidget, monitorWidget, sliderWidget, switchWidget, viewWidget)

  val classyReaders =
    HubNetWidgetReaders.additionalReaders
      .values.map(r => new ClassyReader(r))

  object ClassyReader {
    def apply[A <: WidgetReader, B](reader: WidgetReader)(implicit ev: reader.T =:= B) =
      new ClassyReader(reader)
  }

  class ClassyReader(val reader: WidgetReader) {
    def applies(w: Widget): Boolean =
      reader.classTag.unapply(w).nonEmpty

    def format(w: Widget): String =
      reader.classTag.unapply(w).map(reader.format).get

    def validate(s: String): Boolean =
      reader.validate(s.lines.toList)

    def parse(s: String): Widget = {
      val litParser = Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
      reader.parse(s.lines.toList, litParser)
    }
  }

  test("round-tripping works") {
    forAll(hubNetWidgets) { (widget: Widget) =>
      val reader = classyReaders.find(_.applies(widget)).get
      val serialized = reader.format(widget)
      assert(reader.validate(serialized), "serialized wiget should be valid")
      val deserialized = reader.parse(serialized)
      assert(widget == deserialized, "round-trip must not change widget, written as:\n" + serialized)
    }
  }
}
