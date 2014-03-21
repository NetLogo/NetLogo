// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace.model

import scalaz.Scalaz.ToStringOpsFromString

/*
case class Model(code: String = "", previewCode: String = "", widgets: List[Widget] = Nil, dimensions: api.WorldDimensions = api.WorldDimensions.square(16)) {
  val sliders = widgets.filter(_.isInstanceOf[Slider])
  val switches = widgets.filter(_.isInstanceOf[Switch])
  val choosers = widgets.filter(_.isInstanceOf[Chooser])
  val plots = widgets.filter(_.isInstanceOf[Plot])
  val inputBoxes = widgets.filter(_.isInstanceOf[InputBox[_]])

  override def toString =
    template.replace("<<CODE SECTION>>", code).
             replace("<<SLIDER SECTION>>\n",  sliders.mkString("\n\n") + "\n").
             replace("<<CHOOSER SECTION>>\n", choosers.mkString("\n\n") + "\n").
             replace("<<SWITCH SECTION>>\n",  switches.mkString("\n\n") + "\n").
             replace("<<INPUTBOX SECTION>>\n",  inputBoxes.mkString("\n\n") + "\n").
             replace("<<PLOT SECTION>>\n",    plots.mkString("\n\n") + "\n").
             replace("<<PREVIEW SECTION>>", previewCode).
             replace("<<TURTLE SHAPES SECTION>>", api.ModelReader.defaultShapes.mkString("\n")).
             replace("<<LINK SHAPES SECTION>>", api.ModelReader.defaultLinkShapes.mkString("\n")).
             replace("<<WRAPPING-ALLOWED-IN-X>>", (if (dimensions.wrappingAllowedInX) "1" else "0")).
             replace("<<WRAPPING-ALLOWED-IN-Y>>", (if (dimensions.wrappingAllowedInY) "1" else "0")).
             replace("<<MAX-PXCOR-OR-MINUS-ONE>>",
               (if (dimensions.minPxcor == -dimensions.maxPxcor)
                  dimensions.maxPxcor else -1).toString).
             replace("<<MAX-PYCOR-OR-MINUS-ONE>>",
               (if (dimensions.minPycor == -dimensions.maxPycor)
                  dimensions.maxPycor else -1).toString).
             replace("<<MIN-PXCOR>>", dimensions.minPxcor.toString).
             replace("<<MAX-PXCOR>>", dimensions.maxPxcor.toString).
             replace("<<MIN-PYCOR>>", dimensions.minPycor.toString).
             replace("<<MAX-PYCOR>>", dimensions.maxPycor.toString)
}*/

sealed trait Widget
case class Button(display: String, left: Integer, top: Integer, right: Integer, bottom: Integer,
             source: List[String], forever: Boolean) extends Widget
case class Switch(display: String, left: Integer, top: Integer, right: Integer, bottom: Integer,
             varName: String) extends Widget
case class Slider(display: String, left: Integer, top: Integer, right: Integer, bottom: Integer,
             varName: String, min: String, max: String, default: Float, step: String) extends Widget
case class Monitor(display: String, left: Integer, top: Integer, right: Integer, bottom: Integer,
             source: String, precision: Integer) extends Widget
case class Graph(display: String, left: Integer, top: Integer, right: Integer, bottom: Integer,
             ymin: Float, ymax: Float, xmin: Float, xmax: Float) extends Widget
case class Output(left: Integer, top: Integer, right: Integer, bottom: Integer) extends Widget
case class View(left: Integer, top: Integer, right: Integer, bottom: Integer) extends Widget

/*
trait WidgetLine {
  type T
  def parse(line: String): T
  def format(v: T): String
  def valid(v: String): Boolean
}

object IntegerLine extends WidgetLine {
  type T = Integer
  def parse(line: String): Integer = line.toInt
  def format(v: Integer): String = v.toString()
  def valid(v: String): Boolean = v.parseInt.isSuccess
}
object FloatLine extends WidgetLine {
  type T = Float
  def parse(line: String): Float = line.toFloat
  def format(v: Float): String = v.toString
  def valid(v: String): Boolean = v.parseFloat.isSuccess
}
object CodeLine extends WidgetLine {
  type T = Array[String]
  def parse(line: String): Array[String] = line.split("\n")
  def format(v: Array[String]): String = v.mkString("\n")
  def valid(v: String): Boolean = true
}
object StringLine extends WidgetLine {
  type T = String
  def parse(line: String): String = line
  def format(v: String): String = v.mkString("\n")
  def valid(v: String): Boolean = true
}
class SpecifiedLine(str: String) extends WidgetLine {
  type T = Unit
  def parse(line: String): Unit = {}
  def format(x: Unit): String = str
  def valid(v: String): Boolean = v == str
}*/
trait WidgetLine[T] {
  def parse(line: String): T
  def format(v: T): String
  def valid(v: String): Boolean
}

