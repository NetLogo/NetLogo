// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import scalaz.Scalaz.ToStringOpsFromString

import org.nlogo.api.UpdateMode

sealed trait Widget
case class Button(display: String, left: Int, top: Int, right: Int, bottom: Int,
             source: String, forever: Boolean) extends Widget
case class Plot(display: String, left: Int = 0, top: Int = 0, right: Int = 5, bottom: Int = 5,
             xAxis: String = "", yAxis: String = "", ymin: Float = 0, ymax: Float = 0, xmin: Float = 0, xmax: Float = 0,
             autoPlotOn: Boolean = true, legendOn: Boolean = false,
             setupCode: String = "", updateCode: String = "", pens: Pens = Pens()) extends Widget
case class Pen(display: String, interval: Float = 1, mode: Int = 0, color: Int = 0, inLegend: Boolean = false,
             setupCode: String = "", updateCode: String = "") extends Widget
case class Pens(pens: List[Pen] = Nil) extends Widget
case class Switch(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, on: Boolean = false) extends Widget
case class Chooser(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, choices: List[String] = Nil, currentChoice: Int = 0) extends Widget
sealed trait Direction
case object Horizontal extends Direction
case object Vertical extends Direction
case class Slider(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, min: String = "1", max: String = "10", default: Float = 1, step: String = "1",
             units: String = "", direction: Direction = Horizontal) extends Widget
case class Monitor(display: String, left: Int, top: Int, right: Int, bottom: Int,
             source: String, precision: Int) extends Widget
case class Output(left: Int, top: Int, right: Int, bottom: Int) extends Widget

abstract class InputBoxType[T](val name:String)
case object Num extends InputBoxType[Double]("Number")
case object Str extends InputBoxType[String]("String")
case object StrReporter extends InputBoxType[String]("Reporter")
case object StrCommand extends InputBoxType[String]("Commands")
case object Col extends InputBoxType[Int]("Color")
case class InputBox[T](left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0, varName: String,
             value: T, multiline: Boolean = false, boxtype: InputBoxType[T]) extends Widget
case class View(left: Int = 0, top: Int = 0, right: Int = 5, bottom: Int = 5,
  patchSize: Float = 12, fontSize: Float = 9, wrappingAllowedInX: Boolean = true, wrappingAllowedInY: Boolean = true,
  minPxcor: Int = -5, maxPxcor: Int = 5, minPycor: Int = -5, maxPycor: Int = 5,
  updateMode: UpdateMode = UpdateMode.TickBased, showTickCounter: Boolean = true, tickCounterLabel: String = "ticks",
  frameRate: Int = 25) extends Widget


trait WidgetLine[T] {
  def parse(line: String): T
  def format(v: T): String
  def valid(v: String): Boolean
}

object IntLine extends WidgetLine[Int] {
  def parse(line: String): Int = line.toInt
  def format(v: Int): String = v.toString()
  def valid(v: String): Boolean = v.parseInt.isSuccess
}
object BooleanLine extends WidgetLine[Boolean] {
  def parse(line: String): Boolean = line == "1"
  def format(v: Boolean): String = if(v) "1" else "0"
  def valid(v: String): Boolean = v == "1" || v == "0"
}
object FloatLine extends WidgetLine[Float] {
  type T = Float
  def parse(line: String): Float = line.toFloat
  def format(v: Float): String = v.toString
  def valid(v: String): Boolean = v.parseFloat.isSuccess
}
object StringLine extends WidgetLine[String] {
  def parse(line: String): String = line
  def format(v: String): String = v.mkString("\n")
  def valid(v: String): Boolean = true
}
class SpecifiedLine(str: String) extends WidgetLine[Unit] {
  def parse(line: String): Unit = {}
  def format(x: Unit): String = str
  def valid(v: String): Boolean = v == str
}
class MapLine[T](map: List[Tuple2[String, T]]) extends WidgetLine[T] {
  def parse(line: String): T = map.collectFirst({case (v, x) => x}).get
  def format(v: T): String = map.collectFirst({case (x, v) => x}).get
  def valid(v: String): Boolean = map.collectFirst({case (v, x) => x}).nonEmpty
}
class ReservedLine(output: String) extends WidgetLine[Unit] {
  def parse(line: String): Unit = {}
  def format(x: Unit): String = output
  def valid(v: String): Boolean = true
}
object ReservedLine extends ReservedLine("RESERVED") {
  def apply(output: String) = new ReservedLine(output)
}

