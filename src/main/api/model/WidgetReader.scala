// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.nlogo.api
import org.nlogo.core.StringEscaper.unescapeString
import org.nlogo.core.StringEscaper.escapeString
import org.nlogo.core._

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
case class SpecifiedLine(str: String) extends WidgetLine[Unit] {
  def parse(line: String): Unit = {}
  def format(x: Unit): String = str
  def valid(v: String): Boolean = v == str
}
case class MapLine[T](map: List[Tuple2[String, T]]) extends WidgetLine[T] {
  def parse(line: String): T = map.collectFirst{case (v, x) => x}.get
  def format(v: T): String = map.collectFirst{case (x, v) => x}.get
  def valid(v: String): Boolean = map.toMap.contains(v)
}
case class ReservedLine(output: String = "RESERVED") extends WidgetLine[Unit] {
  def parse(line: String): Unit = {}
  def format(x: Unit): String = output
  def valid(v: String): Boolean = true
}

trait WidgetReader {
  type T <: Widget
  def format(t: T): String
  def validate(lines: List[String]): Boolean
  def parse(lines: List[String]): T
}

object WidgetReader {
  def read(lines: List[String], parser: api.ParserServices): Widget = {
    val readers = List(ButtonReader, SliderReader, ViewReader, MonitorReader, SwitchReader, PlotReader, ChooserReader(parser),
      OutputReader, TextBoxReader, new InputBoxReader())
    readers.find(_.validate(lines)) match {
      case Some(reader) => reader.parse(lines)
      case None =>
        System.out.println(lines)
        throw new RuntimeException(
          "Couldn't find corresponding reader!")
    }
  }

  def format(widget: Widget, parser: api.ParserServices): String =
    widget match {
      case b: Button => ButtonReader.format(b)
      case s: Slider => SliderReader.format(s)
      case v: View => ViewReader.format(v)
      case m: Monitor => MonitorReader.format(m)
      case s: Switch => SwitchReader.format(s)
      case p: Plot => PlotReader.format(p)
      case c: Chooser => new ChooserReader(parser).format(c)
      case o: Output => OutputReader.format(o)
      case t: TextBox => TextBoxReader.format(t)
      case i: InputBox[_] => new InputBoxReader{type U = Any}.format(i.asInstanceOf[InputBox[Any]])
      case _ => throw new UnsupportedOperationException("Widget type is not supported: " + widget.getClass.getName)
    }

  def readInterface(lines: List[String], parser: api.ParserServices): List[Widget] = {
    var widgets = Vector[Widget]()
    var widgetLines = Vector[String]()
    for(line <- lines)
      if(line.nonEmpty)
        widgetLines :+= line
      else {
        if(!widgetLines.forall(_.isEmpty))
          widgets :+= read(widgetLines.toList, parser)
        widgetLines = Vector()
      }
    if(!widgetLines.forall(_.isEmpty))
      widgets :+= read(widgetLines.toList, parser)

    widgets.toList
  }

  def formatInterface(widgets: List[Widget], parser: api.ParserServices): String =
    widgets.map(format(_, parser)).mkString("\n\n")
}

abstract class BaseWidgetReader extends WidgetReader {
  type T <: Widget
  def definition: List[WidgetLine[_]]
  def asList(t: T): List[Any]
  def asWidget(vals: List[Any]): T
  def format(t: T): String = {
    (definition.asInstanceOf[List[WidgetLine[Any]]].zip(asList(t)).map{case (d, v) => d.format(v)}).mkString("\n")
  }
  def validate(lines: List[String]): Boolean = {
    (lines.size == definition.size ||
     (definition.indexWhere(_.default.nonEmpty) != -1 &&
      lines.size >= definition.indexWhere(_.default.nonEmpty))) &&
    (definition zip lines).forall{case (d, l) => d.valid(l)}
  }
  def parse(lines: List[String]): T =
    asWidget(definition.asInstanceOf[List[WidgetLine[AnyRef]]].zipWithIndex.map{case (d, idx) =>
      if(idx < lines.size) d.parse(lines(idx)) else d.default.get })
}

