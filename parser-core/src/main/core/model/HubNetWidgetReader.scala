// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{
  Button, Chooser, Chooseable, WorldDimensions, Direction, Dump, Horizontal,
  LogoList, Monitor, Slider, Vertical, View }
import org.nlogo.core.{ LiteralParser, Widget }
import org.nlogo.core.StringEscaper.{ escapeString, unescapeString }

import scala.reflect.ClassTag

import OptionalString.OptionableString

object HubNetWidgetReader {
  trait HubNetWidgetBaseReader extends BaseWidgetReader

  object HubNetButtonReader extends HubNetWidgetBaseReader {
    type T = Button
    def classTag: ClassTag[Button] = ClassTag(classOf[Button])

    val definition = List(
      SpecifiedLine("BUTTON"),
      IntLine(),
      IntLine(),
      IntLine(),
      IntLine(),
      OptionalStringLine("NIL", false),
      SpecifiedLine("NIL"),
      SpecifiedLine("NIL"),
      SpecifiedLine("1"),
      SpecifiedLine("T"),
      SpecifiedLine("OBSERVER"),
      SpecifiedLine("NIL"),
      OptionalStringLine("NIL", false, s => s.length >= 1))

    def asList(button: Button): List[Any] =
      List(
        (),
        button.left,
        button.top,
        button.right,
        button.bottom,
        button.display.toOptionalString,
        (),
        (),
        (),
        (),
        (),
        (),
        button.actionKey.map(_.toString).toOptionalString)

    def asWidget(vals: List[Any], literalParser: LiteralParser): Button = {
      val List(_, left: Int, top: Int, right: Int, bottom: Int,
        display: OptionalString,
        _, _, _, _, _, _,
        actionKey: OptionalString) = vals
      Button(source = None, display = display.toOption,
        left = left, top = top, right = right, bottom = bottom,
        forever = false,
        actionKey = actionKey.toOption.map(_.head))
    }
  }

  object HubNetChooserReader extends HubNetWidgetBaseReader {
    type T = Chooser
    def classTag: ClassTag[T] = ClassTag(classOf[Chooser])

    def definition = List(
      new SpecifiedLine("CHOOSER"),
      IntLine(),
      IntLine(),
      IntLine(),
      IntLine(),
      OptionalStringLine("NIL", false), // display name
      OptionalStringLine("NIL", false), // varname. Always equal to display, but saved separately
      StringLine(), // choices
      IntLine()) // current choice

    def asList(chooser: Chooser): List[Any] =
      List(
        (),
        chooser.left,
        chooser.top,
        chooser.right,
        chooser.bottom,
        chooser.display.toOptionalString,
        chooser.display.toOptionalString,
        escapeString(chooser.choices.map(_.value).map(o => Dump.logoObject(o, true, false)).mkString(" ")),
        chooser.currentChoice)

    def asWidget(vals: List[Any], parser: LiteralParser): Chooser = {
      val List(_, left: Int, top: Int, right: Int, bottom: Int, display: OptionalString, _, choicesStr: String, currentChoice: Int) = vals

      val escapedChoices = unescapeString(choicesStr)
      val choices =
        parser.readFromString(s"[${escapedChoices}]") match {
          case l: LogoList => l.scalaIterator.map(Chooseable.apply).toList
          case _ => throw new Exception("Invalid chooser list: " + choicesStr)
        }
      Chooser(display.toOption, left, top, right, bottom, display.toOption, choices, currentChoice)
    }
  }

  object HubNetMonitorReader extends HubNetWidgetBaseReader {
    type T = Monitor
    def classTag: ClassTag[T] = ClassTag(classOf[Monitor])

    def definition = List(
      SpecifiedLine("MONITOR"),
      IntLine(),
      IntLine(),
      IntLine(),
      IntLine(),
      OptionalStringLine("NIL", false),
      OptionalStringLine("NIL", true),
      IntLine(),
      IntLine())

    def asList(monitor: Monitor): List[Any] =
      List(
        (),
        monitor.left,
        monitor.top,
        monitor.right,
        monitor.bottom,
        monitor.display.toOptionalString,
        monitor.source.toOptionalString,
        monitor.precision,
        1)

    def asWidget(vals: List[Any], parser: LiteralParser): Monitor = {
      val List(_, left: Int, top: Int, right: Int, bottom: Int, display: OptionalString,
        source: OptionalString, precision: Int, _) = vals

      Monitor(left = left, top = top, right = right, bottom = bottom, display = display.toOption, source = source.toOption, precision = precision,
        fontSize = 11)
    }
  }