trait WidgetReader {
  type T <: Widget
  def format(t: T): List[String]
  def validate(lines: List[String]): Boolean
  def parse(lines: List[String]): T
}

object WidgetReader {
  def read(lines: List[String]): Widget = {
    val readers = List(ButtonReader, SliderReader)
    readers.find(_.validate(lines)) match {
      case Some(reader) => reader.parse(lines)
      case None => throw new Exception("Couldn't find corresponding reader!")
    }
  }

  def format(widget: Widget): List[String] = {
    widget match {
      case _:Button => ButtonReader.format(widget.asInstanceOf[Button])
      case _ => throw new Exception("XXX IMPLEMENT ME")
    }
  }

  def readInterface(lines: List[String]): List[Widget] = {
    var widgets = Vector[Widget]()
    var widgetLines = Vector[String]()
    for(line <- lines)
      if(line.nonEmpty)
        widgetLines :+= line
      else {
        if(!widgetLines.forall(_.isEmpty))
          widgets :+= read(widgetLines.toList)
        widgetLines = Vector()
      }
    if(!widgetLines.forall(_.isEmpty))
      widgets :+= read(widgetLines.toList)

    widgets.toList
  }
}

abstract class BaseWidgetReader extends WidgetReader {
  type T <: Widget
  def definition: List[WidgetLine[_]]
  def asList(t: T): List[Any]
  def asAnyRef(vals: List[Any]): T
  def format(t: T): List[String] = {
    definition.asInstanceOf[List[WidgetLine[Any]]].zip(asList(t)).map{case (d, v) => d.format(v)}
  }
  def validate(lines: List[String]): Boolean =
    lines.size == definition.size && (definition zip lines).forall{case (d, l) => d.valid(l)}
  def parse(lines: List[String]): T =
    asAnyRef(definition.asInstanceOf[List[WidgetLine[AnyRef]]].zip(lines).map{case (d, s) => d.parse(s)})
}

object ButtonReader extends BaseWidgetReader {
  type T = Button
  def definition = List(new SpecifiedLine("BUTTON"),
                        StringLine,   // display
                        IntLine,  // left
                        IntLine,  // top
                        IntLine,  // right
                        IntLine,  // bottom
                        StringLine,     // code to execute
                        BooleanLine)  // forever?
  def asList(button: Button) = List((), button.display, button.left, button.top, button.right, button.bottom,
                                    button.source, button.forever)
  def asAnyRef(vals: List[Any]): Button = {
    val List(_, display: String, left: Int, top: Int, right: Int, bottom: Int,
      source: String, forever: Boolean) = vals
    new Button(display, left, top, right, bottom, source, forever)
  }
}

object PlotReader extends BaseWidgetReader {
  type T = Plot
  def definition = List(new SpecifiedLine("PLOT"),
                        IntLine,  // left
                        IntLine,  // top
                        IntLine,  // right
                        IntLine,  // bottom
                        StringLine,  // display
                        StringLine,  // xaxis
                        StringLine,  // yaxis
                        FloatLine,   // ymin
                        FloatLine,   // ymax
                        FloatLine,   // xmin
                        FloatLine,   // xmax
                        BooleanLine, // autoploton
                        BooleanLine, // legend on
                        StringLine   // Double code lines, parse later
                      )
  def asList(plot: Plot) = List((), plot.left, plot.right, plot.top, plot.bottom, plot.display,
                                    plot.xAxis, plot.yAxis, plot.ymin, plot.ymax, plot.xmin, plot.xmax,
                                    plot.autoPlotOn, plot.legendOn, """"" """"")
  def asAnyRef(vals: List[Any]): Plot = {
    val List(_, left: Int, right: Int, top: Int, bottom: Int, display: String,
      xAxis: String, yAxis: String, ymin: Float, ymax: Float, xmin: Float, xmax: Float,
      autoPlotOn: Boolean, legendOn: Boolean, _) = vals
    new Plot(display, left, top, right, bottom, xAxis, yAxis, ymin, ymax, xmin, xmax, autoPlotOn, legendOn, "", "")
  }
}

