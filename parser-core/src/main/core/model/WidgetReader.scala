// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core
import org.nlogo.core.StringEscaper.unescapeString
import org.nlogo.core.StringEscaper.escapeString
import org.nlogo.core._

import scala.reflect.ClassTag
import scala.annotation.tailrec

// parse and valid are separated for clarity later on in the overarching reader, FD 4/16/14
trait WidgetLine[T] {
  def parse(line: String): T
  def format(v: T): String
  def valid(v: String): Boolean
  def default(): Option[T] = None
}

case class IntLine(override val default: Option[Int] = None) extends WidgetLine[Int] {
  def parse(line: String): Int = line.toInt
  def format(v: Int): String = v.toString()
  def valid(v: String): Boolean = util.Try(v.toInt).isSuccess
}
case class BooleanLine(override val default: Option[Boolean] = None) extends WidgetLine[Boolean] {
  def parse(line: String): Boolean = line == "1"
  def format(v: Boolean): String = if(v) "1" else "0"
  def valid(v: String): Boolean = v == "1" || v == "0"
}
case class InvertedBooleanLine(override val default: Option[Boolean] = None) extends WidgetLine[Boolean] {
  def parse(line: String): Boolean = line == "0"
  def format(v: Boolean): String = if(v) "0" else "1"
  def valid(v: String): Boolean = v == "0" || v == "1"
}
case class StringBooleanLine(override val default: Option[Boolean] = None) extends WidgetLine[Boolean] {
  def parse(line: String): Boolean = line == "true"
  def format(v: Boolean): String = if(v) "true" else "false"
  def valid(v: String): Boolean = v == "true" || v == "false"
}
case class TNilBooleanLine(override val default: Option[Boolean] = None) extends WidgetLine[Boolean] {
  def parse(line: String): Boolean = line == "T"
  def format(v: Boolean): String = if(v) "T" else "NIL"
  def valid(v: String): Boolean = v == "T" || v == "NIL"
}
case class DoubleLine(override val default: Option[Double] = None) extends WidgetLine[Double] {
  type T = Double
  def parse(line: String): Double = line.toDouble
  def format(v: Double): String = v.toString
  def valid(v: String): Boolean = util.Try(v.toDouble).isSuccess
}
case class StringLine(override val default: Option[String] = None) extends WidgetLine[String] {
  def parse(line: String): String = line
  def format(v: String): String = v
  def valid(v: String): Boolean = true
}
case class CharLine(override val default: Option[Char] = None) extends WidgetLine[Char] {
  def parse(line: String): Char = line(0)
  def format(v: Char): String = v.toString
  def valid(v: String): Boolean = v.length >= 1
}
case class EscapedStringLine(override val default: Option[String] = None) extends WidgetLine[String] {
  def parse(line: String): String = unescapeString(line)
  def format(v: String): String = escapeString(v)
  def valid(v: String): Boolean = true
}
case class OptionalEscapedStringLine(override val default: Option[String] = None) extends WidgetLine[String] {
  def parse(line: String): String = if (line == "NIL") "" else unescapeString(line)
  def format(v: String): String = if (v.isEmpty) "NIL" else escapeString(v)
  def valid(v: String): Boolean = !v.isEmpty
}
case class SpecifiedLine(str: String) extends WidgetLine[Unit] {
  def parse(line: String): Unit = {}
  def format(x: Unit): String = str
  def valid(v: String): Boolean = v == str
}
case class MapLine[T](map: List[Tuple2[String, T]]) extends WidgetLine[T] {
  def parse(line: String): T =
    map.collectFirst{ case (k, v) if k == line => v }.get
  def format(valueToFormat: T): String =
    map.collectFirst{ case (k, v) if v == valueToFormat => k}.get
  def valid(v: String): Boolean = map.toMap.contains(v)
}
case class ReservedLine(output: String = "RESERVED") extends WidgetLine[Unit] {
  def parse(line: String): Unit = {}
  def format(x: Unit): String = output
  def valid(v: String): Boolean = true
}
case class OptionLine[T](noneLine: String, someLineReader: WidgetLine[T]) extends WidgetLine[Option[T]] {
  def parse(line: String): Option[T] = if(noneLine == line) None else Some(someLineReader.parse(line))
  def format(x: Option[T]): String =
    x match {
      case None => noneLine
      case Some(t) => someLineReader.format(t)
    }
  def valid(v: String): Boolean = (noneLine == v) || someLineReader.valid(v)
}

