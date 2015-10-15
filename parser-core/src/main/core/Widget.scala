// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import ConstraintSpecification._

trait Widget

sealed trait DeclaresGlobal {
  def varName: String
}

sealed trait DeclaresConstraint {
  def varName: String
  def constraint: ConstraintSpecification
}

sealed trait DeclaresGlobalCommand {
  def varName: String
  def default: Any
  def asNetLogoString(x: Any): String = x match {
    case s: String => s""""${StringEscaper.escapeString(s)}""""
    case b: Boolean => if(b) "true" else "false"
    case l: List[Any] => l.map(asNetLogoString).mkString("[", " ", "]")
    case ll: LogoList => ll.toList.map(asNetLogoString).mkString("[" , " ", "]")
    case o: AnyRef => o.toString
  }
  def command: String = "set " + varName + " " + asNetLogoString(default)
}
case class Button(display: Option[String], left: Int, top: Int, right: Int, bottom: Int,
             source: String, forever: Boolean, buttonType: String = "OBSERVER",
             actionKey: String = "NIL", disableUntilTicksStart: Boolean = false) extends Widget

case class Plot(display: String, left: Int = 0, top: Int = 0, right: Int = 5, bottom: Int = 5,
             xAxis: String = "", yAxis: String = "", xmin: Double = 0, xmax: Double = 0, ymin: Double = 0, ymax: Double = 0,
             autoPlotOn: Boolean = true, legendOn: Boolean = false,
             setupCode: String = "", updateCode: String = "", pens: List[Pen] = Nil) extends Widget
case class Pen(display: String, interval: Double = 1, mode: Int = 0, color: Int = 0, inLegend: Boolean = false,
             setupCode: String = "", updateCode: String = "") extends Widget
case class TextBox(display: String, left: Int = 0, top: Int = 0, right: Int = 5, bottom: Int = 5,
             fontSize: Int, color: Double, transparent: Boolean) extends Widget
case class Switch(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, on: Boolean = false)
              extends Widget with DeclaresGlobal with DeclaresGlobalCommand with DeclaresConstraint {
  override def default = on
  override def constraint = BooleanConstraintSpecification(default)
}

sealed trait Chooseable {
  type ChosenType <: AnyRef

  def value: ChosenType
}

object Chooseable {
  def apply(a: AnyRef): Chooseable = {
    a match {
      case s: String => ChooseableString(s)
      case d: java.lang.Double => ChooseableDouble(d)
      case b: java.lang.Boolean => ChooseableBoolean(b)
      case l: LogoList => ChooseableList(l)
      case invalidElement => throw new RuntimeException(s"Invalid chooser option $invalidElement")
    }
  }
}

case class ChooseableDouble(value: java.lang.Double) extends Chooseable {
  type ChosenType = java.lang.Double
}

case class ChooseableString(value: String) extends Chooseable {
  type ChosenType = String
}

case class ChooseableList(value: LogoList) extends Chooseable {
  type ChosenType = LogoList
}

case class ChooseableBoolean(value: java.lang.Boolean) extends Chooseable {
  type ChosenType = java.lang.Boolean
}

case class Chooser(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, choices: List[Chooseable] = Nil, currentChoice: Int = 0)
           extends Widget with DeclaresGlobal with DeclaresGlobalCommand with DeclaresConstraint {
  override def default = choices(currentChoice).value
  override def constraint = ChoiceConstraintSpecification(choices.map(_.value), currentChoice)
}

sealed trait Direction
case object Horizontal extends Direction
case object Vertical extends Direction
case class Slider(display: String, left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0,
             varName: String, min: String = "1", max: String = "10", default: Double = 1, step: String = "1",
             units: Option[String] = None, direction: Direction = Horizontal)
             extends Widget with DeclaresGlobal with DeclaresGlobalCommand with DeclaresConstraint {
  override def constraint = NumericConstraintSpecification(default)
}
case class Monitor(display: Option[String], left: Int, top: Int, right: Int, bottom: Int,
             source: String, precision: Int, fontSize: Int) extends Widget

case class Output(left: Int, top: Int, right: Int, bottom: Int, fontSize: Int) extends Widget

sealed abstract class InputBoxType(val name:String)
case object Num extends InputBoxType("Number")
case object Str extends InputBoxType("String")
case object StrReporter extends InputBoxType("String (reporter)")
case object StrCommand extends InputBoxType("String (commands)")
case object Col extends InputBoxType("Color")

case class InputBox[T](left: Int = 0, top: Int = 0, right: Int = 0, bottom: Int = 0, varName: String,
             value: T, multiline: Boolean = false, boxtype: InputBoxType)
           extends Widget with DeclaresGlobal with DeclaresGlobalCommand with DeclaresConstraint {
  override def default = value
  override def constraint = boxtype match {
    case Col => NumericInputConstraintSpecification(boxtype.name, value.asInstanceOf[Int].toDouble)
    case Num => NumericInputConstraintSpecification(boxtype.name, value.asInstanceOf[Double])
    case _ => StringInputConstraintSpecification(boxtype.name, value.asInstanceOf[String])
  }
}
case class View(left: Int = 0, top: Int = 0, right: Int = 5, bottom: Int = 5,
  patchSize: Double = 12, fontSize: Int = 9, wrappingAllowedInX: Boolean = true, wrappingAllowedInY: Boolean = true,
  minPxcor: Int = 0, maxPxcor: Int = 0, minPycor: Int = 0, maxPycor: Int = 0,
  updateMode: UpdateMode = UpdateMode.TickBased, showTickCounter: Boolean = true, tickCounterLabel: String = "ticks",
  frameRate: Double = 25) extends Widget {

  def dimensions: WorldDimensions = new WorldDimensions(minPxcor, maxPxcor, minPycor, maxPycor,
                                                        patchSize, wrappingAllowedInX, wrappingAllowedInY)
}
object View {
  def square(dim: Int) = View(minPxcor = -dim, maxPxcor = dim, minPycor = -dim, maxPycor = dim)
}