object ButtonReader extends BaseWidgetReader {
  type T = Button
  def definition = List(new SpecifiedLine("BUTTON"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        StringLine(),   // display
                        StringLine(),   // code to execute
                        TNilBooleanLine(),  // forever?
                        ReservedLine(),
                        ReservedLine(),
                        StringLine(),   // buttonType
                        ReservedLine(),
                        StringLine(),  // actionkey
                        ReservedLine(),
                        ReservedLine(),
                        BooleanLine(Some(true))  // go time
                      )
  def asList(button: Button) = List((), button.left, button.top, button.right, button.bottom, button.display,
                                    button.source, button.forever, (), (), button.buttonType, (), button.actionKey, (), (), true)
  def asWidget(vals: List[Any]): Button = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: String,
      source: String, forever: Boolean, _, _, buttonType: String, _, actionKey: String, _, _, _) = vals
    Button(display, left, top, right, bottom, source, forever, buttonType, actionKey)
  }
}

object PenReader {
  // This is tricky because the string literals may contain escaped double quotes, so it's
  // nontrivial to figure out where one literal ends and the next starts.  Assumes the
  // opening double quote has already been detected and discarded.
  private def parseOne(s: String): (String, String) =
    if(s.isEmpty)
      ("", "")
    else if (s.head == '"')
      ("", s.tail.trim)
    else if(s.take(2) == "\\\"")
      parseOne(s.drop(2)) match {
        case (more1, more2) =>
          ('"' + more1, more2)
      }
    else
      parseOne(s.tail) match {
        case (more1, more2) =>
          (s.head + more1, more2)
      }
  def quoted(s:String) = '"' + s + '"'
  def parseStringLiterals(s: String): List[String] =
    s.headOption match {
      case Some('"') =>
        val (result, more) = parseOne(s.tail)
        result :: parseStringLiterals(more)
      case _ =>
        Nil
    }

  def parse(line: String): Pen = {
    require(line.head == '"')
    val (display, rest) = parseOne(line.tail)
    val (rest1, rest2) = rest.span(_ != '"')
    val List(interval, mode, color, inLegend) =
      rest1.trim.split("\\s+").toList
    require(api.PlotPenInterface.isValidPlotPenMode(mode.toInt))
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

  def definition = List(new SpecifiedLine("PLOT"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        StringLine(),  // display
                        StringLine(),  // xaxis
                        StringLine(),  // yaxis
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
  def asWidget(vals: List[Any]): Plot = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: String,
      xAxis: String, yAxis: String, xmin: Double, xmax: Double, ymin: Double, ymax: Double,
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
  override def parse(lines: List[String]): Plot = {
    val (plotLines, penLines) = lines.span(_ != "PENS")
    val pens = if (penLines.size > 1) penLines.tail.map(PenReader.parse(_)) else Nil
    asWidget(definition.asInstanceOf[List[WidgetLine[AnyRef]]].zipWithIndex.map{case (d, idx) =>
      if(idx < plotLines.size) d.parse(plotLines(idx)) else d.default.get } :+ pens)
  }
}

object SliderReader extends BaseWidgetReader {
  type T = Slider

  def definition = List(new SpecifiedLine("SLIDER"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        StringLine(),   // display
                        StringLine(),   // varname
                        StringLine(),   // min
                        StringLine(),   // max
                        DoubleLine(),    // default
                        StringLine(),   // step
                        ReservedLine(),
                        StringLine(),   // units
                        new MapLine(List(("HORIZONTAL", Horizontal), ("VERTICAL", Vertical)))
                      )
  def asList(slider: Slider) = List((), slider.left, slider.top, slider.right, slider.bottom, slider.display,
                                    slider.varName, slider.min, slider.max, slider.default, slider.step,
                                    (), slider.units, slider.direction)
  def asWidget(vals: List[Any]): Slider = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: String, varName: String, min: String,
             max: String, default: Double, step: String, _, units: String, direction: Direction) = vals
    Slider(display, left, top, right, bottom, varName, min, max, default, step, units, direction)
  }
}

object TextBoxReader extends BaseWidgetReader {
  type T = TextBox

  def definition = List(new SpecifiedLine("TEXTBOX"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        StringLine(),   // display
                        IntLine(),   // font size
                        DoubleLine(), // color
                        BooleanLine()  // transparent
                      )
  def asList(textBox: TextBox) = List((), textBox.left, textBox.top, textBox.right, textBox.bottom,
                                    textBox.display, textBox.fontSize, textBox.color, textBox.transparent)
  def asWidget(vals: List[Any]): TextBox = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: String, fontSize: Int, color: Double, transparent: Boolean) = vals
    TextBox(display, left, top, right, bottom, fontSize, color, transparent)
  }
}

object SwitchReader extends BaseWidgetReader {
  type T = Switch

