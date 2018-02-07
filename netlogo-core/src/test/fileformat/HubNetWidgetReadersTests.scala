// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.{ Button, Chooser, Chooseable, Femto, Horizontal,
  ChooseableDouble, ChooseableString, ChooseableBoolean, ChooseableList,
  LiteralParser, LogoList, Monitor, Slider, Switch, Vertical, View, Widget,
  WorldDimensions, model },
    model.HubNetWidgetReader

import org.nlogo.core.model.WidgetReader

import org.scalacheck.{ Arbitrary, Gen, Shrink }

import org.scalatest.FunSuite
import org.scalatest.prop.GeneratorDrivenPropertyChecks

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
    left  = pos._1, top    = pos._2,
    right = pos._3, bottom = pos._4,
    source = None, forever = false, actionKey = actionKey)

  val escapedChars = Gen.oneOf('\n', '\t', '\r', '\\', '"')

  val chooserAcceptableString = escapable(genNameString)

  val chooseableList: Gen[List[Chooseable]] =
    Gen.listOf(
      Gen.oneOf(
        Arbitrary.arbDouble.arbitrary.map(d => ChooseableDouble(Double.box(d))),
        Arbitrary.arbBool.arbitrary.map(b => ChooseableBoolean(Boolean.box(b))),
        chooserAcceptableString.map(ChooseableString.apply),
        Gen.listOf(Gen.identifier).map(l => ChooseableList(LogoList(l: _*)))))

  val chooserWidget: Gen[Chooser] = for {
    pos           <- genPos
    varName       <- optionalNameString
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
      source    <- Gen.oneOf(display, Option.empty[String])
      pos       <- genPos
      precision <- Gen.choose(0, 17)
    } yield Monitor(display = display,
      left = pos._1, top = pos._2,
      right = pos._3, bottom = pos._4,
      source = source, precision = precision, fontSize = 11)

  val sliderWidget: Gen[Slider] =
    for {
      pos       <- genPos
      name      <- optionalNameString
      min       <- Arbitrary.arbDouble.arbitrary
      max       <- Arbitrary.arbDouble.arbitrary.suchThat(_ > min)
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
    name    <- optionalNameString
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

  val hubNetWidgets: Gen[Widget] =
    Gen.oneOf(buttonWidget, chooserWidget, monitorWidget, sliderWidget, switchWidget, viewWidget)

  implicit def shrinkWidget(implicit shrinkChooseableList: Shrink[List[Chooseable]]) = Shrink[Widget]({ w: Widget =>
    w match {
      case c: Chooser =>
        shrinkChooseableList.shrink(c.choices)
          .map(choices => c.copy(choices = choices))
          .flatMap(chooser =>
              chooser.variable.map(v =>
                Shrink.shrinkString.shrink(v).map(newName => chooser.copy(variable = Some(newName), display = Some(newName))))
              .getOrElse(Stream(chooser)))
      case s: Slider =>
        s.variable
          .map(varName =>
              Shrink.shrinkString.shrink(varName).map(newName => s.copy(variable = Some(newName), display = Some(newName))))
          .getOrElse(Stream.empty[Widget])
      case _ => Stream.empty[Widget]
    }
  })

  implicit val arbWidget = Arbitrary(hubNetWidgets)

  lazy val litParser =
    Femto.scalaSingleton[LiteralParser]("org.nlogo.parse.CompilerUtilities")
}

class HubNetWidgetReadersTest extends FunSuite with GeneratorDrivenPropertyChecks {

  import HubNetGenerators._

  test("serializes / deserializes hubnet widgets") {
    forAll(hubNetWidgets) { (widget: Widget) =>
      val serialized = HubNetWidgetReader.format(widget)
      assert(! serialized.isEmpty)
      val deserialized = HubNetWidgetReader.read(serialized.lines.toList, litParser)
      assert(widget == deserialized, "round-trip must not change widget, written as:\n" + serialized)
    }
  }

  test("pathological case 1") {
    val chooser =
      Chooser(None,-2147483648,-185212488,2147483647,859780949,None,List(ChooseableBoolean(true), ChooseableBoolean(true), ChooseableBoolean(false), ChooseableDouble(8.502858506075463E-83)), 10)
    val serialized = HubNetWidgetReader.format(chooser)
    val deserialized = HubNetWidgetReader.read(serialized.lines.toList, litParser)
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
    reader.validate(s.lines.toList)

  def parse(s: String): Widget = {
    reader.parse(s.lines.toList, litParser)
  }
}
