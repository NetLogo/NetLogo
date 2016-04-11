// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.parboiled.scala._
import org.nlogo.api.Dump
import org.nlogo.core.{ Button, Chooseable, Chooser, Direction,
  Horizontal, LiteralParser, LogoList, Monitor, Slider, Switch,
  Vertical, View, WorldDimensions }
import org.nlogo.core.model.WidgetReader

import scala.reflect.ClassTag

import ParboiledWidgetParser.{ restoreLines, stripLines }

object HubNetWidgetReaders {
  def saveNillableString(s: String): String =
    if (s != null && s.trim.nonEmpty)
      s
    else
      "NIL"

  val additionalReaders: Map[String, WidgetReader] =
    Map(
      "BUTTON"  -> HubNetButtonReader,
      "CHOOSER" -> HubNetChooserReader,
      "MONITOR" -> HubNetMonitorReader,
      "SLIDER"  -> HubNetSliderReader,
      "SWITCH"  -> HubNetSwitchReader,
      "VIEW"    -> HubNetViewReader)
}

import HubNetWidgetReaders._

object HubNetSwitchReader extends DefaultParboiledWidgetParser with WidgetReader {
  class SwitchParser extends Parser with ParboiledWidgetParser.RichRule {
    def parserToSwitch(
      pos: (Int, Int, Int, Int),
      name: Option[String],
      // note that the value of on is stored as 0: on, 1: off
      notOn: Boolean): Switch = {
        Switch(
          name.map(restoreLines),
          pos._1, pos._2, pos._3, pos._4,
          name.map(restoreLines), ! notOn)
    }

    def HubNetSwitch: Rule1[Switch] = rule {
      SwitchHeader ~ NewLine ~
      PositionInformation ~
      IgnoredLine ~
      NillableString ~ NewLine ~
      BooleanDigit ~ NewLine ~
      IgnoredLine ~
      IgnoredText ~~> parserToSwitch _
    }

    def SwitchHeader: Rule0 = rule { "SWITCH" }

  }

  def parser = new SwitchParser {
    override val buildParseTree = true
  }

  type T = Switch

  type ParsedWidget = Switch

  def classTag: ClassTag[T] = ClassTag(classOf[Switch])

  override def parseRule = parser.HubNetSwitch

  def format(switch: T): String = {
    Seq(
      "SWITCH",
      switch.left.toString,
      switch.top.toString,
      switch.right.toString,
      switch.bottom.toString,
      switch.display.map(stripLines).getOrElse("NIL"),
      switch.variable.map(stripLines).getOrElse("NIL"),
      if (switch.on) "0" else "1",
      "1",    // compat
      "-1000" // compat
    ).mkString("", "\n", "\n")
  }
}

object HubNetViewReader extends DefaultParboiledWidgetParser with WidgetReader {
  class ViewParser extends Parser with ParboiledWidgetParser.RichRule {
    def parserToView(
      pos: (Int, Int, Int, Int),
      dim: List[Int],
      maxPy: Int): View = {
        View(
          left = pos._1, top = pos._2, right = pos._3, bottom = pos._4,
          dimensions = WorldDimensions(dim(0), dim(1), dim(2), maxPy))
    }

    def HubNetView: Rule1[View] = rule {
      ViewHeader ~ NewLine ~
      PositionInformation ~
      nTimes(12, IgnoredLine) ~
      nTimes(3, IntValue ~ NewLine) ~
      IntValue ~~> parserToView _
    }

    def ViewHeader: Rule0 = rule { "VIEW" }
  }

  def parser = new ViewParser {
    override val buildParseTree = true
  }

  type T = View

  type ParsedWidget = View

  def classTag: ClassTag[T] = ClassTag(classOf[View])

  override def parseRule = parser.HubNetView