  def definition = List(new SpecifiedLine("SWITCH"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        StringLine(),   // display
                        StringLine(),   // varname
                        InvertedBooleanLine(),  // on
                        ReservedLine(),
                        ReservedLine()
                      )
  def asList(switch: Switch) = List((), switch.left, switch.top, switch.right, switch.bottom,
                                    switch.display, switch.varName, switch.on, (), ())
  def asWidget(vals: List[Any]): Switch = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: String, varName: String, on: Boolean, _, _) = vals
    Switch(display, left, top, right, bottom, varName, on)
  }
}

case class ChooserReader(val parser: api.ParserServices) extends BaseWidgetReader {
  type T = Chooser

  def definition = List(new SpecifiedLine("CHOOSER"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        StringLine(),   // display
                        StringLine(),   // varname
                        StringLine(),   // choices
                        IntLine()   // current choice
                      )
  def asList(chooser: Chooser) = List((), chooser.left, chooser.top, chooser.right, chooser.bottom, chooser.display,
    chooser.varName, chooser.choices.map(v => api.Dump.logoObject(v, true, false)).mkString(" "), chooser.currentChoice)
  def asWidget(vals: List[Any]): Chooser = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: String, varName: String,
      choicesStr: String, currentChoice: Int) = vals
    val choices = parser.readFromString("[" + choicesStr + "]").asInstanceOf[api.LogoList]

    Chooser(display, left, top, right, bottom, varName, choices.toList, currentChoice)
  }
}

object MonitorReader extends BaseWidgetReader {
  type T = Monitor

  def definition = List(new SpecifiedLine("MONITOR"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        StringLine(),   // display
                        StringLine(),   // source
                        IntLine(),   // precision
                        ReservedLine(),
                        IntLine()    // font size
                      )
  def asList(monitor: Monitor) = List((), monitor.left, monitor.top, monitor.right, monitor.bottom, monitor.display,
    monitor.source, monitor.precision, (), monitor.fontSize)
  def asWidget(vals: List[Any]): Monitor = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, display: String, source: String, precision: Int, _, fontSize: Int) = vals
    Monitor(display, left, top, right, bottom, source, precision, fontSize)
  }
}

object OutputReader extends BaseWidgetReader {
  type T = Output

  def definition = List(new SpecifiedLine("OUTPUT"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        IntLine()    // font size
                      )
  def asList(output: Output) = List((), output.left, output.top, output.right, output.bottom, output.fontSize)
  def asWidget(vals: List[Any]): Output = {
    val List(_, left: Int, top: Int, right: Int, bottom: Int, fontSize: Int) = vals
    Output(left, top, right, bottom, fontSize)
  }
}

class InputBoxReader extends BaseWidgetReader {
  type U
  type T = InputBox[U]

  val inputBoxTypes =
    List((Num, DoubleLine()), (Col, IntLine()), (Str, StringLine()), (StrCommand, StringLine()), (StrReporter, StringLine()))