// To do later, for now just mocked out.  But the pens are of the format:
//   name interval mode color inLegend setupCode updateCode
object PensReader extends WidgetReader {
  type T = Pens
  def quoted(s:String) = '"' + s + '"'
  def formatPen(pen: Pen): String = quoted(pen.display) + " 1.0 0 -16777216 true " +
    quoted(pen.setupCode.mkString("\\n")) + " " + quoted(pen.updateCode.mkString("\\n"))
  def format(pens: Pens): List[String] = "PENS" :: pens.pens.map(formatPen(_))
  def validate(lines: List[String]): Boolean = true
  def parse(lines: List[String]): Pens = new Pens(Nil)
}

object SliderReader extends BaseWidgetReader {
  type T = Slider

  def definition = List(new SpecifiedLine("SLIDER"),
                        IntLine,  // left
                        IntLine,  // top
                        IntLine,  // right
                        IntLine,  // bottom
                        StringLine,   // display
                        StringLine,   // varname
                        StringLine,   // min
                        StringLine,   // max
                        FloatLine,    // default
                        StringLine,   // step
                        ReservedLine,
                        StringLine,   // units
                        new MapLine(List(("HORIZONTAL", Horizontal), ("VERTICAL", Vertical)))
                      ) 
  def asList(slider: Slider) = List((), slider.left, slider.right, slider.top, slider.bottom, slider.display,
                                    slider.varName, slider.min, slider.max, slider.default, slider.step,
                                    (), slider.units, slider.direction)
  def asAnyRef(vals: List[Any]): Slider = {
    val List(_, left: Int, right: Int, top: Int, bottom: Int, display: String, varName: String, min: String,
             max: String, default: Float, step: String, _, units: String, direction: Direction) = vals
    new Slider(display, left, top, right, bottom, varName, min, max, default, step, units, direction)
  }
}

object SwitchReader extends BaseWidgetReader {
  type T = Switch

  def definition = List(new SpecifiedLine("SWITCH"),
                        StringLine,   // display
                        IntLine,  // left
                        IntLine,  // top
                        IntLine,  // right
                        IntLine,  // bottom
                        StringLine,   // varname
                        BooleanLine,  // on
                        ReservedLine,
                        ReservedLine
                      ) 
  def asList(switch: Switch) = List(switch.display, switch.left, switch.right, switch.top, switch.bottom, switch.varName, (), ())
  def asAnyRef(vals: List[Any]): Switch = {
    val List(display: String, left: Int, right: Int, top: Int, bottom: Int, varName: String, on: Boolean, _, _) = vals
    new Switch(display, left, top, right, bottom, varName, on)
  }
}

object ChooserReader extends BaseWidgetReader {
  type T = Chooser

  def definition = List(new SpecifiedLine("CHOOSER"),
                        IntLine,  // left
                        IntLine,  // top
                        IntLine,  // right
                        IntLine,  // bottom
                        StringLine,   // display
                        StringLine,   // varname
                        StringLine,   // choices
                        IntLine   // current choice
                      ) 
  def asList(chooser: Chooser) = List(chooser.left, chooser.right, chooser.top, chooser.bottom, chooser.display,
    chooser.varName, chooser.choices, chooser.currentChoice)
  def asAnyRef(vals: List[Any]): Chooser = {
    val List(left: Int, right: Int, top: Int, bottom: Int, display: String, varName: String,
      choices: List[String] @unchecked, currentChoice: Int) = vals
    new Chooser(display, left, top, right, bottom, varName, choices, currentChoice)
  }
}