object IntegerLine extends WidgetLine[Integer] {
  def parse(line: String): Integer = line.toInt
  def format(v: Integer): String = v.toString()
  def valid(v: String): Boolean = v.parseInt.isSuccess
}
object FloatLine extends WidgetLine[Float] {
  type T = Float
  def parse(line: String): Float = line.toFloat
  def format(v: Float): String = v.toString
  def valid(v: String): Boolean = v.parseFloat.isSuccess
}
object CodeLine extends WidgetLine[List[String]] {
  def parse(line: String): List[String] = line.split("\n").toList
  def format(v: List[String]): String = v.mkString("\n")
  def valid(v: String): Boolean = true
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

abstract class WidgetParser {
  type T <: Widget
  def definition: List[WidgetLine[_]]
  def asList(t: T): List[Any]
  def asAnyRef(vals: List[Any]): T
  def format(t: T): List[String] = {
    definition.asInstanceOf[List[WidgetLine[Any]]].zip(asList(t)).map{case (d, v) => d.format(v)}
  }
  def validate(lines: List[String]): Boolean =
    lines.size == definition.size && (definition zip lines).forall{case (d, l) => d.valid(l)}
  def construct(lines: List[String]): T =
    asAnyRef(definition.asInstanceOf[List[WidgetLine[AnyRef]]].zip(lines).map{case (d, s) => d.parse(s)})
}

object ButtonParser extends WidgetParser {
  type T = Button
  def definition = List(new SpecifiedLine("BUTTON"),
                        StringLine,
                        IntegerLine,
                        IntegerLine)
  def asList(button: Button) = List(Unit, button.display, button.left, button.top)
  def asAnyRef(vals: List[Any]): Button = {
    val _ :: (display: String) :: (left: Int) :: (right: Int) :: (top: Int) :: (bottom: Int) ::
             (source: List[String]) :: (forever: Boolean) :: Nil = vals
    new Button(v2, v3, v4, v5)
  }
}

/*
val counter = Iterator.from(0)
def quoted(s:String) = '"' + s + '"'

case class Plot(name: String, setupCode: String = "", updateCode: String = "", pens: List[Pen] = Nil) extends Widget{
  override def toString =
    "PLOT\n5\n5\n5\n5\n" + name + "\ntime\nnum of turtles\n0.0\n10.0\n0.0\n10.0\ntrue\nfalse\n" +
    quoted(setupCode) + " " + quoted(updateCode) + "\nPENS\n" + pens.mkString("\n")
}

case class Pen(name:String = "Pen" + counter.next, setupCode:String = "", updateCode: String = ""){
  override def toString =
    quoted(name) + " 1.0 0 -16777216 true " + quoted(setupCode) + " " + quoted(updateCode)
}

trait Direction
case object HORIZONTAL extends Direction
case object VERTICAL extends Direction

case class Slider(name: String = "Slider" + counter.next,
                  // all of these should be something other than string. however,
                  // constraints can be foo + 30, so we need to allow for that.
                  // so we currently, clients use "10" even though they want just 10.
                  // this is something we should work out in the future. josh - 3/5/10
                  min: String = "0", max: String = "100", current: Float = 50, inc: String = "1",
                  units:String = "NIL", direction: Direction = HORIZONTAL) extends Widget{
  override def toString = "SLIDER\n5\n5\n5\n5\n" + name + "\n" + name + "\n" +
    min + "\n" + max + "\n" + current + "\n" + inc + "\n1\n" + units + "\n" + direction
}

case class Switch(name:String = "Switch" + counter.next, on: Boolean = true) extends Widget {
  override def toString = "SWITCH\n5\n5\n5\n5\n" + name + "\n" + name + "\n" +
   (if(on) "0" else "1") + "\n1\n1000"
}


case class Chooser(name:String = "Chooser" + counter.next, choices:List[Any] = Nil, index:Int = 0) extends Widget {
  def dump(o:Any) = api.Dump.logoObject(api.ScalaConversions.toLogoObject(o), true, false)
  override def toString =
    "CHOOSER\n5\n5\n5\n5\n" + name + "\n" + name + "\n" + choices.map(dump).mkString(" ") + "\n" + index + "\n"
}

object InputBoxTypes{
  abstract class InputBoxType(val name:String)
  case object Num extends InputBoxType("Number")
  case object Str extends InputBoxType("String")
  case object StrReporter extends InputBoxType("String (reporter)")
  case object StrCommand extends InputBoxType("String (commands)")
  case object Col extends InputBoxType("Color")
}

case class InputBox[T](name:String = "InputBox" + counter.next, multiline: Boolean = false,
        value: T = null, typ: InputBoxTypes.InputBoxType) extends Widget{
  override def toString =
    "INPUTBOX\n5\n5\n5\n5\n" + name + "\n" + value +
    "\n1\n" + (if(multiline) 1 else 0) + "\n" + typ.name + "\n"
}*/