trait WidgetReader {
  type T <: Widget
  def classTag: ClassTag[T]
  def format(t: T): String
  def validate(lines: List[String]): Boolean
  def parse(lines: List[String], literalParser: LiteralParser): T
}

object WidgetReader {
    val defaultReaders = Map[String, WidgetReader](
      "BUTTON"          -> ButtonReader,
      "SLIDER"          -> SliderReader,
      "GRAPHICS-WINDOW" -> ViewReader,
      "MONITOR"         -> MonitorReader,
      "SWITCH"          -> SwitchReader,
      "PLOT"            -> PlotReader,
      "CHOOSER"         -> ChooserReader,
      "OUTPUT"          -> OutputReader,
      "TEXTBOX"         -> TextBoxReader,
      "INPUTBOX"        -> InputBoxReader
    )

  def read(lines: List[String], parser: LiteralParser,
    additionalReaders: Map[String, WidgetReader] = Map(),
    conversion: String => String = identity): Widget = {
    val readers = (defaultReaders ++ additionalReaders).values
    readers.find(_.validate(lines)) match {
      case Some(reader) => reader.parse(lines, parser).convertSource(conversion)
      case None =>
        throw new RuntimeException(
          s"Couldn't find corresponding reader for ${lines.head}")
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

  def readInterface(lines: List[String], parser: LiteralParser,
    additionalReaders: Map[String, WidgetReader] = Map(),
    conversion: String => String): List[Widget] = {
    var widgets = Vector[Widget]()
    var widgetLines = Vector[String]()
    for(line <- lines)
      if(line.nonEmpty)
        widgetLines :+= line
      else {
        if(!widgetLines.forall(_.isEmpty))
          widgets :+= read(widgetLines.toList, parser, additionalReaders, conversion)
        widgetLines = Vector()
      }
    if(!widgetLines.forall(_.isEmpty))
      widgets :+= read(widgetLines.toList, parser, additionalReaders, conversion)

    widgets.toList
  }

  def formatInterface(widgets: Seq[Widget]): String =
    widgets.map(format(_)).mkString("\n\n")
}

abstract class BaseWidgetReader extends WidgetReader {
  type T <: Widget
  def definition: List[WidgetLine[_]]
  def asList(t: T): List[Any]
  def asWidget(vals: List[Any], literalParser: LiteralParser): T
  def format(t: T): String = {
    (definition.asInstanceOf[List[WidgetLine[Any]]].zip(asList(t)).map{case (d, v) => d.format(v)}).mkString("\n")
  }
  def validate(lines: List[String]): Boolean = {
    (lines.size == definition.size ||
     (definition.indexWhere(_.default.nonEmpty) != -1 &&
      lines.size >= definition.indexWhere(_.default.nonEmpty))) &&
    (definition zip lines).forall{case (d, l) => d.valid(l)}
  }
  def parse(lines: List[String], literalParser: LiteralParser): T =
    asWidget(definition.asInstanceOf[List[WidgetLine[AnyRef]]].zipWithIndex.map{case (d, idx) =>
      if(idx < lines.size) d.parse(lines(idx)) else d.default.get }, literalParser)
}

object ButtonReader extends BaseWidgetReader {
  type T = Button
  def classTag: ClassTag[Button] = ClassTag(classOf[Button])
  def definition = List(new SpecifiedLine("BUTTON"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        OptionLine[String]("NIL", StringLine()),   // rawDisplay
                        OptionLine[String]("NIL", EscapedStringLine()),   // code to execute
                        TNilBooleanLine(),  // forever?
                        ReservedLine("1"),
                        ReservedLine("T"),
                        MapLine(List(
                          "OBSERVER" -> AgentKind.Observer,
                          "PATCH"    -> AgentKind.Patch,
                          "TURTLE"   -> AgentKind.Turtle,
                          "LINK"     -> AgentKind.Link)), // buttonKind
                        ReservedLine("NIL"),
                        OptionLine[Char]("NIL", CharLine()),  // actionkey
                        ReservedLine("NIL"),
                        ReservedLine("NIL"),
                        IntLine()  // Enabled before ticks start implemented as an int
                      )
  def asList(button: Button) = List((), button.left, button.top, button.right, button.bottom, button.display,
                                    button.source, button.forever, (), (), button.buttonKind, (), button.actionKey,
                                    (), (), if(button.disableUntilTicksStart) 0 else 1)
  def asWidget(vals: List[Any], literalParser: LiteralParser): Button = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, rawDisplay: Option[String] @unchecked,
      source: Option[String] @unchecked, forever: Boolean, _, _, buttonKind: AgentKind, _, actionKey: Option[Char] @unchecked, _, _,
      enabledBeforeTicks: Int) = vals
    Button(source, left, top, right, bottom, rawDisplay, forever, buttonKind, actionKey, enabledBeforeTicks == 0)
  }
}

object PenReader {
  // This is tricky because the string literals may contain escaped double quotes, so it's
  // nontrivial to figure out where one literal ends and the next starts.  Assumes the
  // opening double quote has already been detected and discarded.
  // There are some models (Scatter in particular) where recursion here can cause
  // a StackOverflowError in certain circumstances.
  // @tailrec protects against this problem cropping up without warning.
  @tailrec private def parseOne(s: String, stringLiteral: String = ""): (String, String) =
    if(s.isEmpty)
      (stringLiteral, s)
    else if (s.head == '"')
      (stringLiteral, s.tail.trim)
    else if(s.take(2) == "\\\"")
      parseOne(s.drop(2), stringLiteral + "\"")
    else
      parseOne(s.tail, stringLiteral + s.head.toString)
  def quoted(s:String) = '"' + s + '"'
  def parseStringLiterals(s: String): List[String] =
    s.headOption match {
      case Some('"') =>
        val (result, more) = parseOne(s.tail)
        result :: parseStringLiterals(more)
      case _ =>
        Nil
    }