object InputBoxReader extends BaseWidgetReader {
  type T = InputBox[_]

  def definition = List(new SpecifiedLine("INPUTBOX"),
                        IntLine,  // left
                        IntLine,  // top
                        IntLine,  // right
                        IntLine,  // bottom
                        StringLine,   // varname
                        StringLine,   // value
                        BooleanLine,  // multiline
                        ReservedLine,
                        StringLine    // inputboxtype
                      ) 
  def asList(inputbox: InputBox[_]) = List(inputbox.left, inputbox.right, inputbox.top, inputbox.bottom, inputbox.varName,
    inputbox.value, inputbox.multiline, (), inputbox.boxtype)
  def asAnyRef(vals: List[Any]): InputBox[_] = {
    val List(left: Int, right: Int, top: Int, bottom: Int, varName: String, value: String,
      multiline: Boolean, _, inputBoxType: InputBoxType[Any] @unchecked) = vals
    new InputBox(left, top, right, bottom, varName, value, multiline, inputBoxType)
  }
}

object ViewReader extends BaseWidgetReader {
  type T = View

  def definition = List(new SpecifiedLine("GRAPHICS-WINDOW"),
                        IntLine,  // left
                        IntLine,  // top
                        IntLine,  // right
                        IntLine,  // bottom
                        ReservedLine("-1"), // maxPxCor or -1
                        ReservedLine("-1"), // maxPyCor or -1
                        FloatLine,    // patchsize
                        ReservedLine, // shapes on, not used
                        FloatLine,    // font size

                        ReservedLine, // hex settings
                        ReservedLine, // and 
                        ReservedLine, // exactDraw
                        ReservedLine, // not used

                        BooleanLine,  // wrappingAllowedInX
                        BooleanLine,  // wrappingAllowedInY
                        ReservedLine, // thin turtle pens!  Always on
                        IntLine,  // minPxcor
                        IntLine,  // maxPxcor
                        IntLine,  // minPycor
                        IntLine,  // maxPycor
                        new MapLine(List(("0", UpdateMode.Continuous), ("1", UpdateMode.TickBased))),
                        new MapLine(List(("0", UpdateMode.Continuous), ("1", UpdateMode.TickBased))), // Twice for compatibility
                        BooleanLine,  // showTickCounter
                        StringLine,   // tick counter label
                        IntLine   // frame rate
                      ) 
  def asList(view: View) = List(view.left, view.right, view.top, view.bottom, (), (), view.patchSize, (), view.fontSize,
    (), (), (), (), view.wrappingAllowedInX, view.wrappingAllowedInY, (),
    view.minPxcor, view.maxPxcor, view.minPycor, view.maxPycor,
    view.updateMode, view.updateMode, view.showTickCounter, view.tickCounterLabel, view.frameRate)
  def asAnyRef(vals: List[Any]): View = {
    val ((left: Int) :: (top: Int) :: (right: Int) :: (bottom: Int) :: _ :: _ :: (patchSize: Float) :: _ ::
         (fontSize: Float) :: _ :: _ :: _ :: _ :: (wrappingAllowedInX: Boolean) :: (wrappingAllowedInY: Boolean) ::
         _ :: (minPxcor: Int) :: (maxPxcor: Int) :: (minPycor: Int) :: (maxPycor: Int) :: (updateMode: UpdateMode) ::
         _ :: (showTickCounter: Boolean) :: (tickCounterLabel: String) :: (frameRate: Int) :: Nil) = vals
    new View(left, top, right, bottom, patchSize, fontSize,
      wrappingAllowedInX, wrappingAllowedInY, minPxcor, maxPxcor, minPycor, maxPycor,
      updateMode, showTickCounter, tickCounterLabel, frameRate)
  }
}