  def format(view: T): String = {
    Seq(
      "VIEW", // 0
      view.left.toString,
      view.top.toString,
      view.right.toString,
      view.bottom.toString,
      "0", // 5
      "0", // 6
      "0", // 7
      "1", // 8
      "1", // 9
      "1", // 10
      "1", // 11
      "1", // 12
      "0", // 13
      "1", // 14
      "1", // 15
      "1", // 16
      view.dimensions.minPxcor.toString, // 17
      view.dimensions.maxPxcor.toString, // 18
      view.dimensions.minPycor.toString, // 19
      view.dimensions.maxPycor.toString  // 20
    ).mkString("", "\n", "\n")
  }
}

object HubNetButtonReader extends DefaultParboiledWidgetParser with WidgetReader {
  class ButtonParser extends Parser with ParboiledWidgetParser.RichRule {
    def parserToButton(
      pos: (Int, Int, Int, Int),
      name: Option[String],
      actionKey: Option[String]): Button = {
        Button(
          display = name,
          left = pos._1, top = pos._2,
          right = pos._3, bottom = pos._4,
          source = None,
          forever = false,
          actionKey = actionKey.map(_.head))
    }

    def HubNetButton: Rule1[Button] = rule {
      ButtonHeader ~ NewLine ~
      PositionInformation ~
      NillableString ~ NewLine ~
      nTimes(6, IgnoredLine) ~
      NillableString ~~> parserToButton _
    }

    def ButtonHeader: Rule0 = rule { "BUTTON" }
  }

  def parser = new ButtonParser {
    override val buildParseTree = true
  }

  type T = Button

  type ParsedWidget = Button

  def classTag: ClassTag[T] = ClassTag(classOf[Button])

  override def parseRule = parser.HubNetButton

  def format(button: T): String = {
    val savedActionKey = button.actionKey.map(_.toString).getOrElse("NIL")
    Seq(
      "BUTTON", // 0
      button.left.toString,
      button.top.toString,
      button.right.toString,
      button.bottom.toString,
      button.display.map(saveNillableString).getOrElse("NIL"),
      "NIL",
      "NIL",
      "1",
      "T",
      "OBSERVER",
      "NIL",
      savedActionKey
    ).mkString("", "\n", "\n")
  }
}

object HubNetMonitorReader extends DefaultParboiledWidgetParser with WidgetReader {
  class MonitorParser extends Parser with ParboiledWidgetParser.RichRule {
    def parserToMonitor(
      pos: (Int, Int, Int, Int),
      name: Option[String],
      precision: Int): Monitor = {
        Monitor(
          display = name,
          left = pos._1, top = pos._2, right = pos._3, bottom = pos._4,
          source = None,
          precision = precision, fontSize = 11)
    }

    def HubNetMonitor: Rule1[Monitor] = rule {
      MonitorHeader ~ NewLine ~
      PositionInformation ~
      NillableString ~ NewLine ~
      IgnoredLine ~
      IntValue ~ NewLine ~
      IntDigits ~~> parserToMonitor _
    }

    def MonitorHeader: Rule0 = rule { "MONITOR" }
  }

  def parser = new MonitorParser {
    override val buildParseTree = true
  }

  type T = Monitor

  type ParsedWidget = Monitor

  def classTag: ClassTag[T] = ClassTag(classOf[Monitor])

  override def parseRule = parser.HubNetMonitor

  def format(monitor: T): String = {
    Seq(
      "MONITOR",
      monitor.left.toString,
      monitor.top.toString,
      monitor.right.toString,
      monitor.bottom.toString,
      monitor.display.map(saveNillableString).getOrElse("NIL"),
      "NIL",
      monitor.precision,
      "1").mkString("", "\n", "\n")
  }
}


object HubNetSliderReader extends DefaultParboiledWidgetParser with WidgetReader {
  class SliderParser extends Parser with ParboiledWidgetParser.RichRule {
    def parserToSlider(
      pos: (Int, Int, Int, Int),
      name: Option[String],
      sliderParams: List[Double],
      units: Option[String],
      direction: Direction): Slider = {
        val List(min, max, value, inc) = sliderParams
        Slider(
          display = name.map(restoreLines),
          left = pos._1, top = pos._2,
          right = pos._3, bottom = pos._4,
          variable = name.map(restoreLines),
          min = min.toString,
          max = max.toString,
          default = value,
          step = inc.toString,
          units = units,
          direction = direction)
    }