  def parse(line: String, literalParser: LiteralParser): Pen = {
    require(line.head == '"')
    val (display, rest) = parseOne(line.tail)
    val (rest1, rest2) = rest.span(_ != '"')
    val List(interval, mode, color, inLegend) =
      rest1.trim.split("\\s+").toList
    require(PlotPenInterface.isValidPlotPenMode(mode.toInt))
    // optional; pre-5.0 models don't have them
    val (setup, update) =
      parseStringLiterals(rest2) match {
        case List(setup, update) =>
          (setup, update)
        case _ =>
          ("", "")
      }
    Pen(display = unescapeString(display), interval = interval.toDouble, mode = mode.toInt, color = color.toInt,
        inLegend = inLegend.toBoolean, setupCode = unescapeString(setup), updateCode = unescapeString(update))
  }

  def format(pen: Pen): String = quoted(pen.display) + " " + pen.interval + " " + pen.mode + " " + pen.color + " " + pen.inLegend + " " +
    quoted(escapeString(pen.setupCode)) + " " + quoted(escapeString(pen.updateCode))
}

object PlotReader extends BaseWidgetReader {
  type T = Plot
  def classTag: ClassTag[T] = ClassTag(classOf[Plot])

  def definition = List(new SpecifiedLine("PLOT"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        OptionLine[String]("NIL", StringLine()),  // display
                        OptionLine[String]("NIL", StringLine()),  // xaxis
                        OptionLine[String]("NIL", StringLine()),  // yaxis
                        DoubleLine(),   // xmin
                        DoubleLine(),   // xmax
                        DoubleLine(),   // ymin
                        DoubleLine(),   // ymax
                        StringBooleanLine(), // autoploton
                        StringBooleanLine(), // legend on
                        StringLine(Some(""""" """""))   // Double code lines, parse later
                      )
  def asList(plot: Plot) = List((), plot.left, plot.top, plot.right, plot.bottom, plot.display,
                                    plot.xAxis, plot.yAxis, plot.xmin, plot.xmax, plot.ymin, plot.ymax,
                                    plot.autoPlotOn, plot.legendOn,
                                    "\"" + escapeString(plot.setupCode) + "\" \"" + escapeString(plot.updateCode) + "\"")
  def asWidget(vals: List[Any], literalParser: LiteralParser): Plot = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: Option[String] @unchecked,
      xAxis: Option[String] @unchecked, yAxis: Option[String] @unchecked, xmin: Double, xmax: Double, ymin: Double, ymax: Double,
      autoPlotOn: Boolean, legendOn: Boolean, code: String, pens: List[Pen] @unchecked) = vals
    val List(setupCode: String, updateCode: String) = PenReader.parseStringLiterals(code)
    Plot(display, left, top, right, bottom, xAxis, yAxis, xmin, xmax, ymin, ymax, autoPlotOn, legendOn,
         unescapeString(setupCode), unescapeString(updateCode), pens)
  }

