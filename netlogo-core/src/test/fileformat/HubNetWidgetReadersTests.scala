// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Button, Chooser, Chooseable, Femto, Horizontal,
  ChooseableDouble, ChooseableString, ChooseableBoolean, ChooseableList,
  LiteralParser, LogoList, Monitor, Slider, Switch, Vertical, View, Widget,
  WorldDimensions }
import org.nlogo.core.model.WidgetReader
import org.nlogo.util.AnyFunSuiteEx

import org.scalacheck.{ Arbitrary, Gen }

import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks

object HubNetGenerators {
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
    x  = pos._1, y    = pos._2,
    width = pos._3 - pos._1, height = pos._4 - pos._2,
    oldSize = true,
    source = None, forever = false, actionKey = actionKey)

  val escapedChars = Gen.oneOf('\n', '\t', '\r', '\\', '"')

  val chooserAcceptableString = Gen.listOf(Gen.oneOf(Gen.alphaNumChar, escapedChars)).map(l =>
      if (l.isEmpty) "" else l.foldLeft("") { case (a, b) => a + b })

  val chooseableList: Gen[List[Chooseable]] =
    Gen.listOf(
      Gen.oneOf(
        Arbitrary.arbDouble.arbitrary.map(d => ChooseableDouble(Double.box(d))),
        Arbitrary.arbBool.arbitrary.map(b => ChooseableBoolean(Boolean.box(b))),
        chooserAcceptableString.map(ChooseableString.apply),
        Gen.listOf(Gen.identifier).map(l => ChooseableList(LogoList(l*)))))

  val chooserWidget: Gen[Chooser] = for {
    pos           <- genPos
    varName       <- escapableOpt(optionalNameString)
    choices       <- chooseableList
    currentChoice <- Gen.choose(0, choices.length)
  } yield Chooser(display = varName,
    x  = pos._1, y    = pos._2,
    width = pos._3 - pos._1, height = pos._4 - pos._2,
    oldSize = true,
    variable = varName, choices = choices,
    currentChoice = currentChoice)

  val monitorWidget: Gen[Monitor] =
    for {
      display   <- optionalNameString
      source    <- Gen.oneOf(display, Option.empty[String])
      pos       <- genPos
      precision <- Gen.choose(0, 17)
    } yield Monitor(display = display,
      x = pos._1, y = pos._2,
      width = pos._3 - pos._1, height = pos._4 - pos._2,
      oldSize = true,
      source = source, precision = precision, fontSize = 11)

  val sliderWidget: Gen[Slider] =
    for {
      pos       <- genPos
      name      <- escapableOpt(optionalNameString)
      min       <- Arbitrary.arbDouble.arbitrary
      max       <- Arbitrary.arbDouble.arbitrary.suchThat(_ > min)
      value     <- Gen.choose(min, max)
      inc       <- Arbitrary.arbDouble.arbitrary
      units     <- optionalNameString
      direction <- Gen.oneOf(Horizontal, Vertical)
      } yield {
        Slider(display = name,
          x = pos._1, y = pos._2,
          width = pos._3 - pos._1, height = pos._4 - pos._2,
          oldSize = true,
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
        x = pos._1, y = pos._2,
        width = pos._3 - pos._1, height = pos._4 - pos._2,
        oldSize = true,
        variable = name, on = isOn)
    }

  val viewWidget: Gen[View] = for {
    pos   <- genPos
    minPx <- Arbitrary.arbInt.arbitrary
    maxPx <- Arbitrary.arbInt.arbitrary.map(_ + minPx)
    minPy <- Arbitrary.arbInt.arbitrary
    maxPy <- Arbitrary.arbInt.arbitrary.map(_ + minPy)
  } yield View(
    x = pos._1, y = pos._2,
    width = pos._3 - pos._1, height = pos._4 - pos._2,
    dimensions = WorldDimensions(minPx, maxPx, minPy, maxPy))

  val hubNetWidgets: Gen[Widget] =
    Gen.oneOf(buttonWidget, chooserWidget, monitorWidget, sliderWidget, switchWidget, viewWidget)

  implicit val arbWidget: org.scalacheck.Arbitrary[org.nlogo.core.Widget] = Arbitrary(hubNetWidgets)

  val classyReaders =
    HubNetWidgetReaders.additionalReaders
      .values.map(r => new ClassyReader(r))

  lazy val litParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
}

class HubNetWidgetReadersTest extends AnyFunSuiteEx with ScalaCheckDrivenPropertyChecks {

  import HubNetGenerators._

  test("serializes / deserializes hubnet widgets") {
    forAll(hubNetWidgets) { (widget: Widget) =>
      val reader = classyReaders.find(_.applies(widget)).get
      val serialized = reader.format(widget)
      assert(reader.validate(serialized), "serialized wiget should be valid")
      val deserialized = reader.parse(serialized)
      assert(widget == deserialized, "round-trip must not change widget, written as:\n" + serialized)
    }
  }

  test("pathological case 1") {
    val chooser =
      Chooser(None,-2147483648,-185212488,2147483647,859780949,true,None,List(ChooseableBoolean(true), ChooseableBoolean(true), ChooseableBoolean(false), ChooseableDouble(8.502858506075463E-83)), 10)
    val reader = HubNetChooserReader
    val serialized = reader.format(chooser)
    assert(reader.validate(serialized.linesIterator.toList), "serialized wiget should be valid")
    val deserialized = reader.parse(serialized.linesIterator.toList, litParser)
    assert(chooser == deserialized, "round-trip must not change widget, written as:\n" + serialized)
  }
}

object ClassyReader {
  def apply[A <: WidgetReader, B](reader: WidgetReader) =
    new ClassyReader(reader)
}

class ClassyReader(val reader: WidgetReader) {
  lazy val litParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")

  def applies(w: Widget): Boolean =
    reader.classTag.unapply(w).nonEmpty

  def format(w: Widget): String =
    reader.classTag.unapply(w).map(reader.format).get

  def validate(s: String): Boolean =
    reader.validate(s.linesIterator.toList)

  def parse(s: String): Widget = {
    reader.parse(s.linesIterator.toList, litParser)
  }
}