    def HubNetSlider: Rule1[Slider] = rule {
      SliderHeader ~ NewLine ~
      PositionInformation ~
      NillableString ~ NewLine ~
      IgnoredLine ~
      nTimes(4, DoubleValue ~ NewLine) ~
      IgnoredLine ~
      NillableString ~ NewLine ~
      Direction ~~> parserToSlider _
    }

    def Direction: Rule1[Direction] = rule {
      "VERTICAL" ~ push(Vertical) | "HORIZONTAL" ~ push(Horizontal)
    }

    def SliderHeader: Rule0 = rule { "SLIDER" }
  }

  def parser = new SliderParser {
    override val buildParseTree = true
  }

  type T = Slider

  type ParsedWidget = Slider

  def classTag: ClassTag[T] = ClassTag(classOf[Slider])

  override def parseRule = parser.HubNetSlider

  def format(slider: T): String = {
    val savedName = slider.variable.map(stripLines).getOrElse("NIL")
    val directionString = if (slider.direction == Vertical) "VERTICAL" else "HORIZONTAL"
    Seq(
      "SLIDER",
      slider.left.toString,
      slider.top.toString,
      slider.right.toString,
      slider.bottom.toString,
      savedName,
      savedName,
      slider.min,
      slider.max,
      Dump.number(slider.default),
      slider.step,
      "1",
      slider.units.getOrElse("NIL"),
      directionString
    ).mkString("", "\n", "\n")
  }
}

object HubNetChooserReader
  extends ParboiledWidgetParser
  with DebuggingWidgetParser
  with WidgetReader {

  class ChooserParser(literalParser: Option[LiteralParser]) extends Parser with ParboiledWidgetParser.RichRule {
    def parserToChooser(
      pos: (Int, Int, Int, Int),
      name: Option[String], choices: List[Chooseable],
      selectedIndex: Int): Chooser = {
        val restoredVarName = name.map(restoreLines)
        Chooser(restoredVarName,
          left  = pos._1, top    = pos._2,
          right = pos._3, bottom = pos._4,
          display = restoredVarName, choices = choices,
          currentChoice = selectedIndex)
    }

    def HubNetChooser: Rule1[Chooser] = rule {
      ChooserHeader ~ NewLine ~
      PositionInformation ~
      NillableString ~ NewLine ~
      IgnoredLine ~
      LogoListRule ~ NewLine ~
      NonNegativeIntValue ~~> parserToChooser _
    }

    def LogoListRule: Rule1[List[Chooseable]] =
      rule {
        StringRule ~~> { (s: String) =>
          literalParser.map { p =>
              val escapedValues = restoreLines(s)
              p.readFromString(s"[$escapedValues]") match {
                case l: LogoList => l.scalaIterator.map(Chooseable.apply).toList
                case _           => throw new Exception("Invalid chooser list: " + s)
              }
          }.getOrElse(List[Chooseable]())
        }
      }

    def NonNegativeIntValue: Rule1[Int] =
      rule { oneOrMore(Digit) ~> (_.toInt) }

    def ChooserHeader: Rule0 = rule { "CHOOSER" }
  }

  type T = Chooser

  type ParsedWidget = Chooser

  def classTag: ClassTag[T] = ClassTag(classOf[Chooser])

  def validatingParseRule(lines: List[String]): Rule1[this.ParsedWidget] =
    new ChooserParser(None).HubNetChooser

  def parsingRule(lines: List[String], literalParser: LiteralParser): Rule1[this.ParsedWidget] =
    new ChooserParser(Some(literalParser)).HubNetChooser

  def format(chooser: T): String = {
    val savedVarName = saveNillableString(stripLines(chooser.varName))
    Seq(
      "CHOOSER",
      chooser.left.toString,
      chooser.top.toString,
      chooser.right.toString,
      chooser.bottom.toString,
      savedVarName,
      savedVarName,
      stripLines(chooser.choices.map(_.value).map(o => Dump.logoObject(o, true, false)).mkString(" ")),
      chooser.currentChoice.toString
    ).mkString("", "\n", "\n")
  }
}