  override def format(plot: Plot): String = {
    (definition.asInstanceOf[List[WidgetLine[Any]]].zip(asList(plot)).map{case (d, v) => d.format(v)}).mkString("\n") +
      "\nPENS\n" +
      plot.pens.map(PenReader.format(_)).mkString("\n")
  }
  override def validate(lines: List[String]): Boolean = super.validate(lines.toList.span(_ != "PENS")._1)
  override def parse(lines: List[String], literalParser: LiteralParser): Plot = {
    val (plotLines, penLines) = lines.span(_ != "PENS")
    val pens = if (penLines.size > 1) penLines.tail.map(PenReader.parse(_, literalParser)) else Nil
    asWidget(definition.asInstanceOf[List[WidgetLine[AnyRef]]].zipWithIndex.map{case (d, idx) =>
      if(idx < plotLines.size) d.parse(plotLines(idx)) else d.default.get } :+ pens, literalParser)
  }
}

object SliderReader extends BaseWidgetReader {
  type T = Slider
  def classTag: ClassTag[T] = ClassTag(classOf[Slider])

  def definition = List(
    new SpecifiedLine("SLIDER"),
    IntLine(),  // left
    IntLine(),  // top
    IntLine(),  // right
    IntLine(),  // bottom
    OptionLine[String]("NIL", StringLine()), // display
    OptionLine[String]("NIL", StringLine()), // varname
    StringLine(),   // min
    StringLine(),   // max
    DoubleLine(),    // default
    StringLine(),   // step
    ReservedLine("1"),
    OptionLine[String]("NIL", StringLine()),   // units
    new MapLine(List(("HORIZONTAL", Horizontal), ("VERTICAL", Vertical))))
  def asList(slider: Slider) = List((), slider.left, slider.top, slider.right, slider.bottom, slider.display,
                                    slider.variable, slider.min, slider.max, slider.default, slider.step,
                                    (), slider.units, slider.direction)
  def asWidget(vals: List[Any], literalParser: LiteralParser): Slider = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: Option[String] @unchecked, varName: Option[String] @unchecked, min: String,
             max: String, default: Double, step: String, _, units: Option[String] @unchecked, direction: Direction) = vals
    Slider(varName, left, top, right, bottom, display, min, max, default, step, units, direction)
  }
}

object TextBoxReader extends BaseWidgetReader {
  type T = TextBox
  def classTag: ClassTag[T] = ClassTag(classOf[TextBox])

  def definition = List(new SpecifiedLine("TEXTBOX"),
                        IntLine(),           // left
                        IntLine(),           // top
                        IntLine(),           // right
                        IntLine(),           // bottom
                        OptionLine[String]("NIL", EscapedStringLine()), // display
                        IntLine(),           // font size
                        DoubleLine(),        // color
                        BooleanLine()        // transparent
                      )
  def asList(textBox: TextBox) = List((), textBox.left, textBox.top, textBox.right, textBox.bottom,
                                    textBox.display, textBox.fontSize, textBox.color, textBox.transparent)
  def asWidget(vals: List[Any], literalParser: LiteralParser): TextBox = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: Option[String] @unchecked, fontSize: Int, color: Double, transparent: Boolean) = vals
    TextBox(display, left, top, right, bottom, fontSize, color, transparent)
  }
}

object SwitchReader extends BaseWidgetReader {
  type T = Switch
  def classTag: ClassTag[T] = ClassTag(classOf[Switch])