  def definition = List(new SpecifiedLine("INPUTBOX"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        StringLine(),   // varname
                        StringLine(),   // value
                        BooleanLine(),  // multiline
                        ReservedLine(),
                        StringLine()    // inputboxtype
                      )
  def asList(inputbox: InputBox[U]) = List((), inputbox.left, inputbox.top, inputbox.right, inputbox.bottom, inputbox.varName,
    inputbox.value.toString, inputbox.multiline, (), inputbox.boxtype.name)
  def asWidget(vals: List[Any]): InputBox[U] = {

    val List((), left: Int, top: Int, right: Int, bottom: Int, varName: String, value: String,
      multiline: Boolean, _, inputBoxTypeStr: String) = vals
    val (inputBoxType: InputBoxType[U], widgetline: WidgetLine[U]) = inputBoxTypes.find(_._1.name == inputBoxTypeStr) match {
      case Some(t) => t.asInstanceOf[Tuple2[InputBoxType[U], WidgetLine[U]]]
      case None =>
        throw new RuntimeException(
          "Couldn't find corresponding input box type for " + inputBoxTypeStr)
    }
    InputBox(left, top, right, bottom, varName, widgetline.parse(value), multiline, inputBoxType)
  }
}

object ViewReader extends BaseWidgetReader {
  type T = View

  def definition = List(new SpecifiedLine("GRAPHICS-WINDOW"),
                        IntLine(),  // left
                        IntLine(),  // top
                        IntLine(),  // right
                        IntLine(),  // bottom
                        ReservedLine("-1"), // maxPxCor or -1
                        ReservedLine("-1"), // maxPyCor or -1
                        DoubleLine(),    // patchsize
                        ReservedLine(), // shapes on, not used
                        IntLine(),    // font size

                        ReservedLine(), // hex settings
                        ReservedLine(), // and
                        ReservedLine(), // exactDraw
                        ReservedLine(), // not used

                        BooleanLine(),  // wrappingAllowedInX
                        BooleanLine(),  // wrappingAllowedInY
                        ReservedLine(), // thin turtle pens!  Always on
                        IntLine(),  // minPxcor
                        IntLine(),  // maxPxcor
                        IntLine(),  // minPycor
                        IntLine(),  // maxPycor
                        new MapLine(List(("0", UpdateMode.Continuous), ("1", UpdateMode.TickBased))),
                        new MapLine(List(("0", UpdateMode.Continuous), ("1", UpdateMode.TickBased))), // Twice for compatibility
                        BooleanLine(),  // showTickCounter
                        StringLine(),   // tick counter label
                        DoubleLine(Some(30))   // frame rate
                      )
  def asList(view: View) = List((), view.left, view.top, view.right, view.bottom, (), (), view.patchSize, (), view.fontSize,
    (), (), (), (), view.wrappingAllowedInX, view.wrappingAllowedInY, (),
    view.minPxcor, view.maxPxcor, view.minPycor, view.maxPycor,
    view.updateMode, view.updateMode, view.showTickCounter, view.tickCounterLabel, view.frameRate)
  def asWidget(vals: List[Any]): View = {
    val (_ :: (left: Int) :: (top: Int) :: (right: Int) :: (bottom: Int) :: _ :: _ :: (patchSize: Double) :: _ ::
         (fontSize: Int) :: _ :: _ :: _ :: _ :: (wrappingAllowedInX: Boolean) :: (wrappingAllowedInY: Boolean) ::
         _ :: (minPxcor: Int) :: (maxPxcor: Int) :: (minPycor: Int) :: (maxPycor: Int) :: (updateMode: UpdateMode) ::
         _ :: (showTickCounter: Boolean) :: (tickCounterLabel: String) :: (frameRate: Double) :: Nil) = vals
    View(left, top, right, bottom, patchSize, fontSize,
      wrappingAllowedInX, wrappingAllowedInY, minPxcor, maxPxcor, minPycor, maxPycor,
      updateMode, showTickCounter, tickCounterLabel, frameRate)
  }
}