  object HubNetSliderReader extends HubNetWidgetBaseReader {
    type T = Slider
    def classTag: ClassTag[T] = ClassTag(classOf[Slider])

    def definition = List(
      SpecifiedLine("SLIDER"),
      IntLine(),
      IntLine(),
      IntLine(),
      IntLine(),
      OptionalStringLine("NIL", true), // name
      OptionalStringLine("NIL", true), // duplicate of name
      DoubleLine(), // min
      DoubleLine(), // max
      DoubleLine(), // default
      DoubleLine(), // step
      ReservedLine("1"),
      OptionalStringLine("NIL", false), // units
      new MapLine(List(("HORIZONTAL", Horizontal), ("VERTICAL", Vertical))))

    def asList(slider: Slider): List[Any] = {
      List(
        (),
        slider.left,
        slider.top,
        slider.right,
        slider.bottom,
        slider.variable.toOptionalString,
        slider.variable.toOptionalString,
        slider.min.toDouble,
        slider.max.toDouble,
        slider.default,
        slider.step.toDouble,
        (),
        slider.units.toOptionalString,
        slider.direction)
    }

    def asWidget(vals: List[Any], parser: LiteralParser): Slider = {
      val List(_, left: Int, top: Int, right: Int, bottom: Int,
        display: OptionalString, _,
        min: Double, max: Double, default: Double, step: Double,
        _, units: OptionalString, direction: Direction) = vals

      Slider(display = display.toOption,
        left = left, top = top, right = right, bottom = bottom,
        variable = display.toOption,
        min = min.toString, max = max.toString, default = default, step = step.toString,
        units = units.toOption, direction = direction)
    }
  }

  object HubNetViewReader extends HubNetWidgetBaseReader {
    type T = View
    def classTag: ClassTag[T] = ClassTag(classOf[View])

    def definition = List(
      SpecifiedLine("VIEW"),
      IntLine(),
      IntLine(),
      IntLine(),
      IntLine(),
      ReservedLine("0"), // 5
      ReservedLine("0"), // 6
      ReservedLine("0"), // 7
      ReservedLine("1"), // 8
      ReservedLine("1"), // 9
      ReservedLine("1"), // 10
      ReservedLine("1"), // 11
      ReservedLine("1"), // 12
      ReservedLine("0"), // 13
      ReservedLine("1"), // 14
      ReservedLine("1"), // 15
      ReservedLine("1"), // 16
      IntLine(),
      IntLine(),
      IntLine(),
      IntLine())

    def asList(view: View): List[Any] =
      List(
        (),
        view.left,
        view.top,
        view.right,
        view.bottom,
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        (),
        view.dimensions.minPxcor,
        view.dimensions.maxPxcor,
        view.dimensions.minPycor,
        view.dimensions.maxPycor)

    def asWidget(vals: List[Any], parser: LiteralParser): View = {
      val List(_, left: Int, top: Int, right: Int, bottom: Int,
        _, _, _, _, _, _, _, _, _, _, _, _,
        minPx: Int, maxPx: Int, minPy: Int, maxPy: Int) = vals

      View(
        left = left, top = top, right = right, bottom = bottom,
        dimensions = WorldDimensions(minPx, maxPx, minPy, maxPy))
    }
  }

  val defaultReaders = Map(
    "BUTTON"   -> HubNetButtonReader,
    "CHOOSER"  -> HubNetChooserReader,
    "INPUTBOX" -> InputBoxReader,
    "MONITOR"  -> HubNetMonitorReader,
    "OUTPUT"   -> OutputReader,
    "PLOT"     -> PlotReader,
    "SLIDER"   -> HubNetSliderReader,
    "SWITCH"   -> SwitchReader,
    "TEXTBOX"  -> TextBoxReader,
    "VIEW"     -> HubNetViewReader
  )

  def read(lines: List[String], parser: LiteralParser,
    additionalReaders: Map[String, WidgetReader] = Map(),
    conversion: String => String = identity): Widget = {
      val readers = (defaultReaders ++ additionalReaders).values
      readers.find(_.validate(lines)) match {
        case Some(reader) => reader.parse(lines, parser).convertSource(conversion)
        case None =>
          throw new RuntimeException(s"Couldn't find corresponding reader for ${lines.head}")
      }
  }

  def format(widget: Widget, additionalReaders: Map[String, WidgetReader] = Map()): String = {
    (defaultReaders ++ additionalReaders)
      .values
      .flatMap(r => r.classTag.unapply(widget).map(w => r.format(w)))
      .headOption
      .getOrElse(
        throw new UnsupportedOperationException("Widget type is not supported: " + widget.getClass.getName)
      )
  }
}