  def definition = List(
    new SpecifiedLine("SWITCH"),
    IntLine(),  // left
    IntLine(),  // top
    IntLine(),  // right
    IntLine(),  // bottom
    OptionLine[String]("NIL", StringLine()),   // display
    OptionLine[String]("NIL", StringLine()),   // varname
    InvertedBooleanLine(),  // on
    ReservedLine("1"),
    ReservedLine("-1000"))
  def asList(switch: Switch) = List((), switch.left, switch.top, switch.right, switch.bottom,
                                    switch.display, switch.variable, switch.on, (), ())
  def asWidget(vals: List[Any], literalParser: LiteralParser): Switch = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: Option[String] @unchecked, varName: Option[String] @unchecked, on: Boolean, _, _) = vals
    Switch(varName, left, top, right, bottom, display, on)
  }
}

object ChooserReader extends BaseWidgetReader {
  type T = Chooser
  def classTag: ClassTag[T] = ClassTag(classOf[Chooser])

  def definition = List(
    new SpecifiedLine("CHOOSER"),
    IntLine(),  // left
    IntLine(),  // top
    IntLine(),  // right
    IntLine(),  // bottom
    OptionLine[String]("NIL", EscapedStringLine()), // display
    OptionLine[String]("NIL", StringLine()), // varname
    StringLine(),   // choices
    IntLine())   // current choice

  def asList(chooser: Chooser) = List((), chooser.left, chooser.top, chooser.right, chooser.bottom, chooser.display,
    chooser.variable, chooser.choices.map(v => Dump.logoObject(v.value, true, false)).mkString(" "), chooser.currentChoice)

  def asWidget(vals: List[Any], parser: LiteralParser): Chooser = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: Option[String] @unchecked, variable: Option[String] @unchecked,
    choicesStr: String, currentChoice: Int) = vals

    val choices = parser.readFromString(s"[$choicesStr]").asInstanceOf[LogoList]

    def convertAllNobodies(l: AnyRef): AnyRef = l match {
      case Nobody       => "nobody"
      case ll: LogoList => LogoList(ll.map(convertAllNobodies): _*)
      case other        => other
    }

    Chooser(variable, left, top, right, bottom, display, choices.map(convertAllNobodies).map(Chooseable(_)).toList, currentChoice)
  }
}

object MonitorReader extends BaseWidgetReader {
  type T = Monitor
  def classTag: ClassTag[T] = ClassTag(classOf[Monitor])

  def definition = List(
    new SpecifiedLine("MONITOR"),
    IntLine(),  // left
    IntLine(),  // top
    IntLine(),  // right
    IntLine(),  // bottom
    OptionLine[String]("NIL", StringLine()),   // rawDisplay
    OptionLine[String]("NIL", EscapedStringLine()),   // source
    IntLine(),   // precision
    ReservedLine("1"),
    IntLine())    // font size
  def asList(monitor: Monitor) = List((), monitor.left, monitor.top, monitor.right, monitor.bottom, monitor.display,
    monitor.source, monitor.precision, (), monitor.fontSize)
  def asWidget(vals: List[Any], literalParser: LiteralParser): Monitor = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int,
      rawDisplay: Option[String] @unchecked,
      source: Option[String] @unchecked, precision: Int, _, fontSize: Int) = vals
    Monitor(source, left, top, right, bottom, rawDisplay, precision, fontSize)
  }
}

object OutputReader extends BaseWidgetReader {
  type T = Output
  def classTag: ClassTag[T] = ClassTag(classOf[Output])

  def definition = List(new SpecifiedLine("OUTPUT"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        IntLine()    // font size
                      )
  def asList(output: Output) = List((), output.left, output.top, output.right, output.bottom, output.fontSize)
  def asWidget(vals: List[Any], literalParser: LiteralParser): Output = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, fontSize: Int) = vals
    Output(left, top, right, bottom, fontSize)
  }
}

object InputBoxReader extends BaseWidgetReader {
  type T = InputBox

  def classTag: ClassTag[T] = ClassTag(classOf[InputBox])

  def definition = List(
    new SpecifiedLine("INPUTBOX"),
    IntLine(),  // left
    IntLine(),  // top
    IntLine(),  // right
    IntLine(),  // bottom
    OptionLine[String]("NIL", StringLine()),   // varname
    OptionalEscapedStringLine(),   // value
    ReservedLine("1"),
    BooleanLine(),  // multiline
    StringLine())    // inputboxtype

  def asList(inputbox: InputBox) = List((), inputbox.left, inputbox.top, inputbox.right, inputbox.bottom, inputbox.variable,
    inputbox.boxedValue.asString, (), inputbox.boxedValue.multiline, inputbox.boxedValue.name)
  def asWidget(vals: List[Any], literalParser: LiteralParser): InputBox = {

    val List((), left: Int, top: Int, right: Int, bottom: Int, variable: Option[String] @unchecked, value: String,
      _, multiline: Boolean, inputBoxTypeStr: String) = vals

    val inputBoxValue = inputBoxTypeStr match {
      case "Number" | "Color" => NumericInput(value.toDouble, NumericInput.label(inputBoxTypeStr))
      case "String" | "String (reporter)" | "String (commands)" => StringInput(value, StringInput.label(inputBoxTypeStr), multiline)
      case _ =>
        throw new RuntimeException("Couldn't find corresponding input box type for " + inputBoxTypeStr)
    }
    InputBox(variable, left, top, right, bottom, inputBoxValue)
  }
}

object ViewReader extends BaseWidgetReader {
  type T = View
  def classTag: ClassTag[T] = ClassTag(classOf[View])

  def definition = List(new SpecifiedLine("GRAPHICS-WINDOW"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        ReservedLine("-1"), // maxPxCor or -1
                        ReservedLine("-1"), // maxPyCor or -1
                        DoubleLine(),    // patchsize
                        ReservedLine("1"), // shapes on, not used
                        IntLine(),    // font size

                        ReservedLine("1"), // hex settings
                        ReservedLine("1"), // and
                        ReservedLine("1"), // exactDraw
                        ReservedLine("0"), // not used

                        BooleanLine(),  // wrappingAllowedInX
                        BooleanLine(),  // wrappingAllowedInY
                        ReservedLine("1"), // thin turtle pens!  Always on
                        IntLine(),  // minPxcor
                        IntLine(),  // maxPxcor
                        IntLine(),  // minPycor
                        IntLine(),  // maxPycor
                        new MapLine(List(("0", UpdateMode.Continuous), ("1", UpdateMode.TickBased))),
                        new MapLine(List(("0", UpdateMode.Continuous), ("1", UpdateMode.TickBased))), // Twice for compatibility
                        BooleanLine(),  // showTickCounter
                        OptionLine[String]("NIL", StringLine()), // tick counter label
                        DoubleLine(Some(30))   // frame rate
                      )
  def asList(view: View) = List((), view.left, view.top, view.right, view.bottom, (), (), view.patchSize, (), view.fontSize,
    (), (), (), (), view.wrappingAllowedInX, view.wrappingAllowedInY, (),
    view.minPxcor, view.maxPxcor, view.minPycor, view.maxPycor,
    view.updateMode, view.updateMode, view.showTickCounter, view.tickCounterLabel, view.frameRate)
  def asWidget(vals: List[Any], literalParser: LiteralParser): View = {
    val (_ :: (left: Int) :: (top: Int) :: (right: Int) :: (bottom: Int) :: _ :: _ :: (patchSize: Double) :: _ ::
         (fontSize: Int) :: _ :: _ :: _ :: _ :: (wrappingAllowedInX: Boolean) :: (wrappingAllowedInY: Boolean) ::
         _ :: (minPxcor: Int) :: (maxPxcor: Int) :: (minPycor: Int) :: (maxPycor: Int) :: (updateMode: UpdateMode) ::
         _ :: (showTickCounter: Boolean) :: (tickCounterLabel: Option[String] @unchecked) :: (frameRate: Double) :: Nil) = vals
    View(left, top, right, bottom,
      new WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor, patchSize, wrappingAllowedInX, wrappingAllowedInY),
        fontSize, updateMode, showTickCounter, tickCounterLabel, frameRate)
  }
}
